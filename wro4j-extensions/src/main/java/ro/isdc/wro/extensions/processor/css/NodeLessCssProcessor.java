/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;


/**
 * Important node: this processor is not cross platform and has some pre-requesites in order to work.
 * <p/>
 * Same as {@link RhinoLessCssProcessor} but uses <code>lessc</code> shell utility to process the less.
 * <p/>
 * Installation instructions: Install the libnode-less package (Unix OS)
 * 
 * <pre>
 *   sudo apt-get install libnode-less
 * </pre>
 * 
 * It is possible to test whether the lessc utility is available using {@link NodeLessCssProcessor#isSupported()}
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 10 Sep 2012
 */
@SupportedResourceType(ResourceType.CSS)
public class NodeLessCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, SupportAware {
  private static final String OPTION_NO_COLOR = "--no-color";
  private static final String SHELL_COMMAND = "lessc";
  private static final Logger LOG = LoggerFactory.getLogger(NodeLessCssProcessor.class);
  public static final String ALIAS = "nodeLessCss";
  /**
   * Flag indicating that we are running on Windows platform. This will be initialized only once in constructor.
   */
  private final boolean isWindows;
  
  public NodeLessCssProcessor() {
    // initialize this field at construction.
    final String osName = System.getProperty("os.name");
    LOG.debug("OS Name: {}", osName);
    isWindows = osName != null && osName.contains("Windows");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      final String resourceUri = resource == null ? "unknown.less" : resource.getUri();
      writer.write(process(resourceUri, content));
    } catch (final WroRuntimeException e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
      onException(e);
    } finally {
      // return for later reuse
      reader.close();
      writer.close();
    }
  }
  
  private String process(final String resourceUri, final String content) {
    InputStream shellIn = null;
    // the file holding the input file to process
    File temp = null;
    try {
      temp = createTempFile();
      final String encoding = "UTF-8";
      IOUtils.write(content, new FileOutputStream(temp), encoding);
      LOG.debug("absolute path: {}", temp.getAbsolutePath());
      final String tempFilePath = temp.getPath();
      final Process process = createProcess(tempFilePath);
      shellIn = process.getInputStream();
      /**
       * It is important to read before waitFor is invoked because read stream is blocking stdout while Java application
       * doesn't read the whole buffer. It hangs when processing large files. The lessc isn't closing till all STDOUT
       * flushed. This blocks io and Node does not exit because of that.
       */
      final String result = IOUtils.toString(shellIn, encoding);
      int exitStatus = process.waitFor();// this won't return till `out' stream being flushed!      
      
      if (exitStatus != 0) {
        LOG.error("exitStatus: {}", exitStatus);
        // find a way to get rid of escape character found at the end (minor issue)
        final String errorMessage = MessageFormat.format("Error in LESS: \n{0}",
            result.replace(tempFilePath, resourceUri));
        throw new WroRuntimeException(errorMessage);
      }
      return result;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new WroRuntimeException(e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(shellIn);
      // always cleanUp
      FileUtils.deleteQuietly(temp);
    }
  }
  
  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }
  
  /**
   * Creates process responsible for running lessc shell command by reading the file content from the sourceFilePath
   * 
   * @param sourceFilePath
   *          the source path of the file from where the lessc will read the less file.
   * @throws IOException
   *           when the process execution fails.
   */
  private Process createProcess(final String sourceFilePath)
      throws IOException {
    final String[] commandLine = getCommandLine(sourceFilePath);
    LOG.debug("commandLine arguments: {}", Arrays.asList(commandLine));
    final ProcessBuilder processBuilder = new ProcessBuilder(commandLine).redirectErrorStream(true);
    return processBuilder.start();
  }
  
  /**
   * @return true if the processor is supported on this environment. The implementation check if the required shell
   *         utility is available.
   */
  @Override
  public boolean isSupported() {
    try {
      new ProcessBuilder(getCommandLine("")).start();
      return true;
    } catch (Exception e) {
      LOG.warn("The {} processor is not supported.", getClass().getName());
      return false;
    }
  }
  
  /**
   * @return arguments for command line. The implementation will take care of OS differences.
   */
  private String[] getCommandLine(final String filePath) {
    return isWindows ? buildArgumentsForWindows(filePath) : buildArgumentsForNonWindows(filePath);
  }
  
  /**
   * @return arguments required to run lessc on non Windows platform.
   */
  private String[] buildArgumentsForNonWindows(final String filePath) {
    return new String[] {
      SHELL_COMMAND, OPTION_NO_COLOR, filePath
    };
  }
  
  /**
   * @return arguments required to run lessc on Windows platform.
   */
  private String[] buildArgumentsForWindows(final String filePath) {
    return new String[] {
      "cmd", "/c", SHELL_COMMAND, OPTION_NO_COLOR, filePath
    };
  }
  
  private File createTempFile() {
    return new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString() + ".less");
  }
}
