package de.lmu.bio.calcium.tools;

import de.lmu.bio.calcium.utils.CaImageUtils;
import de.lmu.bio.calcium.CaTask;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import ij.ImagePlus;

import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;


public class CaDriftEstimator extends CaTask {

    DefaultTreeModel treeModel;
    double tol = 20.0;
    int stepSize = 20;
    CaImage forImage = null;

    public CaDriftEstimator(DefaultTreeModel model) {
        super("Estimating drift");
        treeModel = model;
    }

    public CaDriftEstimator(DefaultTreeModel model, CaImage img) {
        super("Estimating drift");
        treeModel = model;
        forImage = img;
    }

    public void estimateDrift (ArrayList<CaImage> images) {
        int nimages = images.size();
        int curimg = 1;

        for (CaImage img : images) {
            estimateDrift(img);
            fireTaskProgress(curimg++, nimages, "Processing Image " + curimg);
        }
    }

    public void estimateDrift(CaImage img) {
        ImagePlus imp = img.openImage();
        Point maxShift = CaImageUtils.estimateDriftMax(imp, stepSize, tol);

        img.setDrift(maxShift);
        treeModel.nodeChanged(img);
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

        imgList = neuron.getImages(true);
        return imgList;
    }

    @Override
    public void runTask() throws Exception {
        if (forImage == null) {
            ArrayList<CaImage> images = getImages();
            estimateDrift(images);
        } else {
            fireTaskProgress(1, 2, "Processing Image");
            estimateDrift(forImage);
            fireTaskProgress(2, 2, "Processing Image");
        }   treeModel.nodeChanged(forImage);
    }

}
