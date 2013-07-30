package de.lmu.bio.calcium.ui;

import de.lmu.bio.calcium.model.CaImage;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;

import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CaImageWindow extends StackWindow implements MouseListener {
    protected CaImage image;
    protected DefaultTreeModel treeModel;

    public CaImageWindow(ImagePlus timp, CaImage img, DefaultTreeModel treeModel) {
        super(new CaImagePlus(timp));
        this.image = img;
        CaIWOverlay overlay = new CaIWOverlay();
        imp.setOverlay(overlay);
        getCanvas().addMouseListener(this);
        this.treeModel = treeModel;
    }

    public static CaImageWindow createWindow(CaImage img, DefaultTreeModel treeModel) {
        ImagePlus ip = img.openImage();

        if (ip == null) {
            IJ.showMessage("Could not open Image");
            return null;
        }

        return new CaImageWindow(ip, img, treeModel);
    }

    public void replaceCurrentRoi(Roi newRoi) {
        Roi roi = imp.getRoi();
        Overlay overlay = imp.getOverlay();
        if (overlay.contains(roi)) {
            overlay.remove(roi);
            imp.deleteRoi();
        }

        imp.setRoi(newRoi, true);
    }

    public CaImage getCaImage() {
        return image;
    }

    //Event listeners (Mouse, Keyboard)
    //------------------------------------------------------
    private boolean roiChanged = false;
    @Override
    public void mouseClicked(MouseEvent e) { /* System.out.println("Mouse clicked!"); */ }

    @Override
    public void mousePressed(MouseEvent e) {
        Roi roi = imp.getRoi();
        if (roi != null && roi.getState() != 3) {
            System.out.println("Roi change: Pressed State " + roi.getState());
            roiChanged = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Overlay overlay = imp.getOverlay();
        System.err.println(overlay);
        Roi roi = imp.getRoi();

        if (roiChanged) {
            if (roi != null) {
                System.out.println("Roi Changed! " + roi.getState());
            }
            roiChanged = false;
        }

        if (roi != null && roi.getState() != Roi.CONSTRUCTING && !overlay.contains(roi)) {
            System.out.println("State " + roi.getState());
            System.err.print("New ROI!\n");
            overlay.add(roi);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    //------------------------------------------------------------------------------------------------------------------
    public class CaIWOverlay extends Overlay {

        public CaIWOverlay() {
            drawLabels(true);
            drawNames(true);
            drawBackgrounds(true);
            add(image.getRoiFg());
            add(image.getRoiBg());
        }

        @Override
        public void remove(Roi roi) {
            super.remove(roi);
            System.err.print("ROI REMOVED!\n");
            if (image.getRoiBg() == roi) {
                image.setRoiBg(null);
            } else if (image.getRoiFg() == roi) {
                image.setRoiFg(null);
            }
        }

        @Override
        public void remove(int index) {
            Roi roi = super.get(index);
            remove(roi);
            System.err.print("ROI REMOVED (+)!\n");
        }

        public boolean missRoiType(String type) {
            return getIndex(type) == -1;
        }

        @Override
        public void add(Roi roi) {

            if (roi == null)
                return;

            System.err.println("Overlay: Roi added!");

            int type = -1;
            //Exclude certain types from being valid FG & BG Rois
            if (roi.getType() > 1 ) {
                if (missRoiType("FG")) {
                    type = 1;
                } else if (missRoiType("BG")) {
                    type = 0;
                }
            }

            switch(type) {
                case 0:
                    roi.setStrokeColor(Color.blue);
                    roi.setName("BG");
                    image.setRoiBg(roi);
                    break;

                case 1:
                    roi.setName("FG");
                    roi.setStrokeColor(Color.red);
                    image.setRoiFg(roi);
                    break;

                default:
                    roi.setStrokeWidth(0.1);
            }

            super.add(roi);
        }
    }

    //Event listeners (Mouse, Keyboard)
    //------------------------------------------------------
    public static class CaImagePlus extends ImagePlus {

        public CaImagePlus(ImagePlus tip) {
            String title = tip.getTitle();
            setImage(tip);
            setTitle(title);
        }

        @Override
        public void createNewRoi(int sx, int sy) {
            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] create ROI!");
            super.createNewRoi(sx, sy);
        }

        @Override
        public void setRoi(Roi newRoi, boolean updateDisplay) {
            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] set ROI!");
            super.setRoi(newRoi, updateDisplay);
            Overlay overlay = getOverlay();
            if (overlay != null && !overlay.contains(roi)) {
                overlay.add(roi);
            }

        }

        @Override
        public void deleteRoi() {
            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] delete ROI!");
            super.deleteRoi();
        }
//
//        @Override
//        public void restoreRoi() {
//            super.restoreRoi();
//            System.err.println("[de.lmu.bio.calcium.ui.CaImageWindow.CaImagePlus] restore ROI!");
//            Overlay overlay = getOverlay();
//            if (overlay != null) {
//                overlay.add(roi);
//            }
//        }
    }
}
