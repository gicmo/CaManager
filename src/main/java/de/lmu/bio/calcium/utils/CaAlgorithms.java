package de.lmu.bio.calcium.utils;
import java.awt.*;
import java.awt.geom.Point2D;

public class CaAlgorithms {

    //Ramer–Douglas–Peucker algorithm
    public static Point2D.Double[] downSamplePathRDP(Point2D.Double[] input, int start, int end, double eps) {
        Point2D.Double a = input[start];
        Point2D.Double b = input[end];

        double dMax = Double.MIN_VALUE;
        int    iMax = 0;
        for (int i = start+1; i < end-1; i++) {
            double d = calcDistance(a, b, input[i]);
            if (d > dMax) {
                dMax = d;
                iMax = i;
            }
        }

        Point2D.Double[] result;
        if (!(dMax < eps)) {
            Point2D.Double[] X = downSamplePathRDP(input, start, iMax, eps);
            Point2D.Double[] Y = downSamplePathRDP(input, iMax, end, eps);

            System.err.print(X.length);
            result = new Point2D.Double[X.length + Y.length - 1];
            System.arraycopy(X, 0, result, 0, X.length - 1);
            System.arraycopy(Y, 0, result, X.length - 1, Y.length);
        } else {
            result = new Point2D.Double[] {input[start], input[end]};
        }

        return result;
    }

    //*****
    public static double calcDistance (Point2D.Double a, Point2D.Double b, Point2D.Double x) {
        double len = Math.hypot(b.x - a.x, b.y - a.y);
        double nom = Math.abs((x.x - a.x) * (b.y - a.y) - (x.y - a.y) * (b.x - a.x));
        return nom / len;
    }

    public static int findNearestPow2(int w, int h) {
        int wh_len = Math.min(w, h);

        int max_len = 1;
        int temp;
        while((temp = max_len << 1) < wh_len) {
            max_len = temp;
        }

        return max_len;
    }

    public static Point calcMeanPoint(Polygon poly, int n) {

        long sx = 0;
        long sy = 0;

        for (int i = 0; i < Math.min(poly.npoints, n); i++) {
            sx += poly.xpoints[i];
            sy += poly.ypoints[i];
        }

        int x = Math.round(sx / n);
        int y = Math.round(sy / n);

        return new Point(x,  y);
    }

    public static Polygon addPolygon(Polygon to, Polygon from) {
        for (int i = 0; i < from.npoints; i++)
            to.addPoint(from.xpoints[i], from.ypoints[i]);
        return to;
    }

    public static Polygon mergePolygons(Polygon a, Polygon b) {
        Polygon p = new Polygon();
        addPolygon(p, a);
        addPolygon(p, b);
        return p;
    }

    public static Integer[] createRange(int n) {
        Integer[] range = new Integer[n];

        for (int i = 0; i < n; i++) {
            range[i] = i;
        }

        return range;
    }

    public static int[] arange(int X, int step) {
        // N = ((X - 1) / step) + 1

        int N = (int) Math.floor((X + step - 1) / step);

        int x[] = new int[N];
        //one basis:     x_n = (n - 1) * step + 1
        //-> zero basis: x_n = n * step + 1
        for (int n = 0; n < N; n++) {
            x[n] = n * step + 1;
        }

        return x;
    }

    public static Rectangle createSquare(Polygon p, int height, int width) {
        Point mean = calcMeanPoint(p, p.npoints);

        int maxSizeX = Math.min(mean.x, width - mean.x);
        int maxSizeY = Math.min(mean.y, height - mean.y);

        if (maxSizeX < 0 || maxSizeY < 0) {
            throw new IllegalStateException("FIXME"); //FIXME
        }

        int size = findNearestPow2(maxSizeX, maxSizeY);

        return new Rectangle(mean.x - size, mean.y - size, 2*size, 2*size);
    }

    public static Rectangle createSquare(Polygon p, Dimension dims) {
        return createSquare(p, dims.height, dims.width);
    }

    public static float[][] calcDFF(float[][] data, int baseline, int start, int tlen) {

        if (data == null || data[0].length == 0) {
            return null;
        }

        // data is [position][time]
        tlen = Math.min(tlen, data[0].length);

        float[][] dff = new float[tlen][data.length];
        for (int pos = 0; pos < data.length; pos++) {

            float F = 0;
            float nF = Math.min(start + baseline, start + tlen);
            for (int t = start; t < nF; t++) {
                F += data[pos][t];
            }

            F /= (nF - start);

            for (int t = start; t < start + tlen; t++) {
                dff[t - start][pos] = (data[pos][t] - F) / F;
            }
        }

        return dff;
    }
}
