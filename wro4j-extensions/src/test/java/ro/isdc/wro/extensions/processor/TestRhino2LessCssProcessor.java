/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Test;
import org.lesscss.LessCompiler;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.Rhino2LessCssProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test less css processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestRhino2LessCssProcessor {
  @Test
  public void testFromFolder()
      throws Exception {
    final ResourcePostProcessor processor = new Rhino2LessCssProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }


  @Test
  public void executeMultipleTimesDoesntThrowOutOfMemoryException() throws Exception {
    final LessCompiler lessCss = new LessCompiler();
    for (int i = 0; i < 100; i++) {
      lessCss.compile("#id {.class {color: red;}}");
    }
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final ResourcePreProcessor processor = new Rhino2LessCssProcessor();
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          processor.process(null, new StringReader("#id {.class {color: red;}}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }

  /**
   * Test that processing invalid less css produces exceptions
   */
  @Test
  public void shouldFailWhenInvalidLessCssIsProcessed()
      throws Exception {
    final ResourcePreProcessor processor = new Rhino2LessCssProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "invalid");
    WroTestUtils.forEachFileInFolder(testFolder, new Function<File, Void>() {
      @Override
      public Void apply(final File input)
          throws Exception {
        try {
          processor.process(null, new FileReader(input), new StringWriter());
          Assert.fail("Expected to fail, but didn't");
        } catch (final WroRuntimeException e) {
          //expected to throw exception, continue
        }
        return null;
      }
    });
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new Rhino2LessCssProcessor(), ResourceType.CSS);
  }
}
