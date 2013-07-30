package de.lmu.bio.calcium.utils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.filter.MaximumFinder;
import ij.process.FHT;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.image.IndexColorModel;
import java.util.Arrays;
import java.util.Comparator;

public class CaImageUtils {

    public static Dimension calcImageSize(ImageProcessor a, ImageProcessor b) {
        int w = Math.min(a.getWidth(), b.getWidth());
        int h = Math.min(a.getHeight(), b.getHeight());

        return new Dimension(w, h);
    }


    public static Polygon findInlayPolygon(ImageProcessor ip) {

        MaximumFinder finder = new MaximumFinder();
        Polygon p = finder.getMaxima(ip, 20.0, false); //FIXME hardcoded value
        final Integer idx[] = CaAlgorithms.createRange(p.npoints);
        final float[] data = new float[p.npoints];

        for (int i = 0; i < p.npoints; i++) {
            data[i] = ip.getPixelValue(p.xpoints[i], p.ypoints[i]);
        }

        Arrays.sort(idx, new Comparator<Integer>() {
            @Override
            public int compare(final Integer a, final Integer b) {
                return -Float.compare(data[a], data[b]);
            }
        });

        Polygon result = new Polygon();
        final int max_points = Math.min(p.npoints, 2);
        for (int i = 0; i < max_points; i++) {
            int n = idx[i];
            result.addPoint(p.xpoints[n], p.ypoints[n]);
        }

        return result;
    }

    public static Point estimateDrift(ImageProcessor ref, ImageProcessor img, double tol,
                                      Polygon poiRef, Polygon poiImg)
    {
        if (poiRef == null) {
            poiRef = CaImageUtils.findInlayPolygon(ref);
        }

        if (poiImg == null) {
            poiImg = CaImageUtils.findInlayPolygon(img);
        }

        Polygon p = CaAlgorithms.mergePolygons(poiRef, poiImg);
        Dimension maxSize = CaImageUtils.calcImageSize(ref, img);
        Rectangle bounds = CaAlgorithms.createSquare(p, maxSize);
        ImageProcessor a = CaImageUtils.getCrop(ref, bounds);
        ImageProcessor b = CaImageUtils.getCrop(img, bounds);
        return CaImageUtils.calcShift(a, b, tol);
    }

    public static Point calcShift(ImageProcessor ref, ImageProcessor img, double tol) {
        FHT fht1 = new FHT(img);
        FHT fht2 = new FHT(ref);

        fht1.transform();
        fht2.transform();

        FHT result = fht1.conjugateMultiply(fht2);

        result.inverseTransform();
        result.swapQuadrants();
        result.resetMinAndMax();

        MaximumFinder finder = new MaximumFinder();
        Polygon maxima = finder.getMaxima(result, tol, true); //FIXME hardcoded value

        // Find the actual maximum
        int n = -1;
        float max_val = -1;
        for (int i = 0; i < maxima.npoints; i++) {
            int x =  maxima.xpoints[i];
            int y =  maxima.ypoints[i];
            float pv = result.getPixelValue(x, y);

            if (pv > max_val) {
                n = i;
                max_val = pv;
            }
        }

        int max_len = ref.getWidth();
        int delta = (int) (max_len/2.0);

        int x = maxima.xpoints[n] - delta;
        int y = maxima.ypoints[n] - delta;

        return new Point(x, y);
    }

    public static ImageProcessor getCrop(ImageProcessor ip, Rectangle bounds)  {
        ip.setRoi(bounds.x, bounds.y, bounds.width, bounds.height);
        ImageProcessor cropped =  ip.crop();
        ip.setRoi((Roi) null);
        return cropped;
    }

    public static Point estimateDriftMax(ImagePlus imp, int stepSize, double tol) {
        ImageStack stack = imp.getImageStack();
        int nslices = imp.getNSlices();

        int xpoints[] = CaAlgorithms.arange(nslices, stepSize);

        ImageProcessor ip1 = stack.getProcessor(xpoints[0]);
        Polygon p1 = CaImageUtils.findInlayPolygon(ip1);

        Point maxShift = new Point(0,0);
        double shiftLen = 0;

        for (int i = 1; i < xpoints.length; i++) {
            int x = xpoints[i];
            ImageProcessor ip2 = stack.getProcessor(x);
            Point shift = estimateDrift(ip1, ip2, tol, p1, null);

            double thisLen;
            if ((thisLen = Math.hypot(shift.x, shift.y )) > shiftLen) {
                shiftLen = thisLen;
                maxShift = shift;
            }
        }

        return maxShift;
    }

    public static IndexColorModel heatMap = null;
    public static synchronized IndexColorModel getHeatmap() {
        if (heatMap != null) {
            return heatMap;
        }

        final byte[] R = calcHeatMapChannel(-1.5f, 4.5f);
        final byte[] G = calcHeatMapChannel(-0.5f, 3.5f);
        final byte[] B = calcHeatMapChannel(0.5f, 2.5f);

        heatMap = new IndexColorModel(8, 256, R, G, B);
        return heatMap;
    }

    public static byte[] calcHeatMapChannel(float min, float max) {
        final byte[] data = new byte[256];

        for (int i = 0; i < data.length; i++) {
            float val = 4.0f * i / 256.0f;
            data[i] = clampByte (Math.min(val + min, -val + max));
        }

        return data;
    }

    public static byte clampByte(float value) {
        return (byte) (255.0f * Math.min (1.0f, Math.max(value, 0.0f)));
    }

}
