/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Random;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ContextPropagatingCallable;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestGroupsProcessor.
 * 
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestGroupsProcessor {
  private GroupsProcessor victim;
  final String groupName = "group";
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new GroupsProcessor();
    initVictim(new WroConfiguration());
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  private void initVictim(final WroConfiguration config) {
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(new WroModel().addGroup(new Group(groupName)));
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(modelFactory);
    initVictim(config, managerFactory);
  }
  
  private void initVictim(final WroConfiguration config, final WroManagerFactory managerFactory) {
    Context.set(Context.standaloneContext(), config);
    final Injector injector = InjectorBuilder.create(managerFactory).build();
    injector.inject(victim);
  }
  
  @Test
  public void shouldReturnEmptyStringWhenGroupHasNoResources() {
    final CacheEntry key = new CacheEntry(groupName, ResourceType.JS, true);
    Assert.assertEquals(StringUtils.EMPTY, victim.process(key));
  }
  
  /**
   * Same as above, but with ignoreEmptyGroup config updated.
   */
  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenGroupHasNoResourcesAndIgnoreEmptyGroupIsFalse() {
    WroConfiguration config = new WroConfiguration();
    config.setIgnoreEmptyGroup(false);
    initVictim(config);
    final CacheEntry key = new CacheEntry("group", ResourceType.JS, true);
    victim.process(key);
  }
  
  @Test
  public void shouldLeaveContentUnchangedWhenAProcessorFails() {
    final CacheEntry key = new CacheEntry(groupName, ResourceType.JS, true);
    final Group group = new Group(groupName).addResource(Resource.create("1.js")).addResource(Resource.create("2.js"));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(new WroModel().addGroup(group));
    // the locator which returns the name of the resource as its content
    final UriLocatorFactory locatorFactory = new SimpleUriLocatorFactory().addUriLocator(createUrlMockingLocator());
    
    final ResourcePreProcessor failingPreProcessor = createFailingProcessor();
    final ProcessorsFactory processorsFactory = new SimpleProcessorsFactory().addPreProcessor(failingPreProcessor).addPostProcessor(
        new ProcessorDecorator(failingPreProcessor));
    final BaseWroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        locatorFactory);
    managerFactory.setProcessorsFactory(processorsFactory);
    
    final WroConfiguration config = new WroConfiguration();
    config.setIgnoreFailingProcessor(true);
    initVictim(config, managerFactory);
    
    final String actual = victim.process(key);
    Assert.assertEquals("1.js2.js", actual);
  }

  /**
   * @return a processor which fails during processing.
   */
  private ResourcePreProcessor createFailingProcessor() {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        throw new IOException("BOOM!");
      }
    };
  }
  
  private ResourcePreProcessor createSlowProcessor() {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        try {
          Thread.sleep(20);
          IOUtils.copy(reader, writer);
          Thread.sleep(30);
        } catch (Exception e) {
          throw new WroRuntimeException("Unxpected", e);
        }
      }
    };
  }
  
  @Test
  public void test() throws Exception {
    final CacheEntry jsKey = new CacheEntry(groupName, ResourceType.JS, true);
    final CacheEntry cssKey = new CacheEntry(groupName, ResourceType.CSS, true);
    final Group group = new Group(groupName).addResource(Resource.create("1.js")).addResource(Resource.create("2.js")).addResource(
        Resource.create("1.css")).addResource(Resource.create("2.css")).addResource(Resource.create("3.css")).addResource(
        Resource.create("4.css")).addResource(Resource.create("5.css")).addResource(Resource.create("3.js")).addResource(Resource.create("4.js"));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(new WroModel().addGroup(group));
    // the locator which returns the name of the resource as its content
    final UriLocatorFactory locatorFactory = new SimpleUriLocatorFactory().addUriLocator(createUrlMockingLocator());
    
    
    final ProcessorsFactory processorsFactory = new SimpleProcessorsFactory().addPreProcessor(createSlowProcessor()).addPreProcessor(
        createSlowProcessor()).addPreProcessor(createSlowProcessor()).addPostProcessor(
        new ProcessorDecorator(createSlowProcessor())).addPostProcessor(new ProcessorDecorator(createSlowProcessor()));
    final BaseWroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        locatorFactory);
    managerFactory.setProcessorsFactory(processorsFactory);
    
    initVictim(new WroConfiguration(), managerFactory);
    //new ContextPropagatingCallable<Void>(
    WroTestUtils.runConcurrently(new Callable<Void>() {
      public Void call()
          throws Exception {
        Context.set(Context.standaloneContext());
        String actual = null;
        if (new Random().nextBoolean()) {
          actual = victim.process(jsKey);
          Assert.assertEquals("1.js2.js3.js4.js", actual);
        } else {
          actual = victim.process(cssKey);
          Assert.assertEquals("1.css2.css3.css4.css5.css", actual);
        }
        Context.unset();
        return null;
      }
    }, 10);
  }

  /**
   * @return a locator which returns the resource uri as the content.
   */
  private UriLocator createUrlMockingLocator() {
    return new UriLocator() {
      public boolean accept(final String uri) {
        return true;
      }
      public InputStream locate(final String uri)
          throws IOException {
        return new ByteArrayInputStream(uri.getBytes());
      }
    };
  }
}
