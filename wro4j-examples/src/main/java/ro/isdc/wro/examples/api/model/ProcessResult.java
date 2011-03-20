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
  private int originalSize;
  /**
   * The result of processing size.
   */
  private int compressedSize;
  /**
   * Processing duration in milliseconds.
   */
  private int processTime;
  /**
   * Code resulted after the processing is applied.
   */
  private String processedCode;


  /**
   * @return the originalSize
   */
  public int getOriginalSize() {
    return this.originalSize;
  }


  /**
   * @param originalSize the originalSize to set
   */
  public void setOriginalSize(final int originalSize) {
    this.originalSize = originalSize;
  }


  /**
   * @return the compressedSize
   */
  public int getCompressedSize() {
    return this.compressedSize;
  }


  /**
   * @param compressedSize the compressedSize to set
   */
  public void setCompressedSize(final int compressedSize) {
    this.compressedSize = compressedSize;
  }


  /**
   * @return the processTime
   */
  public int getProcessTime() {
    return this.processTime;
  }


  /**
   * @param processTime the processTime to set
   */
  public void setProcessTime(final int processTime) {
    this.processTime = processTime;
  }


  /**
   * @return the processedCode
   */
  public String getProcessedCode() {
    return this.processedCode;
  }


  /**
   * @param processedCode the processedCode to set
   */
  public void setProcessedCode(final String processedCode) {
    this.processedCode = processedCode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
