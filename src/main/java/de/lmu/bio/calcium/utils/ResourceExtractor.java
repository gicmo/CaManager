package de.lmu.bio.calcium.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


public class ResourceExtractor {
    public ResourceExtractor(String resourceName) {
        File tmp = new File (resourceName);
        fileName = tmp.getName();
        path = resourceName;
    }

    private String path;
    private String fileName;
    private File resourceExtracted;

    private File getFileForExtracted() {

        if (resourceExtracted == null) {
            Class<?> clazz = getClass();
            URL r = clazz.getProtectionDomain().getCodeSource().getLocation();
            File sourceLocation = new File(r.getPath());
            File parentDir = sourceLocation.getParentFile();
            resourceExtracted = new File(parentDir, fileName);
        }

        return resourceExtracted;
    }

    public boolean exists() {
        File fe = getFileForExtracted();
        return fe.exists();
    }

    public boolean extract() {
       Class<?> clazz = getClass();

       File dest = getFileForExtracted();
        if (dest.exists()) {
            return false;
        }

        InputStream is = clazz.getResourceAsStream(path);
        if (is == null) {
            return false;
        }

        try {
            OutputStream os = FileUtils.openOutputStream(dest);
            IOUtils.copy(is, os);
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public String getPath() {
        File fe = getFileForExtracted();
        return fe.getAbsolutePath();
    }


}
