package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * HACK to pass more then one file through usual File parameter.
 * Its required due to wildcard locator contract
 */
public class MultiFile extends File {
    private List<File> files;

    public MultiFile(List<URL> urls) {
        super(urls.get(0).getFile());
        files = new ArrayList<File>();
        for (URL url : urls) {
            files.add(new File(url.getFile()));
        }
    }

    public List<File> getFiles() {
        return files;
    }
}
