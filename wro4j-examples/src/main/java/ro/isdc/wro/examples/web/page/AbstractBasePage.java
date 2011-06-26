package ro.isdc.wro.examples.web.page;


import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Homepage
 */
public class AbstractBasePage extends WebPage implements IAjaxIndicatorAware {
  private static final long serialVersionUID = 1L;
  private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();
  /**
   * Constructor that is invoked when page is invoked without a session.
   *
   * @param parameters Page parameters
   */
  public AbstractBasePage(final PageParameters parameters) {
    add(indicatorAppender);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAjaxIndicatorMarkupId() {
    return indicatorAppender.getMarkupId();
  }
}
