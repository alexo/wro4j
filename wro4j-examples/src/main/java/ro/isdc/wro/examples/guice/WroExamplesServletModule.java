package ro.isdc.wro.examples.guice;

import java.util.Map;

import javax.servlet.FilterConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.directwebremoting.servlet.DwrServlet;
import org.springframework.web.servlet.DispatcherServlet;

import ro.isdc.wro.examples.web.ExternalResourceServlet;
import ro.isdc.wro.examples.web.WebResourceOptimizationApplication;
import ro.isdc.wro.examples.web.http.DispatchResourceServlet;
import ro.isdc.wro.examples.web.http.DynamicResourceServlet;
import ro.isdc.wro.examples.web.http.RedirectResourceServlet;
import ro.isdc.wro.http.WroFilter;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

/**
 * @author Alex Objelean
 */
final class WroExamplesServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    //bindings
    bind(WroFilter.class).in(Singleton.class);
    bind(WicketFilter.class).in(Singleton.class);
    bind(DwrServlet.class).in(Singleton.class);
    bind(ExternalResourceServlet.class).in(Singleton.class);
    bind(DynamicResourceServlet.class).in(Singleton.class);
    bind(RedirectResourceServlet.class).in(Singleton.class);
    bind(DispatchResourceServlet.class).in(Singleton.class);
    bind(DispatcherServlet.class).in(Singleton.class);

    //filters
    //find out how to add dispatchers to the filter mapping configuration
    filter("/wro/*").through(WroFilter.class);
    wicketFilter("/*");

    //servlets
    springMvcServlet("/api/*");
    serve("/dwr/*").with(DwrServlet.class);
    serve("/external/*").with(ExternalResourceServlet.class);
    serve("/resource/dynamic.js").with(DynamicResourceServlet.class);
    serve("/resource/redirect.js").with(RedirectResourceServlet.class);
    serve("/resource/dispatch.js").with(DispatchResourceServlet.class);
  }

  /**
   * @param path
   */
  private void springMvcServlet(final String path) {
    final Map<String, String> initParamsMap = Maps.newHashMap();
    initParamsMap.put("contextConfigLocation", "/WEB-INF/applicationContext.xml");
    serve(path).with(DispatcherServlet.class, initParamsMap);
  }

  /**
   * Prepare the wicket filter
   */
  private void wicketFilter(final String wicketFilterPath) {
    final Map<String, String> wicketFilterMap = Maps.newHashMap();
    wicketFilterMap.put(ContextParamWebApplicationFactory.APP_CLASS_PARAM,
      WebResourceOptimizationApplication.class.getName());
    filter(wicketFilterPath).through(new WicketFilter() {
      @Override
      protected String getFilterPathFromConfig(final FilterConfig filterConfig) {
        return StringUtils.EMPTY;
      }
    }, wicketFilterMap);
  }
}