package de.lmu.bio.calcium.ui;

import de.lmu.bio.calcium.CaSettings;

import java.awt.*;
import java.io.File;

public class CaDialogUtils {

    public static File[] getImageFiles(Frame parent) {
        FileDialog fd = new FileDialog(parent, "Select Image files", FileDialog.LOAD);
        return getImageFiles(fd);
    }

    public static File[] getImageFiles(Dialog parent) {
        FileDialog fd = new FileDialog(parent, "Select Image files", FileDialog.LOAD);
        return getImageFiles(fd);
    }

    private static File[] getImageFiles(FileDialog fd) {
        fd.setMultipleMode(true);
        fd.setDirectory(CaSettings.get().getDataDir().getAbsolutePath());
        fd.setFilenameFilter((dir, name) -> name.endsWith(".tiff") || name.endsWith(".tif"));
        fd.setVisible(true);
        return fd.getFiles();
    }

}
