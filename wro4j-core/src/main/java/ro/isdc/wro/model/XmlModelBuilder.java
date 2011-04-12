package ro.isdc.wro.model;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;

/**
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.7
 */
public class XmlModelBuilder extends ChangeAwareModelBuilder<Document> {

  /** Default xml to parse. */
  private static final String XML_SCHEMA_FILE = "ro/isdc/wro/wro.xsd";

  /** Document containing all group and resource definitions. */
  private Document modelDocument;

  /**
   * Creates a new {@link XmlModelBuilder} and sets the factory bound to this.
   *
   * @param factory Model factory bound to this builder. It cannot be null.
   */
  public XmlModelBuilder(final WroModelFactory factory) {
    super(factory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Document build() {
    return getDocument();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModelBuilder<Document> addGroup(final Group group) {
    Document documentRoot = getDocument();
    Element document = documentRoot.getDocumentElement();

    document.appendChild(createGroupEl(documentRoot, group));

    return super.addGroup(group);
  }

  /**
   * Reads document from {@link WroModelFactory#getConfigResourceAsStream()}.
   *
   * @return A document containing all groups and resources.
   */
  protected Document read() throws IOException {
    Document document = null;

    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory
          .newInstance();

      factory.setNamespaceAware(true);

      final InputStream configResource = getModelFactory()
          .getConfigResourceAsStream();

      if (configResource == null) {
        throw new WroRuntimeException(
            "Could not locate config resource (wro.xml)!");
      }

      document = factory.newDocumentBuilder().parse(configResource);
      validate(document);
      document.getDocumentElement().normalize();

    } catch(Exception ex) {
      throw new IOException("Cannot read the document.", ex);
    }

    return document;
  }

  /**
   * Returns the current {@link Document}. If there's no document loaded yet,
   * this method will read the model configuration file via {@link #read()}.
   *
   * @return A valid {@link Document}.
   */
  protected final Document getDocument() {
    try {
      if (modelDocument == null) {
        modelDocument = read();
      }

      return modelDocument;
    } catch(IOException ex) {
      throw new IllegalStateException("Cannot read XML document.", ex);
    }
  }

  /**
   * Checks if xml structure is valid.
   *
   * @param document xml document to validate.
   */
  private void validate(final Document document) throws IOException,
      SAXException {

    Schema schema = getSchema();
    // create a Validator instance, which can be used to validate an instance
    // document
    Validator validator = schema.newValidator();
    // validate the DOM tree
    validator.validate(new DOMSource(document));
  }

  /**
   * @return Schema
   */
  private Schema getSchema() throws IOException, SAXException {
    // create a SchemaFactory capable of understanding WXS schemas
    SchemaFactory factory = SchemaFactory.newInstance(
        XMLConstants.W3C_XML_SCHEMA_NS_URI);

    // load a WXS schema, represented by a Schema instance
    Source schemaFile = new StreamSource(Thread.currentThread()
        .getContextClassLoader().getResourceAsStream(XML_SCHEMA_FILE));
    Schema schema = factory.newSchema(schemaFile);

    return schema;
  }

  private Element createGroupEl(final Document document, final Group group) {
    Element element = document.createElement("group");
    element.setAttribute("name", group.getName());

    for (Resource resource : group.getResources()) {
      element.appendChild(createResourceEl(document, resource));
    }

    return element;
  }

  private Element createResourceEl(final Document document,
      final Resource resource) {

    Element element = document.createElement(resource.getType().name()
        .toLowerCase());

    element.setTextContent(resource.getUri());
    element.setAttribute("minimize", String.valueOf(resource.isMinimize()));

    return element;
  }
}
