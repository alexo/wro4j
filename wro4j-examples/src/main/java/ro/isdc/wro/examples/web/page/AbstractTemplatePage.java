package ro.isdc.wro.examples.web.page;


import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Homepage
 */
public class AbstractTemplatePage extends AbstractBasePage implements IAjaxIndicatorAware {
  private static final long serialVersionUID = 1L;
  private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();
  /**
   * Constructor that is invoked when page is invoked without a session.
   *
   * @param parameters Page parameters
   */
  public AbstractTemplatePage(final PageParameters parameters) {
    super(parameters);
    add(newSidebar("sidebar"));
    add(indicatorAppender);
  }

  private Component newSidebar(final String id) {
    //return new TwitterBar(id);
    return new EmptyPanel(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAjaxIndicatorMarkupId() {
    return indicatorAppender.getMarkupId();
  }
}
