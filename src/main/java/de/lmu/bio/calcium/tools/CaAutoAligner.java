package de.lmu.bio.calcium.tools;

import de.lmu.bio.calcium.utils.CaImageUtils;
import de.lmu.bio.calcium.CaTask;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.*;

public class CaAutoAligner extends CaTask {
    DefaultTreeModel treeModel;
    private boolean ascending = false;
    private double tol = 20.0;
    public int slice = 1;

    public static class AlignContext {
        public CaImage img;
        ImagePlus      imp;
        ImageProcessor ip;
        ImageStack     stack;
        Polygon        inlay;
        private int slice;


        public AlignContext(int _slice, CaImage _img) {
            img = _img;
            slice = _slice;
            openImage();
        }

        public void openImage() {
            imp = img.openImage();
            stack = imp.getImageStack();

            int thisSlice = slice;
            if (thisSlice > imp.getNSlices()) {
                IJ.showStatus("Warning. Not all image have same stack size");
                thisSlice = imp.getNSlices();
            }

            ip = stack.getProcessor(thisSlice);
        }

        public void findInlay() {
            inlay = CaImageUtils.findInlayPolygon(ip);
        }

    }

    public CaAutoAligner(DefaultTreeModel model, boolean descending, int targetSlice) {
        super("Aligning");
        treeModel = model;
        slice = targetSlice;
        ascending = !descending;
    }


    public void doAlignROIs() {
        ArrayList<CaImage> images = getImages();

        int nimages = images.size();

        if (nimages < 2) {
            return;
        }

        Iterator<CaImage> iter = images.iterator();

        int curimg = 1;
        fireTaskProgress(curimg, nimages, "Opening Image");

        AlignContext cur = new AlignContext(slice, iter.next());
        treeModel.nodeChanged(cur.img);//update slice count

        cur.findInlay();

        AlignContext ref;
        while (iter.hasNext()) {
            ref = cur;
            fireTaskProgress(curimg++, nimages, "Processing Image " + curimg);
            cur = new AlignContext(slice, iter.next());
            cur.findInlay();
            fireTaskProgress(curimg, nimages, "Calculating shift");
            Point shift = CaImageUtils.estimateDrift(ref.ip, cur.ip, tol, ref.inlay, cur.inlay);
            System.err.printf("shift: %d %d\n", shift.x, shift.y);
            cur.img.copyRois(ref.img, shift);
            treeModel.nodeStructureChanged(cur.img);

        }
    }

    public ArrayList<CaImage> getImages() {
        CaNeuron neuron;
        ArrayList<CaImage> imgList = new ArrayList<CaImage>();
        if (treeModel == null) {
            return imgList;
        }

        Object o;
        if ((o = treeModel.getRoot()) == null ||
                !(o instanceof CaNeuron)) {
            return imgList;
        }

        neuron = (CaNeuron) o;

        imgList = neuron.getImages(ascending);
        return imgList;
    }

    @Override
    public void runTask() throws Exception {
        doAlignROIs();
    }

}
