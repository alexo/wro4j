/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;


/**
 * Factory responsible for creation of {@link WroModel} object.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface WroModelFactory {
  /**
   * Creates a {@link WroModel} object. The concrete implementation must synchronize the instantiation of the model.
   *
   * @return an instance of {@link WroModel}.
   * @throws WroRuntimeException if model cannot be created.
   */
  WroModel getInstance();

  /**
   * Called to indicate that the factory is being taken out of service.
   */
  void destroy();

  void onModelChanged();

  /**
   * Returns an {@link InputStream} to read the model from. It must be a valid
   * {@link InputStream}. Consumer is responsible for closing this
   * {@link InputStream}.
   *
   * @return A valid {@link InputStream} containing group & resource definition.
   * @throws IOException if the stream couldn't be read.
   */
  InputStream getConfigResourceAsStream() throws IOException;
}
