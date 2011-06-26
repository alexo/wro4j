/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ro.isdc.wro.examples.api.model.ProcessResult;
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
import ro.isdc.wro.util.StopWatch;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.javascript.jscomp.CompilationLevel;


/**
 * @author Alex Objelean
 */
@Service
public class ProcessorService {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessorService.class);
  private Map<String, ResourcePostProcessor> map = Maps.newHashMap();
  /**
   * Default processor name.
   */
  private static final String JS_MIN = "jsMin";


  public ProcessorService() {
    map.put(JS_MIN, new JSMinProcessor());
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
   * @return a collection of supported processors.
   */
  public Collection<String> supportedProcessors() {
    return map.keySet();
  }


  /**
   * Process an external url with JsMin processor.
   *
   * @param codeUrl the external url of the resource.
   * @return processed content.
   * @throws IOException if the url is not valid.
   */
  public String process(final String codeUrl)
    throws IOException {
    return processCode(getResourceContent(codeUrl), getProcessorByName(JS_MIN));
  }


  /**
   * Process a resource form external url with specified processor.
   *
   * @param codeUrl the external url of the resource.
   * @param processorName the name of the processor to use for resource compression.
   * @return processed content.
   */
  public String process(final String codeUrl, final String processorName)
    throws IOException {
    return processCode(getResourceContent(codeUrl), getProcessorByName(processorName));
  }


  public ProcessResult processStat(final String codeUrl)
    throws IOException {
    return createStat(codeUrl, getProcessorByName(JS_MIN));
  }


  public ProcessResult processStat(final String codeUrl, final String processorName)
    throws IOException {
    LOG.debug("codeUrl: " + codeUrl);
    LOG.debug("processorName: " + processorName);
    return createStat(codeUrl, getProcessorByName(processorName));
  }

  private ProcessResult createStat(final String codeUrl, final ResourcePostProcessor processor)
    throws IOException {
    LOG.debug("processStat: " + codeUrl + " processor: " + processor.getClass());
    final ProcessResult result = new ProcessResult();
    final StopWatch watch = new StopWatch();

    watch.start("retrieve");
    final String input = getResourceContent(codeUrl);
    watch.stop();

    result.setRetrieveTime(watch.getLastTaskTimeMillis());

    watch.start("processInfo: " + codeUrl);
    final String output = processCode(input, processor);
    watch.stop();

    result.setInputSize(input.length());
    result.setOutputSize(output.length());
    result.setProcessTime(watch.getLastTaskTimeMillis());
    LOG.debug("result: " + result);
    return result;
  }


  private ResourcePostProcessor getProcessorByName(final String processorName) {
    final ResourcePostProcessor processor = map.get(processorName);
    if (processor == null) {
      throw new RuntimeException("Invalid processorName: " + processorName + ". Available processors: " + map.keySet());
    }
    return processor;
  }


  private String getResourceContent(final String codeUrl)
    throws IOException {
    final UriLocator resourceLocator = new UrlUriLocator();
    final InputStream is = resourceLocator.locate(codeUrl);
    return IOUtils.toString(is);
  }


  /**
   * @param code
   * @return
   * @throws IOException
   */
  private String processCode(final String code, final ResourcePostProcessor processor)
    throws IOException {
    Assert.notNull(code);
    Assert.notNull(processor);
    final Writer writer = new StringWriter();
    processor.process(new StringReader(code), writer);
    return writer.toString();
  }
}
