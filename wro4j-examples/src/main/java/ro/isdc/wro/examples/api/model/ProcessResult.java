/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.api.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Contains processing result details.
 *
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class ProcessResult
  implements Serializable {
  /**
   * Original size in bytes.
   */
  private int inputSize;
  /**
   * The result of processing size.
   */
  private int outputSize;
  /**
   * Resource retrieving duration in milliseconds.
   */
  private long retrieveTime;
  /**
   * Processing duration in milliseconds.
   */
  private long processTime;
  /**
   * The status of the processing. By default it is OK.
   */
  private StatusCode statusCode = StatusCode.OK;


  /**
   * @return the originalSize
   */
  public int getInputSize() {
    return this.inputSize;
  }


  /**
   * @param inputSize the originalSize to set
   */
  public void setInputSize(final int inputSize) {
    this.inputSize = inputSize;
  }


  /**
   * @return the compressedSize
   */
  public int getOutputSize() {
    return this.outputSize;
  }


  /**
   * @param outputSize the compressedSize to set
   */
  public void setOutputSize(final int outputSize) {
    this.outputSize = outputSize;
  }


  /**
   * @return the processTime
   */
  public long getProcessTime() {
    return this.processTime;
  }


  /**
   * @param processTime the processTime to set
   */
  public void setProcessTime(final long processTime) {
    this.processTime = processTime;
  }


  /**
   * @return the statusCode
   */
  public StatusCode getStatusCode() {
    return this.statusCode;
  }


  /**
   * @param statusCode the statusCode to set
   */
  public void setStatusCode(final StatusCode statusCode) {
    this.statusCode = statusCode;
  }


  /**
   * @return the retrieveTime
   */
  public long getRetrieveTime() {
    return this.retrieveTime;
  }


  /**
   * @param retrieveTime the retrieveTime to set
   */
  public void setRetrieveTime(final long retrieveTime) {
    this.retrieveTime = retrieveTime;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
