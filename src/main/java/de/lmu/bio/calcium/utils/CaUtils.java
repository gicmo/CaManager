package de.lmu.bio.calcium.utils;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import tracing.Path;

import java.awt.geom.Point2D;

public class CaUtils {
    public static Roi pathToRoiDownsample(Path path, double eps) {

        Point2D.Double[] input = new Point2D.Double[path.points];
        double p[] = new double[3];

        for (int i = 0; i < path.points; i++) {

            path.getPointDouble(i, p);
            input[i] = new Point2D.Double(p[0], p[1]);
        }

        Point2D.Double[] result = CaAlgorithms.downSamplePathRDP(input, 0, path.points - 1, eps);

        float x[] = new float[result.length];
        float y[] = new float[result.length];
        for (int i = 0; i < result.length; i++) {
            x[i] = (float) result[i].getX();
            y[i] = (float) result[i].getY();
        }
        System.err.println("Points: " + path.points + "downsampled to: " + result.length);

        return new PolygonRoi(x, y, result.length, Roi.POLYLINE);
    }
}
