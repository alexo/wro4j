package ro.isdc.wro.model.resource.locator.support;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestWebjarAssetLocator {
  private WebjarAssetLocator victim;
  @Before
  public void setUp(){
    victim = new WebjarAssetLocator();
  }

  @Test
  public void test() {
    victim.getFullPath("jshint/jshint.js");
  }
}
