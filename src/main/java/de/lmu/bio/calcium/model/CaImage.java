package de.lmu.bio.calcium.model;

import de.lmu.bio.calcium.tools.CaRoiCloner;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.Opener;
import org.apache.commons.io.FilenameUtils;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;

public class CaImage extends CaTreeNode {

    private File file;
    private long mtime;

    private int trial;

    //live metadata
    private int nslices = -1;
    private Point drift = null;

    public CaImage(String path) {

        if (path.startsWith("file://")) {
            file = new File(path.substring(7));
        } else {
            file = new File(path);
        }

        mtime = file.lastModified();
        String filename = file.getName();
        setName(filename);

    }

    private void setName(String filename) {
        filename = stripExtension(filename);
        this.setUserObject(filename);
    }

    private static String stripExtension(String filename, int maxExLen) {
        int index = FilenameUtils.indexOfExtension(filename);
        if (index == -1 || (maxExLen > 0 && (filename.length() - (index+1)) > maxExLen)) {
            return filename;
        } else {
            String exe = filename.substring(index + 1);
            String name = filename.substring(0, index);
            if (exe.equalsIgnoreCase("tif") || exe.equalsIgnoreCase("tiff")) {
                name = stripExtension(name, 3);
            }

            return name;
        }
    }

    public static String stripExtension(String filename) {
        if (filename == null)
            return null;

        return stripExtension(filename, 5);
    }


    public String getFilePath() {
        return file.getAbsolutePath();
    }

    public File getFile() {
        return file;
    }

    public long getMTime() {
        return mtime;
    }

    public String getName() {
        return (String) getUserObject();
    }


    public int getNslices() {
        return nslices;
    }

    public void setNslices(int nslices) {
        this.nslices = nslices;
    }

    public Point getDrift() {
        return drift;
    }

    public void setDrift(Point drift) {
        this.drift = drift;
    }

    public long getCTime() {
        //FIXME (read from FS or image metadata)
        return mtime;
    }

    @Override
    public Object clone() {
        CaImage copy;
        copy = (CaImage)super.clone();
        copy.file = file;
        copy.mtime = mtime;

        return copy;
    }

    public Roi getRoi(String name) {

        CaRoiBox box = getRoiBox(name);
        if (box == null) {
            return null;
        }

        return box.getRoi();
    }

    public CaRoiBox getRoiBox(String name) {
        CaRoiBox result = null;

        if (children == null) {
            return null;
        }

        for (Object child : children) {
            if (!(child instanceof CaRoiBox)) {
                continue;
            }

            CaRoiBox box = (CaRoiBox) child;

            if (box.getName().equals(name)) {
                result = box;
                break;
            }

        }

        return result;
    }

    public int getRoiCount() {
        int count = 0;

        if (children == null) {
            return count;
        }

        for (Object child : children) {
            if (!(child instanceof CaRoiBox)) {
                continue;
            }

            count++;
        }

        return count;
    }

    private void setRoi(Roi value, String name) {
        CaRoiBox box = getRoiBox(name);

        if (value != null) {
            if (box == null) {
                box = new CaRoiBox(value);
                add(box);
            } else {
                box.setRoi(value);
            }
        } else {
            if (box != null) {
                remove(box);
            }
        }
    }

    public Roi getRoiFg() {
        return getRoi("FG");
    }

    public void setRoiFg(Roi value) {
        setRoi(value, "FG");
    }

    public Roi getRoiBg() {
        return getRoi("BG");
    }

    public void setRoiBg(Roi value) {
        setRoi(value, "BG");
    }

    public void copyRois(CaImage source) {
       copyRois(source, null);
    }

    public void copyRois(CaImage source, Point shift) {
        if (source == null || source.children == null) {
            return;
        }

        for (Object child : source.children) {
            if (!(child instanceof CaRoiBox)) {
                continue;
            }

            CaRoiBox box = (CaRoiBox) child;
            Roi dolly = cloneRoi(box.getRoi(), shift);
            setRoi(dolly, box.getName());
        }
    }

    protected Roi cloneRoi(Roi source, Point shift) {

        Roi p;

        if (shift != null) {
            p  = CaRoiCloner.cloneMove(source, shift);
        } else {
            p =  (Roi) source.clone();
        }

        return p;
    }

    public int getTrial() {
        return trial;
    }

    public void setTrial(int trial) {
        this.trial = trial;
    }


    @Override
    public String toString() {

        TreeNode root = this.getRoot();
        String name = super.toString();
        if (name != null && root != null && root instanceof CaNeuron) {
            CaNeuron neuron = (CaNeuron) root;
            String prefix = neuron.getCommonFilePrefix();
            if (prefix != null && name.startsWith(prefix)) {
                name = name.substring(prefix.length());
            }
        }

        return name;
    }

    public ImagePlus openImage() {
        Opener imageOpener = new Opener();
        imageOpener.setSilentMode(true);
        ImagePlus imp = imageOpener.openImage(getFilePath());
        nslices = imp.getNSlices();
        return imp;
    }

}
