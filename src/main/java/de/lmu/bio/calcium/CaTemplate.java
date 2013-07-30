package de.lmu.bio.calcium;

import de.lmu.bio.calcium.io.JSONStringWriter;
import de.lmu.bio.calcium.model.CaGroup;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.model.CaTreeNode;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;


public class CaTemplate {
    JSONObject root = null;
    File path = null;

    public CaTemplate(File path) {
        this.path = path;
    }

    public void parse() throws Exception {
        JSONParser parser = new JSONParser();
        FileReader fr = new FileReader(path);
        Object obj = parser.parse(fr);
        root = (JSONObject) obj;
    }

    public String getFilename() {
        return path.getName();
    }

    public CaNeuron loadNeuron () {
        if (root == null)
            return null;

        CaNeuron neuron = new CaNeuron("");
        loadChildren(root, neuron);
        return neuron;
    }

    public static File[] listTemplates() {
        File documentsDir = new JFileChooser().getFileSystemView().getDefaultDirectory();
        //FIXME
        File templateDir = new File(documentsDir, "Documents" + File.separator + "Templates" + File.separator + "Calcium");
        System.err.println(templateDir.getAbsolutePath());
        FilenameFilter fnFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".catl.json") || name.endsWith(".catl");
            }
        };

        return templateDir.listFiles(fnFilter);
    }

    public static void saveRoi(JSONObject jobj, Roi roi, String name) {
        byte[] data = RoiEncoder.saveAsByteArray(roi);
        if (data == null)
            return;
        String base64 = javax.xml.bind.DatatypeConverter.printBase64Binary(data);
        jobj.put(name, base64);
    }

    private static JSONObject saveNode(CaTreeNode node) {
        JSONObject jobj = new JSONObject();
        if (node instanceof CaGroup) {
            CaGroup group = (CaGroup) node;
            jobj.put("type", "group");
            jobj.put("name", node.getUserObject());
            JSONArray grandChildren = saveChildren(node);
            jobj.put("children", grandChildren);
            jobj.put("container", group.allowImages());
        } else if (node instanceof CaImage) {
            CaImage image = (CaImage) node;
            jobj.put("type", "image");
            jobj.put("path", image.getFilePath());
            Roi roi = image.getRoiFg();
            saveRoi(jobj, roi, "roiFg");

            roi = image.getRoiBg();
            saveRoi(jobj, roi, "roiBg");
        }

        return jobj;
    }

    private static JSONArray saveChildren(CaTreeNode parent) {
        JSONArray children = new JSONArray();
        Enumeration en = parent.children();
        while (en.hasMoreElements()) {
            CaTreeNode node = (CaTreeNode) en.nextElement();
            JSONObject jobj = saveNode(node);
            children.add(jobj);
        }

        return children;
    }

    public static void saveToDisk(CaTreeNode root, String path) {
        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("name", "Neuron");
        JSONArray children =  saveChildren(root);
        jsonRoot.put("children", children);

        try {

            StringWriter stringWriter = new JSONStringWriter();
            jsonRoot.writeJSONString(stringWriter);
            FileWriter file = new FileWriter(path);
            file.write(stringWriter.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -----------------
    public static void loadChildren(JSONObject jsonObject, DefaultMutableTreeNode parent) {
        JSONArray children = (JSONArray) jsonObject.get("children");
        Iterator<JSONObject> iterator = children.iterator();
        while (iterator.hasNext()) {
            JSONObject subObj = iterator.next();
            loadObject(subObj, parent);
        }
    }

    public static Roi loadRoi(JSONObject obj, String key) {
        String dataAsString = (String) obj.get(key);
        if (dataAsString == null)
            return null;

        byte[] data = javax.xml.bind.DatatypeConverter.parseBase64Binary(dataAsString);
        return RoiDecoder.openFromByteArray(data);
    }

    public static void loadObject(JSONObject obj, DefaultMutableTreeNode parent) {
        String objType = (String) obj.get("type");
        DefaultMutableTreeNode res = null;

        if (objType.equals("image")) {
            CaImage img = new CaImage((String) obj.get("path"));
            res = img;

            img.setRoiFg(loadRoi(obj, "roiFg"));
            img.setRoiBg(loadRoi(obj, "roiBg"));

        } else if (objType.equals("group")) {

            boolean isImageContainer = (Boolean) obj.get("container");
            res = new CaGroup((String) obj.get("name"), isImageContainer);
            loadChildren(obj, res);
        }

        parent.add(res);
    }

    @Deprecated
    public static CaNeuron loadNeuronFromDisk(String path) {
        JSONParser parser = new JSONParser();
        CaNeuron neuron = new CaNeuron("Neuron");

        try {
            FileReader fr = new FileReader(path);
            Object obj = parser.parse(fr);
            JSONObject root = (JSONObject) obj;
            loadChildren(root, neuron);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return neuron;
    }


}
