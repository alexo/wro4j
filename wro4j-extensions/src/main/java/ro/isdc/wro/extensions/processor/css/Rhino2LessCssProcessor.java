/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.SoftReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lesscss.LessCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.less.LessCss;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Rhino based processor using the <a href="https://github.com/marceloverdijk/lesscss-java"org.lescss</a> open source
 * library.
 * 
 * @author Alex Objelean
 * @created 1 Oct 2012
 */
@SupportedResourceType(ResourceType.CSS)
public class Rhino2LessCssProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(Rhino2LessCssProcessor.class);

  public static final String ALIAS = "rhino2LessCss";

  private static SoftReference<LessCompiler> lessCompilerReference = new SoftReference<LessCompiler>(null);

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    final LessCompiler lessCompiler = newLessCompiler();
    try {
      writer.write(lessCompiler.compile(content));
    } catch (final Exception e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
      onException(e);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final Exception e) {
    throw WroRuntimeException.wrap(e);
  }

  /**
   * @return the {@link LessCss} engine implementation. Override it to provide a different version of the less.js
   *         library. Useful for upgrading the processor outside the wro4j release.
   */
  protected synchronized LessCompiler newLessCompiler() {
      LessCompiler lessCompiler = lessCompilerReference.get();
      if (lessCompiler == null) {
              lessCompiler = new LessCompiler();
              lessCompiler.setCompress(true);
              lessCompiler.setEncoding("UTF-8");
              lessCompilerReference = new SoftReference<LessCompiler>(lessCompiler);
      }
      return lessCompiler;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

}
