/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.api.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.javascript.jscomp.CompilationLevel;

/**
 * @author Alex Objelean
 */
@Service
public class ProcessorService {
  private Map<String, ResourcePostProcessor> map = Maps.newHashMap();

  public ProcessorService() {
    map.put("jsMin", new JSMinProcessor());
    map.put("packerJs", new PackerJsProcessor());
    map.put("googleClosureSimple", new GoogleClosureCompressorProcessor());
    map.put("googleClosureAdvanced", new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));
    map.put("uglifyJs", new UglifyJsProcessor());
    map.put("beautifyJs", new BeautifyJsProcessor());
    map.put("dojoShrinksafe", new DojoShrinksafeCompressorProcessor());
    map.put("yuiJsMin", YUIJsCompressorProcessor.noMungeCompressor());
    map.put("yuiJsMinAdvanced", YUIJsCompressorProcessor.doMungeCompressor());
  }
  /**
   * Process an external url with JsMin processor.
   *
   * @param codeUrl the external url of the js.
   * @return processed content.
   * @throws IOException if the url is not valid.
   */
  public String process(final String codeUrl) throws IOException {
    return genericProcess(codeUrl, getProcessorByName("jsMin"));
  }

  public String process(final String codeUrl, final String processorName) throws IOException {
    return genericProcess(codeUrl, getProcessorByName(processorName));
  }

  /**
   * @param processorName
   * @return
   */
  private ResourcePostProcessor getProcessorByName(final String processorName) {
    final ResourcePostProcessor processor = map.get(processorName);
    if (processor == null) {
      throw new RuntimeException("Invalid processorName: " + processorName + ". Available processors: " + map.keySet());
    }
    return processor;
  }

  /**
   * @param codeUrl
   * @return
   * @throws IOException
   */
  protected String genericProcess(final String codeUrl, final ResourcePostProcessor processor)
    throws IOException {
    Assert.notNull(codeUrl);
    Assert.notNull(processor);
    final Writer writer = new StringWriter();
    final UriLocator resourceLocator = new UrlUriLocator();
    processor.process(new InputStreamReader(resourceLocator.locate(codeUrl)), writer);
    return writer.toString();
  }
}
