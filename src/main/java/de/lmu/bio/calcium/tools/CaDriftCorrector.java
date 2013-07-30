package de.lmu.bio.calcium.tools;

import de.lmu.bio.calcium.utils.CaImageUtils;
import de.lmu.bio.calcium.CaTask;
import de.lmu.bio.calcium.model.CaImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class CaDriftCorrector extends CaTask {

    CaImage img;
    double tol = 20.0;
    DefaultTreeModel model;

    public CaDriftCorrector(DefaultTreeModel _model, CaImage _img) {
        super("Correcting drift");
        img = _img;
        model = _model;
    }

    public void correctIntraDrift(ImagePlus imp) {
        ImageStack stack = imp.getImageStack();
        int nslices = imp.getNSlices();

        ImageProcessor ip1 = stack.getProcessor(1);
        Polygon p1 = CaImageUtils.findInlayPolygon(ip1);
        for (int i = 1; i < (nslices - 1); i++) {

            ImageProcessor ip2 = stack.getProcessor(i);
            Point shift = CaImageUtils.estimateDrift(ip1, ip2, tol, p1, null);
            ip2.translate(shift.x, shift.y);
        }
    }

    @Override
    public void runTask() throws Exception {
        ImagePlus imp = img.openImage();
        correctIntraDrift(imp);

        Point shift = CaImageUtils.estimateDriftMax(imp, 20, tol);
        img.setDrift(shift);

        //imp.close();
        model.nodeChanged(img);
    }

}
