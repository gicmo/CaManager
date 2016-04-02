package de.lmu.bio.calcium.utils;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import tracing.Path;

import javax.swing.*;
import javax.vecmath.Point2d;

public class CaUtils {
    public static Roi pathToRoiDownsample(Path path, double eps) {

        Point2d[] input = new Point2d[path.points];
        double p[] = new double[3];

        for (int i = 0; i < path.points; i++) {

            path.getPointDouble(i, p);
            input[i] = new Point2d(p[0], p[1]);
        }

        Point2d[] result = CaAlgorithms.downSamplePathRDP(input, 0, path.points - 1, eps);

        float x[] = new float[result.length];
        float y[] = new float[result.length];
        for (int i = 0; i < result.length; i++) {
            x[i] = (float) result[i].x;
            y[i] = (float) result[i].y;
        }

        System.err.println("Points: " + path.points + "downsampled to: " + result.length);

        return new PolygonRoi(x, y, result.length, Roi.POLYLINE);
    }

    public static Object findEntryInComboBox(JComboBox box, String item) {
        return box.getItemAt(findIndexInComboBox(box, item));
    }

    public static int findIndexInComboBox(JComboBox box, String item) {
        final int n = box.getItemCount();

        for (int i = 0; i < n; i++) {
            if (box.getItemAt(i).equals(item)) {
                return i;
            }
        }

        return 0;
    }
}
