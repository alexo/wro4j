package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;


/**
 * Resolves classpath resources looking for wildcard patterns in both file system and in JAR files.
 * <p>
 * The {@link #locateStream(String, File)} overrides the default strategy defined in
 * {@link DefaultWildcardStreamLocator} and it tries to open the provided file as a JAR. If that's successfully opened
 * all entries inside this container will be verified against the wildcard pattern. If the JAR-lookup strategy fails,
 * default strategy is invoked.
 * </p>
 * <p>
 * For the moment this {@link WildcardStreamLocator} only supports a single wildcard.
 * </p>
 *
 * @author Matias Mirabelli <matias.mirabelli@globant.com>
 * @since 1.3.6
 */
public class JarWildcardStreamLocator
    extends DefaultWildcardStreamLocator {
  private static final Logger LOG = LoggerFactory.getLogger(JarWildcardStreamLocator.class);
  /**
   * A {@link List} of file extensions including the final dot. Valid examples are: .jar, .war. By default it only
   * supports .jar extension.
   */
  private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".jar");

  /**
   * Finds the specified URI pattern inside a JAR file. If the specified file isn't a valid JAR default strategy will be
   * used instead.
   */
  @Override
  public InputStream locateStream(final String uri, final File folder)
      throws IOException {
    Validate.notNull(folder);
    final List<File> jarPaths = getJarFiles(folder);
    if (isSupported(jarPaths)) {
      return locateStreamFromJar(uri, jarPaths);
    }
    return super.locateStream(uri, folder);
  }

  /**
   * @return true if the file is of a certain supported type.
   */
  private boolean isSupported(final List<File> jarPath) {
    for (final File jar : jarPath) {
      boolean pass = false;
      for (final String supportedExtension : SUPPORTED_EXTENSIONS) {
        if (jar.getPath().endsWith(supportedExtension)) {
          pass = true;
          break;
        }
      }
      if (!pass) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return the File corresponding to the folder from inside the jar.
   * @VisibleForTestOnly
   */
  List<File> getJarFiles(final File folder) {
    final List<File> jars = new ArrayList<File>();
    if (folder instanceof MultiFile) {
      for (final File file : ((MultiFile) folder).getFiles()) {
        jars.add(new File(StringUtils.substringAfter(StringUtils.substringBeforeLast(file.getPath(), "!"), "file:")));
      }
    } else {
      jars.add(new File(StringUtils.substringAfter(StringUtils.substringBeforeLast(folder.getPath(), "!"), "file:")));
    }
    return jars;
  }

  /**
   * Validates an entry against a wildcard and determines whether the pattern matches or not. If the entry is accepted
   * this will be included in the result {@link java.io.InputStream}.
   *
   * @param entryName
   *          Entry to evaluate. It cannot be null.
   * @param wildcard
   *          Wildcard to match. It cannot be null or empty.
   * @return <code>true</code> if the expression matches, <code>false</code> otherwise.
   */
  private boolean accept(final String entryName, final String wildcard) {
    return FilenameUtils.wildcardMatch(entryName, wildcard);
  }

  /**
   * Opens the specified JAR file and returns a valid handle.
   *
   * @param jarFile
   *          Location of the valid JAR file to read. It cannot be null.
   * @return A valid {@link java.util.jar.JarFile} to read resources.
   * @throws IllegalArgumentException
   *           If the file cannot be opened because an {@link java.io.IOException}.
   * @VisibleForTestOnly
   */
  JarFile open(final File jarFile)
      throws IOException {
    Validate.isTrue(jarFile.exists(), "The JAR file must exists.");
    return new JarFile(jarFile);
  }

  /**
   * Finds the specified wildcard-URI resource(s) inside a JAR file and returns an {@link java.io.InputStream} to read a
   * bundle of matching resources.
   *
   * @param uri
   *          Resource(s) URI to match. It cannot be null or empty.
   * @param jarPaths
   *          A valid JAR file. It cannot be null.
   * @return A valid {@link java.io.InputStream} to read the bundle. Clients are responsible of closing this
   *         {@link java.io.InputStream}
   * @throws IOException
   *           If there's any error reading the JAR file.
   */
  private InputStream locateStreamFromJar(final String uri, final List<File> jarPaths)
      throws IOException {
    LOG.debug("Locating stream from jar: {}", jarPaths);
    //ok to get only first file because its not used later
    final WildcardContext wildcardContext = new WildcardContext(uri, jarPaths.get(0));
    String classPath = FilenameUtils.getPath(uri);

    if (classPath.startsWith(ClasspathUriLocator.PREFIX)) {
      classPath = StringUtils.substringAfter(classPath, ClasspathUriLocator.PREFIX);
    }

    final List<OwnedJarEntry> filteredJarEntryList = new ArrayList<OwnedJarEntry>();
    final List<File> allFiles = new ArrayList<File>();
    for (final File path : jarPaths) {
      final JarFile file = open(path);
      final List<JarEntry> jarEntryList = Collections.list(file.entries());
      for (final JarEntry entry : jarEntryList) {
        final String entryName = entry.getName();
        // ignore the parent folder itself and accept only child resources
        final boolean isSupportedEntry = entryName.startsWith(classPath) && !entryName.equals(classPath)
            && accept(entryName, wildcardContext.getWildcard());
        if (isSupportedEntry) {
          allFiles.add(new File(entryName));
          LOG.debug("\tfound jar entry: {}", entryName);
          filteredJarEntryList.add(new OwnedJarEntry(entry, file));
        }
      }
    }

    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    triggerWildcardExpander(allFiles, wildcardContext);
    for (final OwnedJarEntry entry : filteredJarEntryList) {
      final InputStream is = entry.getJarFile().getInputStream(entry);
      if (is != null) {
        IOUtils.copy(is, out);
        is.close();
      }
    }
    return new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
  }
}
