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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ro.isdc.wro.examples.api.service.ProcessorService;


/**
 * @author Alex Objelean
 */
@Controller
@RequestMapping("/test")
public class ProcessResource {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessResource.class);
  @Autowired
  private ProcessorService processorService;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  // js_code or code_url
  public String process(@RequestParam(value = "code_url") final String codeUrl) throws IOException {
    return processorService.process(codeUrl);
  }

  @ExceptionHandler(value=Throwable.class)
  @ResponseBody
  public String handleException(final Throwable e) {
    LOG.debug("exception: " + e);
    return "Exception occured: " + e.getMessage();
  }
}
