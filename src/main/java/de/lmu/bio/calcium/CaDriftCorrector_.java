package de.lmu.bio.calcium;

import de.lmu.bio.calcium.utils.CaImageUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

import java.awt.*;

public class CaDriftCorrector_ implements ExtendedPlugInFilter {

    ImageProcessor ipref;
    ImagePlus imp;
    ImageStack stack;
    Polygon   pref;
    double tol = 20.0;
    boolean pairwise = true;

    int npasses;

    @Override
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        GenericDialog gd = new GenericDialog("Dirft correction");
        gd.addCheckbox("Pairwise", true);
        gd.showDialog();
        if (gd.wasCanceled())
            return DONE;
        pairwise = gd.getNextBoolean();
        System.err.printf("Do pairwise: %s\n", pairwise ? "Y" : "N");
        return DOES_ALL+DOES_STACKS;
    }

    @Override
    public void setNPasses(int nPasses) {
        npasses = nPasses;
    }

    @Override
    public int setup(String arg, ImagePlus _imp) {

        imp = _imp;
        stack = imp.getStack();

        System.err.printf("Setup\n");
        ipref = stack.getProcessor(1);
        pref = CaImageUtils.findInlayPolygon(ipref);

        return DOES_ALL+DOES_STACKS; //FIXME we lie
    }

    @Override
    public void run(ImageProcessor ip) {

        if (ip.getSliceNumber() == ipref.getSliceNumber()) {
            System.err.printf("%d == %d, skipping\n", ip.getSliceNumber(), ipref.getSliceNumber());
            return;
        }

        IJ.showProgress(ip.getSliceNumber(), imp.getNSlices());

        Point shift = CaImageUtils.estimateDrift(ipref, ip, tol, pref, null);
        System.err.printf("[%d:%d] shift: %d %d\n",
                ipref.getSliceNumber(), ip.getSliceNumber(), shift.x, shift.y);

        ip.translate(-shift.x, -shift.y);
        if (pairwise) {
            int slicenr = ip.getSliceNumber();
            ipref.setPixels(stack.getPixels(slicenr));
            ipref.setSliceNumber(slicenr);
            pref = CaImageUtils.findInlayPolygon(ipref);
            System.err.printf(" %d -> %d\n", slicenr, ipref.getSliceNumber());
        }


    }


}
