/**
 *
 */
package ro.isdc.wro.model.resource.locator;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.support.LocatorProvider;


/**
 * Locator responsible for locating webjar resources. A webjar resource is a classpath resource respecting a certain
 * standard. <a href="http://www.webjars.org/">Read more</a> about webjars.
 * <p/>
 * This locator uses the following prefix to identify a locator capable of handling webjar resources:
 * <code>webjar:</code>
 *
 * @author Alex Objelean
 * @created 6 Jan 2013
 * @since 1.6.2
 */
public class WebjarUriLocator
    implements UriLocator {
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "webjar";
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = format("%s:", ALIAS);
  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }
}
