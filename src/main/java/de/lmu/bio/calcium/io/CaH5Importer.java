package de.lmu.bio.calcium.io;

import de.lmu.bio.calcium.model.CaGroup;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.model.CaTreeNode;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.H5File;

import java.io.File;
import java.util.*;

public class CaH5Importer extends CaImporter {
    private H5File h5;
    private String path;
    private File pathPrefix = null;
    private CaNeuron neuron = null;

    public CaH5Importer(String path) {
        super("Importing file");
        this.path = path;
        File f = new File(path);
        pathPrefix = f.getParentFile();
        System.out.println("Path prefix " + pathPrefix );
    }

    public void open() throws Exception {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        h5 = (H5File) fileFormat.createInstance(path, FileFormat.READ);
        h5.open();
    }

    public void close() throws Exception {
        h5.close();
    }

    @Override
    protected void runTask() throws Exception {
        open();
        neuron = loadNeuron();
        close(); //make sure file is always closed by catching any exception and rethrowing it
    }

    @Override
    public CaNeuron getNeuron() {
        return neuron;
    }

    public CaNeuron loadNeuron() throws Exception {

        HObject ho = h5.get("/");
        List<Group> subgroups = listSubGroups((Group) ho);
        Group neuronGroup = null;
        for (Group g : subgroups) {
            String type = getGroupType(g);
            if (type != null && type.equals("neuron")) {
                neuronGroup = g;
                break;
            }
        }

        if (neuronGroup == null)
            return null;

        CaNeuron neuron = new CaNeuron(neuronGroup.getName());
        loadNeuron(neuronGroup, neuron);
        loadGroup(neuronGroup, neuron);

        return neuron;
    }

    public void loadGroup(Group group, CaGroup parent) {
        String allowImages = getStringAttribug(group, "allowImages");
        parent.setAllowImages(allowImages != null && allowImages.equalsIgnoreCase("yes"));

        //now load all the children<of tye Group> of this group
        List<Group> subgroups = listSubGroups(group);
        for (Group g : subgroups) {
            String type = getGroupType(g);
            if (type == null) {
                System.err.println("Ignoring node [type == null]");
                return;
            }

            CaTreeNode newNode = null;
            if (type.equals("group")) {
                CaGroup nG = new CaGroup(g.getName());
                newNode = nG;
                loadGroup(g, nG);
            } else if (type.equals("image")) {
                newNode = loadImage(g);
            } else {
                System.err.println("Ignoring node [unknown type:" + type +" ]");
            }

            if (newNode != null) {
                parent.add(newNode);
            }
        }
    }


    public CaImage loadImage(Group imageGroup) {
        String name = imageGroup.getName();
        File pathFile = new File(pathPrefix, name + ".tif"); //FIXME
        String path = pathFile.getAbsolutePath();
        CaImage image = new CaImage(path);

        Map<String, Dataset> map = generateDatasetMap(imageGroup);

        Dataset dset = map.get("roiFg");
        Roi roi = loadRoi(dset);
        image.setRoiFg(roi);

        dset = map.get("roiBg");
        roi = loadRoi(dset);
        image.setRoiBg(roi);

        return image;
    }

    public void loadNeuron(Group neuronGroup, CaNeuron neuron) {
        String region = getStringAttribug(neuronGroup, "region");
        String comment = getStringAttribug(neuronGroup, "comment");

        neuron.setComment(comment);
        neuron.setRegion(region);
    }

    public Roi loadRoi(Dataset dset) {
        if (dset == null)
            return null;

        dset.init();
        Roi roi = null;
        try {
            Object objData = dset.read();
            if (!(objData instanceof byte[]))
                return null;
            byte[] data = (byte[]) objData;
            roi = RoiDecoder.openFromByteArray(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return roi;
    }

    public static Map<String, Attribute> getAttributes(HObject hObject)  {
        if (hObject == null)
            return null;

        HashMap<String, Attribute> map = new HashMap<String, Attribute>();

        List mdList = null;
        try {
            mdList = hObject.getMetadata();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mdList == null) {
            return null;
        }

        for (Object obj : mdList) {
            if (!(obj instanceof Attribute))
                continue;
            Attribute attr = (Attribute) obj;
            String key = attr.getName();
            map.put(key, attr);
        }

        return map;
    }

    public static Attribute findAttribute(HObject object, String attrName)  {
        Map<String, Attribute> map = getAttributes(object);
        return map.get(attrName);
    }

    public static String getStringAttribug(HObject object, String name) {
        Attribute attr = findAttribute(object, name);

        if (attr == null) {
            return null;
        }

        Object objVal = attr.getValue();
        String result = null;
        if (objVal instanceof String) {
            result = (String) objVal;
        } else if (objVal instanceof String[]) {
            String[] strArray = (String []) objVal;
            if (strArray.length == 1) {
                result = strArray[0];
            } else {
                System.err.println("getGroupType: String array length > 1");
            }
        }
        else {
            System.err.println("getGroupType: objVal is of unknown type");
        }

        return result;
    }

    public static String getGroupType(Group group) {
        return getStringAttribug(group, "type");
    }

    public static Map<String, Dataset> generateDatasetMap(Group g) {
        if (g == null)
            return null;

        HashMap<String, Dataset> map = new HashMap<String, Dataset>();

        for (Object obj : g.getMemberList()) {
            if (!(obj instanceof Dataset))
                continue;

            Dataset dset = (Dataset) obj;
            String key = dset.getName();
            map.put(key, dset);

        }

        return map;
    }

    public static List<Group> listSubGroups(Group parent) {
        if (parent == null)
            return null;

        ArrayList<Group> list = new ArrayList<Group>();

        for (Object m : parent.getMemberList()) {
            if (!(m instanceof Group))
                continue;

            Group sg = (Group) m;
            list.add(sg);
        }

        return list;
    }

}
