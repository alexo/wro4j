/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.output.NullWriter;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter.FormatterType;
import ro.isdc.wro.extensions.support.lint.ResourceLintReport;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Maven plugin used to validate js scripts defined in wro model using <a href="http://jshint.com/">jsLint</a>.
 *
 * @goal jshint
 * @phase compile
 * @requiresDependencyResolution runtime
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHintMojo
    extends AbstractLinterMojo<LinterError> {
  /**
   * File where the report will be written.
   *
   * @parameter default-value="${project.build.directory}/wro4j-reports/jshint.xml" expression="${reportFile}"
   * @optional
   */
  private File reportFile;
  /**
   * The preferred format of the report.
   *
   * @parameter expression="${reportFormat}"
   * @optional
   */
  private String reportFormat = FormatterType.JSLINT.getFormat();
  /**
   * Allows mixed tabs and whitespace without reporting them
   * 
   * @parameter expression=${messy}
   * @optional
   */
  private boolean messy = false;
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onBeforeExecute() {
	  getLog().info("messy: " + messy);
	  super.onBeforeExecute();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createResourceProcessor() {
    final ResourcePreProcessor processor = new JsHintProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        getProgressIndicator().onProcessingResource(resource);
        // use StringWriter to discard the merged processed result (linting is useful only for reporting errors).
        super.process(resource, reader, new NullWriter());
      }

      @Override
      protected void onException(final WroRuntimeException e) {
        JsHintMojo.this.onException(e);
      }

      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
    	  Collection<LinterError> errors;
      	if (messy) {
      		errors = removeMixedWhitespaceErrors(e);
      	} else {
      		errors = e.getErrors();
      	}
      	
      	if (errors.size()> 0) {
	        final String errorMessage = String.format("%s errors found while processing resource: %s. Errors are: %s", e
	            .getErrors().size(), resource, errors);
	        getProgressIndicator().addFoundErrors(errors.size());
	        getLog().error(errorMessage);
	        // collect found errors
	        addReport(ResourceLintReport.create(resource.getUri(), errors));
	        if (isFailAllowed()) {
	          throw new WroRuntimeException("Errors found when validating resource: " + resource);
        }
      	}
      };
    }.setOptionsAsString(getOptions());
    return processor;
  }
  
  private Collection<LinterError> removeMixedWhitespaceErrors(LinterException e) {
	  Collection<LinterError> errors = e.getErrors();
	  Iterator<LinterError> i = errors.iterator();
	  
	  while (i.hasNext()) {
		  LinterError err = i.next();
		  if (err.getReason().equals("Mixed spaces and tabs.")) {
			  i.remove();
		  }
	  }
	  
	  return errors;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ReportXmlFormatter createXmlFormatter(final LintReport<LinterError> lintReport, final FormatterType type) {
    return ReportXmlFormatter.createForLinterError(lintReport, type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected File getReportFile() {
    return reportFile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFormat() {
    return reportFormat;
  }

  /**
   * @VisibleForTesting
   */
  void setReportFile(final File reportFile) {
    this.reportFile = reportFile;
  }

  /**
   * @param reportFormat
   *          the preferred report format.
   * @VisibleForTesting
   */
  void setReportFormat(final String reportFormat) {
    this.reportFormat = reportFormat;
  }

  /**
   * Used by unit test to check if mojo doesn't fail.
   *
   * @VisibleForTesting
   */
  @Override
  void onException(final Exception e) {
  }
}
