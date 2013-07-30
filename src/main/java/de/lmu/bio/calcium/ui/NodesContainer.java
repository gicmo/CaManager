package de.lmu.bio.calcium.ui;

import javax.swing.tree.TreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class NodesContainer implements Transferable {
    private static DataFlavor dataFlavor;
    private java.util.List<TreeNode> nodes;

    public static DataFlavor getDataFlavor() {
        if (dataFlavor == null) {
            try {
                dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                        ";class=java.util.ArrayList");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return dataFlavor;
    }

    NodesContainer(java.util.List<TreeNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{getDataFlavor()};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.match(dataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if(!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);
        return nodes;
    }

}
