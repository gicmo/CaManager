package de.lmu.bio.calcium.tools;

import ij.IJ;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CaRoiCloner {

    public static Roi cloneMove (Roi source, Point shift) {

        if (source == null)
            return null;

        Roi target = null;
        switch (source.getType()) {

            case Roi.RECTANGLE:
                target = cloneMoveRect(source, shift);
                break;

            case Roi.POLYLINE:
            case Roi.FREELINE:
            case Roi.POLYGON:
                PolygonRoi pr = (PolygonRoi) source;
                target = cloneMove(pr, shift);
                break;

            case Roi.LINE:
                Line lr = (Line) source;
                target = cloneMove(lr, shift);
                break;

            default:
                IJ.showMessage("Roi type: " + source.getTypeAsString() + " not supported." +
                               "\n Please report!");

        }

        if (target != null) {
            target.setStrokeWidth(target.getStrokeWidth());
            target.setStroke(source.getStroke());
            target.setFillColor(source.getFillColor());
        }

        return target;

    }

    public static FloatPolygon cloneMove(FloatPolygon source, Point shift) {
        FloatPolygon poly = source.duplicate();

        for (int n = 0; n < poly.npoints; n++) {
            poly.xpoints[n] += shift.x;
            poly.ypoints[n] += shift.y;
        }
        return poly;
    }

    public static Polygon cloneMove(Polygon source, Point shift) {
        Polygon poly = new Polygon(source.xpoints, source.ypoints, source.npoints);

        for (int n = 0; n < poly.npoints; n++) {
            poly.xpoints[n] += shift.x;
            poly.ypoints[n] += shift.y;
        }

        return poly;
    }

    public static PolygonRoi cloneMove(PolygonRoi source, Point shift) {

        PolygonRoi p;
        if (source.subPixelResolution()) {
            FloatPolygon fp = source.getFloatPolygon();
            FloatPolygon dp = cloneMove(fp, shift);
            p = new PolygonRoi(dp, source.getType());
        } else {
            Polygon ip = source.getPolygon();
            Polygon dp = cloneMove(ip, shift);
            p = new PolygonRoi(dp, source.getType());
        }

        return p;
    }

    public static Roi cloneMoveRect(Roi source, Point shift) {
       Roi target;

        if (source.subPixelResolution()) {

            Rectangle2D.Double bounds = source.getFloatBounds();

            target = new Roi(bounds.x + shift.x,
                             bounds.y + shift.y,
                             bounds.width,
                             bounds.height);

        } else {
            Rectangle r = source.getBounds();
            r.x += shift.x;
            r.y += shift.y;

            target = new Roi(r);
        }

        target.setCornerDiameter(target.getCornerDiameter());

        return target;
    }

    public static Line cloneMove(Line source, Point shift) {

        Line target;
        if (source.subPixelResolution()) {

            target = new Line(source.x1d + shift.x,
                              source.y1d + shift.y,
                              source.x2d + shift.x,
                              source.y2d + shift.y);
        } else {
            target = new Line(source.x1 + shift.x,
                              source.y1 + shift.y,
                              source.x2 + shift.x,
                              source.y2 + shift.y);
        }

        return target;
    }



}
