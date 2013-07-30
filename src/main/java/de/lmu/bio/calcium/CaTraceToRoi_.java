package de.lmu.bio.calcium;

import de.lmu.bio.calcium.utils.CaUtils;
import de.lmu.bio.calcium.ui.CaImageWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import tracing.*;

import java.awt.*;

public class CaTraceToRoi_ implements PlugInFilter {

    ImagePlus imp;

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL+ROI_REQUIRED+NO_CHANGES;
    }

    @Override
    public void run(ImageProcessor ip) {
        Roi roi = imp.getRoi();
        Polygon poly = roi.getPolygon();
        traceNeuron(poly);
    }

    //**
    public void traceNeuron(Polygon poly) {
        int end = poly.npoints - 1;

        int ax = poly.xpoints[0];
        int ay = poly.ypoints[0];
        int bx = poly.xpoints[end];
        int by = poly.ypoints[end];

        TracerThread tracer = new TracerThread (imp, 0, 255, 5*60, 300, ax, ay, 0, bx, by, 0, true, true, null, 1, null, false);
        System.out.println("Running tracer...: " + ax + " " + ay + "; " + bx + " " + by);
        tracer.addProgressListener(new SearchProgress());
        tracer.start();
    }

    //------------------------------------------------------------------------------------------------------------------

    public class SearchProgress implements SearchProgressCallback {

        @Override
        public void pointsInSearch(SearchInterface searchInterface, int i, int i1) {
            IJ.showStatus("Tracing neuron (" + i + ", " + i1 + ")");
        }

        @Override
        public void finished(SearchInterface tracer, boolean b) {
            //FIXME: take care of result

            if (b == false) {
                IJ.showStatus("Tracing neuron FAILED!");
                return;
            }
            IJ.showStatus("Tracing neuron finished successfully!");
            Path result = tracer.getResult();
            result.setName("Roi");
            Roi roi = CaUtils.pathToRoiDownsample(result, 1); //FIXME eps is hardcoded

            ImageWindow wnd = imp.getWindow();
            if (wnd instanceof CaImageWindow) {
                CaImageWindow caWnd = (CaImageWindow) wnd;
                caWnd.replaceCurrentRoi(roi);
            } else {
                imp.deleteRoi();
                imp.setRoi(roi);
            }


        }

        @Override
        public void threadStatus(SearchInterface searchInterface, int i) { }
    }
}
