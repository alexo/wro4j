package ro.isdc.wro.model.resource.locator.support;

import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;

/**
 * This class is responsible for computing the path of the webjar assets based on partial provided path. The
 * implementation is inspired from webjar-locator project.
 *
 * @author Alex Objelean
 * @created 6 Jan 2013
 * @since 1.6.2
 */
public class WebjarAssetLocator {
  private static final String[] WEBJARS_PATH_PREFIX = {"META-INF", "resources", "webjars"};
  private static final String PREFIX = ClasspathUriLocator.PREFIX + StringUtils.join(WEBJARS_PATH_PREFIX, '/');
  public String getFullPath(final String partialPath) {
    return null;
  }

  public static void main(final String[] args) {
    System.out.println(ClasspathUriLocator.PREFIX + StringUtils.join(WEBJARS_PATH_PREFIX, '/'));;
  }
}
