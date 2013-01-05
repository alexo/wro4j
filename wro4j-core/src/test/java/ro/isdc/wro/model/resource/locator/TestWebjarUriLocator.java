package ro.isdc.wro.model.resource.locator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestWebjarUriLocator {
  private UriLocator victim;

  @Before
  public void setUp() {
    victim = new WebjarUriLocator();
  }

  @Test
  public void shouldAcceptKnownUri() {
    assertTrue(victim.accept("webjar:/path/to/resource.js"));
  }

  @Test
  public void shouldNotAcceptUnknown() {
    assertFalse(victim.accept("http://www.server.com/path/to/resource.js"));
  }
}
