package ro.isdc.wro.model.resource.locator.wildcard;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class OwnedJarEntry extends JarEntry {

    private JarFile jarFile;

    public OwnedJarEntry(JarEntry je, JarFile jarFile) {
        super(je);
        this.jarFile = jarFile;
    }

    public JarFile getJarFile() {
        return jarFile;
    }
}
