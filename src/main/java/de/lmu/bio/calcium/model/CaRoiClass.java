package de.lmu.bio.calcium.model;

import ij.gui.Roi;

import java.awt.*;

public enum CaRoiClass {
    FOREGROUND, BACKGROUND;

    public boolean checkType(Roi roi) {
        return checkType(roi, this);
    }

    public static boolean checkType(Roi roi, CaRoiClass cls) {
        final int type = roi.getType();
        switch (cls) {
            case FOREGROUND:
                switch (type) {
                    case Roi.POLYLINE:
                    case Roi.LINE:
                        return true;
                    default:
                        return false;
                }
            case BACKGROUND:
                switch (type) {
                    case Roi.LINE:
                    case Roi.POLYLINE:
                    case Roi.RECTANGLE:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    public Color roiColor() {
        switch (this) {
            case FOREGROUND:
                return Color.RED;
            case BACKGROUND:
                return Color.BLUE;
        }

        return Color.YELLOW;
    }

    public String toString() {
        switch (this) {
            case FOREGROUND:
                return "fg";
            case BACKGROUND:
                return "bg";
        }

        return "unknown";
    }
}
