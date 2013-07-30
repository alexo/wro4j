/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.http;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collection;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.factory.PropertiesAndFilterConfigWroConfigurationFactory;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.factory.DefaultRequestHandlerFactory;
import ro.isdc.wro.http.handler.factory.RequestHandlerFactory;
import ro.isdc.wro.http.support.ResponseHeadersConfigurer;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.DefaultWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.WroUtil;


/**
 * Main entry point. Perform the request processing by identifying the type of the requested resource. Depending on the
 * way it is configured.
 *
 * @author Alex Objelean
 * @created Created on Oct 31, 2008
 */
public class WroFilter
    implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(WroFilter.class);
  /**
   * The prefix to use for default mbean name.
   */
  private static final String MBEAN_PREFIX = "wro4j-";
  /**
   * Filter config.
   */
  private FilterConfig filterConfig;
  /**
   * Wro configuration.
   */
  private WroConfiguration wroConfiguration;
  /**
   * WroManagerFactory. The core of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;
  /**
   * Used to create the collection of requestHandlers to apply
   */
  private RequestHandlerFactory requestHandlerFactory = newRequestHandlerFactory();

  private ResponseHeadersConfigurer headersConfigurer;
  /**
   * Flag used to toggle filter processing. When this flag is false, the filter will proceed with chaining. This flag is
   * true by default.
   */
  private boolean enable = true;
  private Injector injector;
  private MBeanServer mbeanServer = null;

  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config)
      throws ServletException {
    this.filterConfig = config;
    // invoke createConfiguration method only if the configuration was not set.
    this.wroConfiguration = wroConfiguration == null ? createConfiguration() : wroConfiguration;
    this.wroManagerFactory = createWroManagerFactory();
    headersConfigurer = newResponseHeadersConfigurer();
    registerChangeListeners();
    registerMBean();
    doInit(config);
    LOG.info("wro4j version: {}", WroUtil.getImplementationVersion());
    LOG.info("wro4j configuration: {}", wroConfiguration);
  }

  /**
   * Creates configuration by looking up in servletContext attributes. If none is found, a new one will be created using
   * the configuration factory.
   *
   * @return {@link WroConfiguration} object.
   */
  private WroConfiguration createConfiguration() {
    // Extract config from servletContext (if already configured)
    // TODO use a named helper
    final WroConfiguration configAttribute = ServletContextAttributeHelper.create(filterConfig).getWroConfiguration();
    return configAttribute != null ? configAttribute : newWroConfigurationFactory(filterConfig).create();
  }

  /**
   * Creates {@link WroManagerFactory}.
   */
  private WroManagerFactory createWroManagerFactory() {
    if (this.wroManagerFactory == null) {
      // TODO use a named helper
      final WroManagerFactory managerFactoryAttribute = ServletContextAttributeHelper.create(filterConfig)
          .getManagerFactory();
      LOG.debug("managerFactory attribute: {}", managerFactoryAttribute);
      return managerFactoryAttribute != null ? managerFactoryAttribute : newWroManagerFactory();
    }
    LOG.debug("created managerFactory: {}", wroManagerFactory);
    return this.wroManagerFactory;
  }

  /**
   * Expose MBean to tell JMX infrastructure about our MBean (only if jmxEnabled is true).
   */
  private void registerMBean() {
    if (wroConfiguration.isJmxEnabled()) {
      try {
        mbeanServer = getMBeanServer();
        final ObjectName name = getMBeanObjectName();
        if (!mbeanServer.isRegistered(name)) {
          mbeanServer.registerMBean(wroConfiguration, name);
        }
      } catch (final JMException e) {
        LOG.error("Exception occured while registering MBean", e);
      }
    }
  }

  private void unregisterMBean() {
    try {
      if (mbeanServer != null && mbeanServer.isRegistered(getMBeanObjectName())) {
        mbeanServer.unregisterMBean(getMBeanObjectName());
      }
    } catch (final JMException e) {
      LOG.error("Exception occured while registering MBean", e);
    }
  }

  private ObjectName getMBeanObjectName()
      throws MalformedObjectNameException {
    return new ObjectName(newMBeanName(), "type", WroConfiguration.class.getSimpleName());
  }

  /**
   * @return the name of MBean to be used by JMX to configure wro4j.
   */
  protected String newMBeanName() {
    String mbeanName = wroConfiguration.getMbeanName();
    if (StringUtils.isEmpty(mbeanName)) {
      final String contextPath = getContextPath();
      mbeanName = StringUtils.isEmpty(contextPath) ? "ROOT" : contextPath;
      mbeanName = MBEAN_PREFIX + mbeanName;
    }
    return mbeanName;
  }

  /**
   * @return Context path of the application.
   */
  private String getContextPath() {
    String contextPath = null;
    try {
      contextPath = (String) ServletContext.class.getMethod("getContextPath", new Class<?>[] {}).invoke(
          filterConfig.getServletContext(), new Object[] {});
    } catch (final Exception e) {
      contextPath = "DEFAULT";
      LOG.warn("Couldn't identify contextPath because you are using older version of servlet-api (<2.5). Using "
          + contextPath + " contextPath.");
    }
    return contextPath.replaceFirst(ServletContextUriLocator.PREFIX, "");
  }

  /**
   * Override this method if you want to provide a different MBeanServer.
   *
   * @return {@link MBeanServer} to use for JMX.
   */
  protected MBeanServer getMBeanServer() {
    return ManagementFactory.getPlatformMBeanServer();
  }

  /**
   * Register property change listeners.
   */
  private void registerChangeListeners() {
    wroConfiguration.registerCacheUpdatePeriodChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent event) {
        // reset cache headers when any property is changed in order to avoid browser caching
        headersConfigurer = newResponseHeadersConfigurer();
        wroManagerFactory.onCachePeriodChanged(valueAsLong(event.getNewValue()));
      }
    });
    wroConfiguration.registerModelUpdatePeriodChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent event) {
        headersConfigurer = newResponseHeadersConfigurer();
        wroManagerFactory.onModelPeriodChanged(valueAsLong(event.getNewValue()));
      }
    });
    LOG.debug("Cache & Model change listeners were registered");
  }

  /**
   * @return the {@link ResponseHeadersConfigurer}.
   */
  protected ResponseHeadersConfigurer newResponseHeadersConfigurer() {
    return ResponseHeadersConfigurer.fromConfig(wroConfiguration);
  }

  /**
   * @return default implementation of {@link RequestHandlerFactory}
   */
  protected RequestHandlerFactory newRequestHandlerFactory() {
    return new DefaultRequestHandlerFactory();
  }

  private long valueAsLong(final Object value) {
    Validate.notNull(value);
    return Long.valueOf(String.valueOf(value)).longValue();
  }

  /**
   * Custom filter initialization - can be used for extended classes.
   *
   * @see Filter#init(FilterConfig).
   */
  protected void doInit(final FilterConfig config)
      throws ServletException {
  }

  /**
   * {@inheritDoc}
   */
  public final void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    if (isFilterActive(request)) {
      try {
        // add request, response & servletContext to thread local
        Context.set(Context.webContext(request, response, filterConfig), wroConfiguration);

        if (!handledWithRequestHandler(request, response)) {
          processRequest(request, response);
          onRequestProcessed();
        }
        onProcessComplete();
      } catch (final Exception e) {
        onException(e, response, chain);
      } finally {
        Context.unset();
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  /**
   * clear the cache if the {@link WroConfiguration#isDisableCache()} flag is set to true.
   */
  private void onProcessComplete() {
    // Destroy the cached model after the processing is done if cache flag is disabled
    if (wroConfiguration.isDisableCache()) {
      LOG.debug("Disable Cache is true. Destroying model...");
      final WroManager manager = this.wroManagerFactory.create();
      manager.getModelFactory().destroy();
      manager.getCacheStrategy().clear();
    }
  }

  private boolean handledWithRequestHandler(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    final Collection<RequestHandler> handlers = requestHandlerFactory.create();
    Validate.notNull(handlers, "requestHandlers cannot be null!");
    // create injector used for process injectable fields from each requestHandler.
    final Injector injector = getInjector();
    for (final RequestHandler requestHandler : handlers) {
      injector.inject(requestHandler);
      if (requestHandler.isEnabled() && requestHandler.accept(request)) {
        requestHandler.handle(request, response);
        return true;
      }
    }
    return false;
  }

  /**
   * @return {@link Injector} used to inject {@link RequestHandler}'s.
   * @VisibleForTesting
   */
  Injector getInjector() {
    if (injector == null) {
      injector = InjectorBuilder.create(wroManagerFactory).build();
    }
    return injector;
  }

  /**
   * Perform actual processing.
   */
  private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    setResponseHeaders(response);
    // process the uri using manager
    wroManagerFactory.create().process();
  }

  /**
   * @return true if the filter should be applied or proceed with chain otherwise.
   */
  private boolean isFilterActive(final HttpServletRequest request) {
    // prevent StackOverflowError by skipping the already included wro request
    return enable && !DispatcherStreamLocator.isIncludedRequest(request);
  }

  /**
   * Invoked when a {@link Exception} is thrown. Allows custom exception handling. The default implementation proceeds
   * with filter chaining when exception is thrown.
   *
   * @param e
   *          {@link Exception} thrown during request processing.
   */
  protected void onException(final Exception e, final HttpServletResponse response, final FilterChain chain) {
    LOG.debug("Exception occured", e);
    try {
      LOG.debug("Cannot process. Proceeding with chain execution.");
      chain.doFilter(Context.get().getRequest(), response);
    } catch (final Exception ex) {
      // should never happen (use debug level to suppress unuseful logs)
      LOG.debug("Error while chaining the request", e);
    }
  }

  /**
   * Method called for each request and responsible for setting response headers, used mostly for cache control.
   * Override this method if you want to change the way headers are set.<br>
   *
   * @param response
   *          {@link HttpServletResponse} object.
   */
  protected void setResponseHeaders(final HttpServletResponse response) {
    headersConfigurer.setHeaders(response);
  }

  /**
   * Allows external configuration of {@link WroManagerFactory} (ex: using spring IoC). When this value is set, the
   * default {@link WroManagerFactory} initialization won't work anymore.
   * <p/>
   * Note: call this method before {@link WroFilter#init(FilterConfig)} is invoked.
   *
   * @param wroManagerFactory
   *          the wroManagerFactory to set
   */
  public void setWroManagerFactory(final WroManagerFactory wroManagerFactory) {
    this.wroManagerFactory = wroManagerFactory;
  }

  /**
   * @return configured and decorated {@link WroManagerFactory} instance.
   */
  public final WroManagerFactory getWroManagerFactory() {
    return this.wroManagerFactory;
  }

  /**
   * Sets the RequestHandlerFactory used to create the collection of requestHandlers
   *
   * @param requestHandlerFactory
   *          to set
   */
  public void setRequestHandlerFactory(final RequestHandlerFactory requestHandlerFactory) {
    Validate.notNull(requestHandlerFactory);
    this.requestHandlerFactory = requestHandlerFactory;
  }

  /**
   * Factory method for {@link WroManagerFactory}.
   * <p/>
   * Creates a {@link WroManagerFactory} configured in {@link WroConfiguration} using reflection. When no configuration
   * is found a default implementation is used.
   * </p>
   * Note: this method is not invoked during initialization if a {@link WroManagerFactory} is set using
   * {@link WroFilter#setWroManagerFactory(WroManagerFactory)}.
   *
   * @return {@link WroManagerFactory} instance.
   */
  protected WroManagerFactory newWroManagerFactory() {
    return DefaultWroManagerFactory.create(wroConfiguration);
  }

  /**
   * @return implementation of {@link ObjectFactory<WroConfiguration>} used to create a {@link WroConfiguration} object.
   */
  protected ObjectFactory<WroConfiguration> newWroConfigurationFactory(final FilterConfig filterConfig) {
    return new PropertiesAndFilterConfigWroConfigurationFactory(filterConfig);
  }

  /**
   * @return the {@link WroConfiguration} associated with this filter instance.
   * @VisibleForTesting
   */
  public final WroConfiguration getConfiguration() {
    return this.wroConfiguration;
  }

  /**
   * Once set, this configuration will be used, instead of the one built by the factory.
   *
   * @param config
   *          a not null {@link WroConfiguration} to set.
   */
  public final void setConfiguration(final WroConfiguration config) {
    Validate.notNull(config);
    this.wroConfiguration = config;
  }

  /**
   * Sets the enable flag used to toggle filter. This might be useful when the filter has to be enabled/disabled based
   * on environment configuration.
   *
   * @param enable
   *          flag for enabling the {@link WroFilter}.
   */
  public void setEnable(final boolean enable) {
    this.enable = enable;
  }

  /**
   * Useful for unit tests to check the post processing.
   */
  protected void onRequestProcessed() {
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    //Avoid memory leak by unregistering mBean on destroy
    unregisterMBean();
    if (wroManagerFactory != null) {
      wroManagerFactory.destroy();
    }
    if (wroConfiguration != null) {
      wroConfiguration.destroy();
    }
    Context.destroy();
  }

  public void setHeadersConfigurer(ResponseHeadersConfigurer headersConfigurer) {
    this.headersConfigurer = headersConfigurer;
  }
}
