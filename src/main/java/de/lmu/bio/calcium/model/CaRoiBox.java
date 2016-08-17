package de.lmu.bio.calcium.model;

import ij.gui.Roi;
import ij.process.FloatPolygon;

public class CaRoiBox extends CaTreeNode {

    private Roi roi;

    public CaRoiBox(Roi roi) {
        this.roi = roi;
    }

    public Roi getRoi() {
        return roi;
    }


    public void setRoi(Roi roi) {
        this.roi = roi;
    }


    public String getName() {
        return roi.getName();
    }

    @Override
    public String toString() {
        if (roi == null) {
            return "Roi: FIXME";
        }

        return "Roi: " + getName();
    }

    public boolean isForeground() {
        return getName().equals("FG");
    }

    public boolean isBackground() {
        return getName().startsWith("BG");
    }

    public CaRoiClass getRoiClass() { return isForeground() ? CaRoiClass.FOREGROUND : CaRoiClass.BACKGROUND; }

    // wrap some Roi functions for convenience

    public int getType() {
        return roi.getType();
    }
    public float getStrokeWidth() {
        return roi.getStrokeWidth();
    }
    public FloatPolygon getFloatPolygon() {
        return roi.getFloatPolygon();
    }

}
