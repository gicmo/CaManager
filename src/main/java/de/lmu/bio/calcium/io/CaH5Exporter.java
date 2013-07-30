package de.lmu.bio.calcium.io;

import de.lmu.bio.calcium.tools.CaKymoGrapher;
import de.lmu.bio.calcium.CaTask;
import de.lmu.bio.calcium.model.CaGroup;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.model.CaTreeNode;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.H5Datatype;
import ncsa.hdf.object.h5.H5File;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;


public class CaH5Exporter extends CaTask {
    private H5File h5;
    private String path;
    Datatype floatType = null;
    Datatype byteType = null;
    private CaNeuron neuron;
    protected int imagesTotal = 0;
    protected int imageProcessed = 0;

    public CaH5Exporter(String path, CaNeuron neuron) {
        super("Exporting");
        this.path = path;
        this.neuron = neuron;
    }

    public void saveNeuron(CaNeuron neuron) throws Exception {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        if (fileFormat == null) {
            IJ.showMessage("Unable to load H5 library");
            return;
        }

        floatType = new H5Datatype(Datatype.CLASS_FLOAT, Datatype.NATIVE, Datatype.NATIVE, -1);
        byteType  = new H5Datatype(Datatype.CLASS_CHAR, Datatype.NATIVE, Datatype.NATIVE, -1);

        h5 = (H5File) fileFormat.createFile(path, FileFormat.FILE_CREATE_DELETE);

        int res = h5.open();
        Group root = h5.createGroup("/" + neuron.getName(), null);
        writeStringAttribute(root, "type", "neuron");
        writeStringAttribute(root, "region", neuron.getRegion());
        writeStringAttribute(root, "comment", neuron.getComment());
        saveChildren(neuron, root);
        h5.close();
    }

    protected void saveChildren(CaTreeNode parent, Group h5Group) throws Exception {
        Enumeration en = parent.children();
        while (en.hasMoreElements()) {
            CaTreeNode node = (CaTreeNode) en.nextElement();
            if (node instanceof CaGroup) {
                saveGroup((CaGroup) node, h5Group);
            } else if (node instanceof CaImage) {
                saveImage((CaImage) node, h5Group);
            }
        }
    }

    protected void writeStringAttribute(Group group, String name, String data) throws Exception {
        if (data == null || data.length() < 1)
            return;

        int type_id = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
        H5.H5Tset_size(type_id, data.length()); //HDF5Constants.H5T_VLEN)
        int DataSpaceId = H5.H5Screate(HDF5Constants.H5S_SCALAR);
        int GroupId = group.open();
        int attr = H5.H5Acreate(GroupId, name, type_id, DataSpaceId, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
        H5.H5Awrite(attr, type_id, data.getBytes());
        H5.H5Tclose(type_id);
        H5.H5Aclose(attr);
        group.close(GroupId);
    }

    protected void writAttribute(Group group, String name, long data) throws  Exception {
        int DataSpaceId = H5.H5Screate(HDF5Constants.H5S_SCALAR);
        int GroupId = group.open();
        int attr = H5.H5Acreate(GroupId, name, HDF5Constants.H5T_STD_I64LE, DataSpaceId, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
        H5.H5Awrite(attr, HDF5Constants.H5T_STD_I64LE, new Long[] {data}); //should be BE? getBytes()?
        H5.H5Aclose(attr);
        group.close(GroupId);
    }

     protected void saveGroup(CaGroup group, Group parent) throws Exception {
        Group container = h5.createGroup(group.getName(), parent);

        writeStringAttribute(container, "allowImages", group.allowImages() ? "yes" : "no");
        writeStringAttribute(container, "type", "group");

        saveChildren(group, container);
    }

    protected void saveImage(CaImage image, Group h5Group) throws Exception {

        fireTaskProgress(imageProcessed, imagesTotal, "Exporting " + image.getName());

        String name = image.getName();
        Group imgGroup = h5.createGroup(name, h5Group);

        writeStringAttribute(imgGroup, "type", "image");
        writAttribute(imgGroup, "ctime", image.getCTime());

        Roi roiBg = image.getRoiBg();
        Roi roiFg = image.getRoiFg();

        if (roiBg == null && roiFg == null) {
            imageProcessed++;
            return;
        }

        saveRoi(imgGroup, image.getRoiFg(), "roiFg");
        saveRoi(imgGroup, image.getRoiBg(), "roiBg");

        String path = image.getFilePath();
        ImagePlus ip = IJ.openImage(path);

        saveKymoGraph(imgGroup, ip, image.getRoiFg(), "kymoFg");
        saveKymoGraph(imgGroup, ip, image.getRoiBg(), "kymoBg");
        ip.close();

        imageProcessed++;
    }

    public void saveRoi(Group h5Group, Roi roi, String name) throws Exception {
        byte[] data = RoiEncoder.saveAsByteArray(roi);
        if (data == null)
            return;
        Dataset ds = h5.createScalarDS(name, h5Group, byteType, new long[]{data.length}, null, null, 0, data);
    }

    public void saveKymoGraph(Group h5Group, ImagePlus ip, Roi roi, String name) throws Exception {
        if (roi == null)
            return;

        float[][] data = CaKymoGrapher.createRawKymoGraph(ip, roi);
        Dataset ds = h5.createScalarDS(name, h5Group, floatType, new long[]{data.length, data[0].length}, null, null, 0, data);
    }

    @Override
    public void runTask() throws Exception {

        Enumeration<DefaultMutableTreeNode> en = neuron.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();
            if (!(node instanceof CaImage)) {
                continue;
            }
            CaImage img = (CaImage) node;
            imagesTotal++;
        }

        fireTaskProgress(imageProcessed, imagesTotal, "Starting export...");
        saveNeuron(neuron);
    }

    //---
    public void fireTaskProgress(String message) {
        fireTaskProgress(imageProcessed, imagesTotal, message);
    }
}
