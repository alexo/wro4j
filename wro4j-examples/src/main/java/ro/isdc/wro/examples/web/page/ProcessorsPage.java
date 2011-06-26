package ro.isdc.wro.examples.web.page;


import org.apache.wicket.request.mapper.parameter.PageParameters;

import ro.isdc.wro.examples.web.panel.ResourceTransformerPanel;


/**
 * Homepage
 */
public class ProcessorsPage extends AbstractTemplatePage {
  private static final long serialVersionUID = 1L;
  /**
   * Constructor that is invoked when page is invoked without a session.
   *
   * @param parameters Page parameters
   */
  public ProcessorsPage(final PageParameters parameters) {
    super(parameters);
    add(new ResourceTransformerPanel("resourceTransformer"));
  }
}
