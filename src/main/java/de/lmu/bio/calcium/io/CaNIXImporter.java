package de.lmu.bio.calcium.io;


import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.model.CaRoiBox;
import de.lmu.bio.calcium.model.CaRoiClass;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import org.g_node.nix.*;
import ucar.ma2.Array;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class CaNIXImporter extends CaImporter {

    private CaNeuron neuron = null;

    private String path;

    //progress tracking
    private int imagesTotal = 0;
    private int imagesProcessed = 0;

    public CaNIXImporter(String path) {
        super("Importing");
        this.path = path;
    }

    @Override
    public CaNeuron getNeuron() {
        return neuron;
    }

    @Override
    protected void runTask() throws Exception {
        File fd = File.open(path, FileMode.ReadOnly);

        List<Block> blocks = fd.getBlocks(b -> b.getType().equals("neuron"));
        if (blocks.size() != 1) {
            throw new RuntimeException("None or too many blocks with type neuron found");
        }

        Block block = blocks.get(0);
        Section meta = block.getMetadata();

        neuron = new CaNeuron(block.getName());

        if (meta.hasProperty("region")) {
            neuron.setRegion(meta.getProperty("region").getValues().get(0).getString());
        }

        if (meta.hasProperty("age")) {
            neuron.setAge(meta.getProperty("age").getValues().get(0).getString());
        }

        if (meta.hasProperty("condition")) {
            neuron.setCondition(meta.getProperty("condition").getValues().get(0).getString());
        }

        if (meta.hasProperty("sex")) {
            neuron.setSex(meta.getProperty("sex").getValues().get(0).getString());
        }

        if (meta.hasProperty("subregion")) {
            neuron.setSubregion(meta.getProperty("subregion").getValues().get(0).getString());
        }

        if (meta.hasProperty("litter")) {
            neuron.setLitter(meta.getProperty("litter").getValues().get(0).getString());
        }

        List<Group> imgGroups = block.getGroups(g -> g.getType().equals("image.ca"));
        imagesTotal = imgGroups.size();

        imgGroups.stream().map(this::importImage)
                .sorted(Comparator.comparingInt(i -> Integer.parseInt(i.getName())))
                .forEach(i -> neuron.add(i));

        fd.close();
    }

    private CaImage importImage(Group g) {
        fireTaskProgress(imagesProcessed, imagesTotal, "Importing " + g.getName());
        Section meta = g.getMetadata();

        String filename = meta.getProperty("filename").getValues().get(0).getString();
        java.io.File ps = new java.io.File(new java.io.File(path).getParentFile(), filename);

        String filepath;
        if (ps.exists()) {
            filepath = ps.getAbsolutePath();
        } else {
            filepath = meta.getProperty("original_path").getValues().get(0).getString();
        }

        CaImage img = new CaImage(filepath);

        CaImage.Metadata im = img.new Metadata();

        im.ctime = meta.getProperty("creation_time").getValues().get(0).getLong();
        im.channels = meta.getProperty("channels").getValues().get(0).getInt();

        if (meta.hasProperty("condition")) {
            img.setCondition(meta.getProperty("condition").getValues().get(0).getString());
        }

        DataArray chans = g.getDataArray(g.getName() + ".channels");
        RangeDimension dim = chans.getDimensions().get(0).asRangeDimension();

        int extent = chans.getDataExtent().getData()[0];
        int[] data = new int[extent];
        chans.getData(data, new NDSize(1, extent), new NDSize(1, 0)); //FIXME check
        double[] ticks = dim.getTicks();

        im.planeInfo = new CaImage.PlaneInfo[data.length];
        for (int i = 0; i < data.length; i++) {
            im.planeInfo[i] = img.new PlaneInfo();
            im.planeInfo[i].timePoint = i;
            im.planeInfo[i].channel = data[i];
            im.planeInfo[i].deltaT = ticks[i];
        }
        im.timePoints = data.length;
        img.setMetadata(im);

        List<DataArray> roiData = g.getDataArrays(d -> d.getType().startsWith("roi.pt"));

        roiData.stream().map(d -> {

            Section rm = d.getMetadata();
            int roiType = rm.getProperty("type").getValues().get(0).getInt();

            Roi roi = null;
            if (roiType == Roi.RECTANGLE) {
                roi = importRectRoi(d);
            } else if (roiType == Roi.POLYLINE || roiType == Roi.FREELINE) {
                roi = importPolyRoi(d);
            } else {
                return null;
            }

            String[] comps = d.getName().split("\\.");
            roi.setName(comps[comps.length - 1].toUpperCase());

            CaRoiBox box =  new CaRoiBox(roi);
            Color color = null;
            if (rm.hasProperty("color")) {
                int rgb = rm.getProperty("color").getValues().get(0).getInt();
                color = new Color(rgb);
            } else {
                CaRoiClass cls = box.getRoiClass();
                color = cls.roiColor();
            }

            roi.setStrokeColor(color);
            return box;
        }).filter(b -> b != null).forEach(img::add);

        imagesProcessed++;
        return img;
    }

    Roi importPolyRoi(DataArray d) {
        Section rm = d.getMetadata();
        int roiType = rm.getProperty("type").getValues().get(0).getInt();
        double strokeWidth = rm.getProperty("strokeWidth").getValues().get(0).getDouble();

        NDSize shape = d.getDataExtent();
        float[] xy = new float[(int) shape.getElementsProduct()];
        d.getData(xy, shape, new NDSize(2, 0));

        int n = shape.getData()[1];
        float[] x = new float[n];
        float[] y = new float[n];

        System.arraycopy(xy, 0, x, 0, n);
        System.arraycopy(xy, n, y, 0, n);
        Roi roi =  new PolygonRoi(x, y, roiType);
        if (strokeWidth > 1.0) {
            roi.setStrokeWidth(strokeWidth);
        }
        return roi;
    }

    Roi importRectRoi(DataArray d) {
        float[] pts = new float[4];
        d.getData(pts, new NDSize(1, 4), new NDSize(1, 0));

        return new Roi(pts[0], pts[1], pts[2], pts[3]);
    }

}
