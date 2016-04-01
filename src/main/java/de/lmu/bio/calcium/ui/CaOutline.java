package de.lmu.bio.calcium.ui;

import de.lmu.bio.calcium.model.CaGroup;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaTreeNode;
import ij.IJ;
import ij.gui.Roi;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CaOutline extends Outline implements MouseListener {

    private Delegate delegate;
    private DefaultTreeModel treeModel;

    public CaOutline(DefaultTreeModel tModel) {

        setTreeModel(tModel);
        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT_ROWS);

        setTransferHandler(new CaOutlineTransferHandler());

        setRootVisible(true);
        addMouseListener(this);
    }

    public void setDelegate(Delegate handler) {
        this.delegate = handler;
    }

    public void setTreeModel(DefaultTreeModel tModel) {
        treeModel = tModel;
        RowModel rowModel = new CaOutlineRowModel();
        setModel(DefaultOutlineModel.createOutlineModel(tModel, rowModel, false, "Images"));
    }

    //------------------------------------------------------------------------------------------------------------------
    public static class CaOutlineDataProvider implements RenderDataProvider {
        private Icon iconGroupOpen;
        private Icon iconLeaf;
        protected Color readyImage = new Color(125, 174, 131);

        public CaOutlineDataProvider() {
            UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
            iconGroupOpen = uiDefaults.getIcon("Tree.openIcon");
            iconLeaf = uiDefaults.getIcon("Tree.leafIcon");
        }

        public java.awt.Color getBackground(Object o) {
            return null;
        }

        public String getDisplayName(Object o) {
            return null;
        }

        public java.awt.Color getForeground(Object o) {
                 //UIManager.getColor ("controlShadow");
            if (o instanceof CaImage) {
                CaImage image = (CaImage) o;
                if (image.getRoiBg() != null &&
                        image.getRoiFg() != null) {
                    return readyImage;
                }
            }

            return null;
        }

        public javax.swing.Icon getIcon(Object o) {
            if (o instanceof CaGroup) {
                return iconGroupOpen;
            } else if (o instanceof CaImage) {
                return iconLeaf;
            }
            return null;
        }

        public String getTooltipText(Object o) {
            return null;
        }

        public boolean isHtmlDisplayName(Object o) {
            return false;
        }
    }

    //MouseListener
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {

        if (delegate == null)
            return;

        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && !e.isConsumed()) {
            e.consume();
            int row = getSelectedRow();
            Object object = getValueAt(row, 0);
            delegate.doubleClicked(row, object);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        popupHandler(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        popupHandler(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    public void popupHandler(MouseEvent e) {
        if (!e.isPopupTrigger())
            return;

        delegate.popupTriggered(this, e.getPoint());
    }

    //------------------------------------------------------------------------------------------------------------------
    public interface Delegate {
        void doubleClicked(int row, Object object);
        void popupTriggered(CaOutline outline, Point p);
    }


    //------------------------------------------------------------------------------------------------------------------
    public static class CaOutlineRowModel implements RowModel {

        private enum Column {
            Time(0, "Time", false),
            Trial(1, "Trial", true),
            Slices(2, "Slices", false),
            Drift(3, "Drift", false),
            ROILength(4, "ROI Length", false);

            public final int id;
            public final String name;
            public final boolean editable;
            Column(int id, String name, boolean editable) {
                this.id = id;
                this.name = name;
                this.editable = editable;
            }

            public static Column fromInt(int id) {
                for(Column r : Column.values()) {
                    if (r.id == id) {
                        return r;
                    }
                }

                throw new RuntimeException("Invalid key for row " + id);
            }
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Object getValueFor(Object o, int i) {

            if (!(o instanceof CaImage)) {
                return "";
            }

            CaImage image = (CaImage) o;

            Column r = Column.fromInt(i);

            switch (r) {
                case Time:
                    long mtime = image.getCTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date(mtime);
                    return sdf.format(date);

                case Trial:
                    int trail = image.getTrial();
                    return Integer.toString(trail);

                case Slices:
                    int slices = image.getNslices();
                    if (slices < 0) {
                        return "";
                    }
                    return Integer.toString(slices);

                case Drift:
                    Point drift = image.getDrift();
                    if (drift == null) {
                        return "";
                    }

                    double len = Math.hypot(drift.x, drift.y);
                    return IJ.d2s(len);

                case ROILength:
                    Roi roi = image.getRoiFg();
                    //double len = de.lmu.bio.calcium.model.CaImage.getRoiLength(roi, null);
                    len = 0;
                    if (roi != null) {
                        len = roi.getLength();
                    }
                    return len > 0 ? IJ.d2s(len) : "";
            }

            return "";
        }

        @Override
        public Class getColumnClass(int i) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(Object o, int i) {
            return o instanceof CaImage && Column.fromInt(i).editable;
        }

        @Override
        public void setValueFor(Object o, int i, Object o1) {
            if (Column.fromInt(i) == Column.Trial) {
                if (!(o1 instanceof String) || !(o instanceof CaImage)) {
                    return;
                }
                String str = (String) o1;
                CaImage image = (CaImage) o;
                int trial = Integer.parseInt(str);
                image.setTrial(trial);
            }
        }

        @Override
        public String getColumnName(int i) {
            return Column.fromInt(i).name;
        }
    }
    //
    //------------------------------------------------------------------------------------------------------------------
    private class CaOutlineTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent component) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent component) {
            Outline outline = (Outline) component;

            ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
            int[] selectedRows = outline.getSelectedRows();
            for (int row : selectedRows) {
                Object object = outline.getValueAt(row, 0);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;

                if (!(node instanceof CaImage))
                    continue;

                nodes.add(node);
            }

            return new NodesContainer(nodes);
        }

        @Override
        public boolean canImport(TransferSupport support) {

            if (!support.isDataFlavorSupported(NodesContainer.getDataFlavor()) &&
                    !support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                return false;

            int row = ((Outline.DropLocation) support.getDropLocation()).getRow();
            Outline outline = (Outline) support.getComponent();
            Object node = outline.getValueAt(row, 0);
            //System.err.println("canImport " + row);
            if (!(node instanceof CaGroup)) {
                return false;
            }

            CaGroup group = (CaGroup) node;
            boolean isContainer = group.allowImages();
            // System.err.println("canImport " + isContainer);
            //support.setShowDropLocation(isContainer);
            return isContainer;
        }

        public boolean importFiles(Transferable trans, DefaultTreeModel model, CaTreeNode parent, int index) {
            List<File> data = null;
            try {
                data = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (data == null)
                return false;

            for (File file : data) {
                CaImage image = new CaImage("file://" + file.getAbsolutePath());
                model.insertNodeInto(image, parent, index);
            }
            return true;
        }

        public boolean importNodes(Transferable trans, DefaultTreeModel model, CaTreeNode parent, int index) {
            List<DefaultMutableTreeNode> data = null;
            try {
                Object transferData = trans.getTransferData(NodesContainer.getDataFlavor());
                data = (List<DefaultMutableTreeNode>) transferData;
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (data == null)
                return false;

            for (DefaultMutableTreeNode node : data) {
                model.insertNodeInto((MutableTreeNode) node.clone(), parent, 0);
            }

            return true;
        }

        @Override
        public boolean importData(TransferSupport support) {

            if (!canImport(support)) {
                System.err.println("Cannot import data");
                return false;
            }

            Outline outline = (Outline) support.getComponent();
            Outline.DropLocation dropLocation = (Outline.DropLocation) support.getDropLocation();
            int row = dropLocation.getRow();
            Object obj = outline.getValueAt(row, 0);
            CaTreeNode parent = (CaTreeNode) obj;

            //int index = dropLocation.getChildIndex();
            //if (index == -1)
            //    index = group.getChildCount();

            Transferable transferable = support.getTransferable();

            boolean res = false;
            if (support.isDataFlavorSupported(NodesContainer.getDataFlavor())) {
                res = importNodes(transferable, treeModel,  parent, 0);
            } else if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                res = importFiles(transferable, treeModel,  parent, 0);
            }

            return res;
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int action) {
            if (action != TransferHandler.MOVE) {
                return;
            }

            java.util.List<DefaultMutableTreeNode> data = null;
            try {
                data = (List<DefaultMutableTreeNode>) t.getTransferData(NodesContainer.getDataFlavor());
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (data == null) {
                return;
            }

            for (DefaultMutableTreeNode node : data) {
                treeModel.removeNodeFromParent(node);
            }
        }
    }
}
