/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author Alex Objelean
 */
@Controller
@RequestMapping("/test")
public class WroController {

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public String processSubmit() {
    return "Hello world";
  }
}
