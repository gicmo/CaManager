package de.lmu.bio.calcium.model;

import ij.gui.Roi;

public class CaRoiBox extends CaTreeNode {

    private Roi roi;

    CaRoiBox(Roi roi) {
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
        return getName().equals("BG");
    }

}
