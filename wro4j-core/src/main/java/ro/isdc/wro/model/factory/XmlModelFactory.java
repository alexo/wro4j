/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.model.ModelBuilder;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.XmlModelBuilder;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.RecursiveGroupDefinitionException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * Model factory implementation. Creates a WroModel object, based on an xml. This xml contains the description of all
 * groups.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class XmlModelFactory
  implements WroModelFactory, WroConfigurationChangeListener {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XmlModelFactory.class);

  /**
   * Default xml to parse.
   */
  protected static final String XML_CONFIG_FILE = "wro.xml";

  /**
   * CSS tag used in xml.
   */
  private static final String TAG_CSS = "css";

  /**
   * JS tag used in xml.
   */
  private static final String TAG_JS = "js";

  /**
   * GroupRef tag used in xml.
   */
  private static final String TAG_GROUP_REF = "group-ref";

  /**
   * Map between the group name and corresponding element. Hold the map<GroupName, Element> of all group nodes to access
   * any element.
   */
  final Map<String, Element> allGroupElements = new HashMap<String, Element>();

  /**
   * List of groups which are currently processing and are partially parsed. This list is useful in order to catch
   * infinite recurse group reference.
   */
  final Collection<String> processingGroups = new HashSet<String>();

  /**
   * Reference to cached model instance. Using volatile keyword fix the problem with double-checked locking in JDK 1.5.
   */
  private volatile WroModel model;

  /**
   * Scheduled executors service, used to refresh the WroModel.
   */
  private ScheduledExecutorService scheduler;

  /** Builders used to be aware of model changes. */
  private ModelBuilder<? extends Document> modelBuilder;

  /**
   * {@inheritDoc}
   */
  public synchronized WroModel getInstance() {
    initScheduler();
    // use double-check locking
    if (model == null) {
      synchronized (this) {
        if (model == null) {
          final StopWatch stopWatch = new StopWatch();
          stopWatch.start("Create Model");
          model = newModel();
          stopWatch.stop();
          LOG.debug(stopWatch.prettyPrint());
        }
      }
    }
    return model;
  }


  /**
   * Initialize executor service & start the thread responsible for updating the model.
   */
  private void initScheduler() {
    if (scheduler == null) {
      final long period = Context.get().getConfig().getModelUpdatePeriod();
      if (period > 0) {
        scheduler = Executors.newSingleThreadScheduledExecutor(WroUtil.createDaemonThreadFactory());
        // Run a scheduled task which updates the model.
        // Here a scheduleWithFixedDelay is used instead of scheduleAtFixedRate because the later can cause a problem
        // (thread tries to make up for lost time in some situations)
        LOG.info("Schedule Model Update for " + period + " seconds period");
        scheduler.scheduleWithFixedDelay(getSchedulerRunnable(), 0, period, TimeUnit.SECONDS);
      }
    }
  }


  /**
   * @return {@link Runnable} implementation which reloads the model when scheduled.
   */
  private Runnable getSchedulerRunnable() {
    return new Runnable() {
      public void run() {
    		try {
    			model = newModel();
    			//find a way to clear the cache
    			LOG.info("Wro Model (wro.xml) updated!");
    		} catch (final Exception e) {
      		LOG.error("Exception occured", e);
      	}
      }
    };
  }

  /**
   * Build model from scratch after xml is parsed.
   *
   * @return new instance of model.
   */
  private WroModel newModel() {
    if (modelBuilder == null) {
      modelBuilder = newModelBuilder();
    }

    // TODO return a single instance based on some configuration?
    Document document = modelBuilder.build();

    initGroupMap(document);

    // TODO cache model based on application Mode (DEPLOYMENT, DEVELOPMENT)
    return createModel();
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getConfigResourceAsStream() throws IOException {
    return getResourceAsStream(XML_CONFIG_FILE);
  }

  /**
   * Creates a new {@link ModelBuilder} used to read model groups and
   * resources.
   *
   * @return A new {@link ModelBuilder} bound to this model factory.
   */
  protected ModelBuilder<? extends Document> newModelBuilder() {
    return new XmlModelBuilder(this);
  }

  /**
   * Initialize the map
   */
  private void initGroupMap(final Document document) {
    final NodeList groupNodeList = document.getElementsByTagName(
        XmlModelBuilder.TAG_GROUP);
    for (int i = 0; i < groupNodeList.getLength(); i++) {
      final Element groupElement = (Element)groupNodeList.item(i);
      final String name = groupElement.getAttribute(
          XmlModelBuilder.ATTR_GROUP_NAME);
      allGroupElements.put(name, groupElement);
    }
  }


  /**
   * Parse the document and creates the model.
   *
   * @param document to parse.
   * @return {@link WroModel} object.
   */
  private synchronized WroModel createModel() {
    final WroModel model = new WroModel();
    final Set<Group> groups = new HashSet<Group>();
    for (final Element element : allGroupElements.values()) {
      parseGroup(element, groups);
    }
    model.setGroups(groups);
    return model;
  }


  /**
   * Recursive method. Add the parsed element group to the group collection. If the group contains group-ref element,
   * parse recursively this group.
   *
   * @param element Group Element to parse.
   * @param groups list of parsed groups where the parsed group is added..
   * @return list of resources associated with this resource
   */
  private Collection<Resource> parseGroup(final Element element, final Collection<Group> groups) {
    final String name = element.getAttribute(XmlModelBuilder.ATTR_GROUP_NAME);
    if (processingGroups.contains(name)) {
      throw new RecursiveGroupDefinitionException("Infinite Recursion detected for the group: " + name
        + ". Recursion path: " + processingGroups);
    }
    processingGroups.add(name);
    LOG.debug("\tgroupName=" + name);
    // skip if this group is already parsed
    final Group parsedGroup = getGroupByName(name, groups);
    if (parsedGroup != null) {
      // remove before returning
      // this group is parsed, remove from unparsed groups collection
      processingGroups.remove(name);
      return parsedGroup.getResources();
    }
    final Group group = new Group();
    group.setName(name);
    final List<Resource> resources = new ArrayList<Resource>();
    final NodeList resourceNodeList = element.getChildNodes();
    for (int i = 0; i < resourceNodeList.getLength(); i++) {
      final Node node = resourceNodeList.item(i);
      if (node instanceof Element) {
        final Element resourceElement = (Element)node;
        parseResource(resourceElement, resources, groups);
      }
    }
    group.setResources(resources);
    // this group is parsed, remove from unparsed collection
    processingGroups.remove(name);
    groups.add(group);
    return resources;
  }


  /**
   * Check if the group with name <code>name</code> was already parsed and returns Group object with it's resources
   * initialized.
   *
   * @param name the group to check.
   * @param groups list of parsed groups.
   * @return parsed Group by it's name.
   */
  private static Group getGroupByName(final String name, final Collection<Group> groups) {
    for (final Group group : groups) {
      if (name.equals(group.getName())) {
        return group;
      }
    }
    return null;
  }

  /**
   * Creates a resource from a given resourceElement. It can be css, js. If resource tag name is group-ref, the method
   * will start a recursive computation.
   *
   * @param resourceElement
   * @param resources list of parsed resources where the parsed resource is added.
   */
  private void parseResource(final Element resourceElement, final Collection<Resource> resources, final Collection<Group> groups) {
    ResourceType type = null;
    final String tagName = resourceElement.getTagName();
    final String uri = resourceElement.getTextContent();
    if (TAG_JS.equals(tagName)) {
      type = ResourceType.JS;
    } else if (TAG_CSS.equals(tagName)) {
      type = ResourceType.CSS;
    } else if (TAG_GROUP_REF.equals(tagName)) {
      // uri in this case is the group name
      final Element groupElement = allGroupElements.get(uri);
      resources.addAll(parseGroup(groupElement, groups));
    } else {
      // should not ever happen due to validation of xml.
      throw new WroRuntimeException("Usupported resource type: " + tagName);
    }
    if (type != null) {
      final String minimizeAsString = resourceElement
          .getAttribute(XmlModelBuilder.ATTR_MINIMIZE);
      final boolean minimize = StringUtils.isEmpty(minimizeAsString) ? true :
          Boolean.valueOf(resourceElement
              .getAttribute(XmlModelBuilder.ATTR_MINIMIZE));
      final Resource resource = Resource.create(uri, type);
      resource.setMinimize(minimize);
      resources.add(resource);
    }
  }


  /**
   * @return InputStream of the local resource from classpath.
   */
  protected static InputStream getResourceAsStream(final String fileName) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
  }

  /**
   * {@inheritDoc}
   */
  public void onCachePeriodChanged() {
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged() {
  	if (scheduler != null) {
  	  scheduler.shutdown();
  	  scheduler = null;
  	}
    //force scheduler to reload
  	model = null;
  }

  /**
   * {@inheritDoc}
   */
  public void onModelChanged() {
    Context.get().getConfig().reloadModel();
    Context.get().getConfig().reloadCache();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    //kill running threads
    if (scheduler != null) {
      scheduler.shutdownNow();
    }
  }

}
