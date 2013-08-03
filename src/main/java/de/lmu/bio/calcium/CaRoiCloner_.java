package de.lmu.bio.calcium;

import de.lmu.bio.calcium.tools.CaRoiCloner;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

import java.awt.*;

//http://rsbweb.nih.gov/ij/developer/api/ij/plugin/filter/ExtendedPlugInFilter.html
public class CaRoiCloner_ implements ExtendedPlugInFilter {

    String name;
    ImagePlus imp;
    Point shift;
    int x;
    int y;

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL+ROI_REQUIRED+NO_CHANGES;
    }

    @Override
    public void run(ImageProcessor ip) {
        Roi source = imp.getRoi();
        Roi target = CaRoiCloner.cloneMove(source, shift);
        target.setName(name);
        imp.setRoi(target, true);
    }

    @Override
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {

        GenericDialog gd = new GenericDialog("Clone ROI");

        gd.addNumericField("x",   0.0, 0);
        gd.addNumericField("y", -10.0, 0);
        gd.addStringField("Name", "BG");
        gd.showDialog();

        if (gd.wasCanceled())
            return DONE;

        int x = (int) gd.getNextNumber();
        int y = (int) gd.getNextNumber();

        shift = new Point(x, y);
        name = gd.getNextString();

        return DOES_ALL+ROI_REQUIRED+NO_CHANGES;
    }

    @Override
    public void setNPasses(int _nPasses) {

    }
}
