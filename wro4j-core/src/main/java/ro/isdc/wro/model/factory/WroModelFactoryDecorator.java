/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.WroModel;


/**
 * Decorates a {@link WroModelFactory}.
 *
 * @author Alex Objelean
 * @created 13 Mar 2011
 */
public class WroModelFactoryDecorator
  implements WroModelFactory {
  private WroModelFactory decorated;

  public WroModelFactoryDecorator(final WroModelFactory decorated) {
    if (decorated == null) {
      throw new IllegalArgumentException("Decorated WroModelFactory cannot be null!");
    }
    this.decorated = decorated;
  }
  /**
   * {@inheritDoc}
   */
  public WroModel getInstance() {
    return decorated.getInstance();
  }


  /**
   * {@inheritDoc}
   */
  public void destroy() {
    decorated.destroy();
  }

  /**
   * {@inheritDoc}
   */
  public void onModelChanged() {
    decorated.onModelChanged();
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getConfigResourceAsStream() throws IOException {
    return decorated.getConfigResourceAsStream();
  }
}
