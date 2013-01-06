package ro.isdc.wro.examples.support.locator;

import org.webjars.AssetLocator;

/**
 * @author Alex Objelean
 */
public class WebjarLocator {

  public static void main(final String[] args) {
//    System.out.println(AssetLocator.getFullPath("jquery/1.8.3/jquery.js"));
    System.out.println(AssetLocator.getFullPath("jquery.js"));
//    System.out.println(AssetLocator.getWebJarPath("jquery.js"));
  }
}
