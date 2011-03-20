/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ro.isdc.wro.examples.api.service.ProcessorService;


/**
 * @author Alex Objelean
 */
@Controller
public class ProcessResource {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessResource.class);
  @Autowired
  private ProcessorService processorService;


  @RequestMapping(value = "/process", method = RequestMethod.POST)
  @ResponseBody
  public String process(@RequestParam(value = "code_url") final String codeUrl)
    throws IOException {
    return processorService.process(codeUrl);
  }


  @RequestMapping(value = "/process/{processorName}", method = RequestMethod.POST)
  @ResponseBody
  public String process(@PathVariable final String processorName, @RequestParam(value = "code_url") final String codeUrl)
    throws IOException {
    return processorService.process(codeUrl, processorName);
  }

  @RequestMapping(value = "/processStat", method = RequestMethod.POST)
  @ResponseBody
  public Object processStat(@RequestParam(value = "code_url") final String codeUrl)
    throws IOException {
    return processorService.processStat(codeUrl);
  }


  @ExceptionHandler(value = Throwable.class)
  @ResponseBody
  public String handleException(final Throwable e) {
    LOG.debug("exception: " + e);
    return "Exception occured: " + e.getMessage();
  }
}
