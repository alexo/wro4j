package ro.isdc.wro.extensions.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webjars.WebJarAssetLocator;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Locator responsible for locating webjar resources. A webjar resource is a classpath resource respecting a certain
 * standard. <a href="http://www.webjars.org/">Read more</a> about webjars.
 * <p/>
 * This locator uses the following prefix to identify a locator capable of handling webjar resources:
 * <code>webjar:</code>
 *
 * @author Alex Objelean
 * @created 6 Jan 2013
 * @since 1.6.2
 */
public class WebjarUriLocator
        implements UriLocator {
    private static final Logger LOG = LoggerFactory.getLogger(WebjarUriLocator.class);
    /**
     * Alias used to register this locator with {@link LocatorProvider}.
     */
    public static final String ALIAS = "webjar";
    /**
     * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
     */
    public static final String PREFIX = format("%s:", ALIAS);
    private final UriLocator classpathLocator = new ClasspathUriLocator();
    private final List<ClassLoader> webJarClassLoaders = new LinkedList<ClassLoader>();
    private WebJarAssetLocator webjarAssetLocator;

    /**
     * Constructor, initializes with default current threads classLoader
     */
    public WebjarUriLocator() {
        this.addClassLoader(Thread.currentThread().getContextClassLoader());
    }


    /**
     * @return an instance of {@link WebJarAssetLocator} to be used for identifying the fully qualified name of resources
     * based on provided partial path.
     */
    private WebJarAssetLocator newWebJarAssetLocator() {
        return new WebJarAssetLocator(WebJarAssetLocator.getFullPathIndex(
                Pattern.compile(".*"), webJarClassLoaders.toArray(new ClassLoader[webJarClassLoaders.size()])));
    }

    /**
     * Adds a new instance of a {@link ClassLoader} that is used to find WebJars
     *
     * @param cl the classLoader to add
     */
    public WebjarUriLocator addClassLoader(ClassLoader cl) {
        webJarClassLoaders.add(cl);
        return this;
    }

    /**
     * @return the uri which is acceptable by this locator.
     */
    public static String createUri(final String path) {
        notNull(path);
        return PREFIX + path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream locate(final String uri)
            throws IOException {
        LOG.debug("locating: {}", uri);
        try {

            if (null == this.webjarAssetLocator) {
                this.webjarAssetLocator = newWebJarAssetLocator();
            }

            final String fullpath = webjarAssetLocator.getFullPath(extractPath(uri));
            return classpathLocator.locate(ClasspathUriLocator.createUri(fullpath));
        } catch (final Exception e) {
            throw new IOException("No webjar with uri: " + uri + " available.", e);
        }
    }

    /**
     * Replaces the protocol specific prefix and removes the query path if it exist, since it should not be accepted.
     */
    private String extractPath(final String uri) {
        return DefaultWildcardStreamLocator.stripQueryPath(uri.replace(PREFIX, ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final String uri) {
        return uri.trim().startsWith(PREFIX);
    }
}
