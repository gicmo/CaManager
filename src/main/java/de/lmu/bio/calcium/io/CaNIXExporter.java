package de.lmu.bio.calcium.io;

import de.lmu.bio.calcium.CaTask;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.model.CaRoiBox;
import de.lmu.bio.calcium.tools.CaKymoGrapher;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.process.FloatPolygon;
import org.g_node.nix.*;

public class CaNIXExporter extends CaTask {
    private CaNeuron neuron;
    private String path;

    //progress bookkeeping
    private int imagesTotal = 0;
    private int imageProcessed = 0;

    private Block block;
    private Section meta;

    public CaNIXExporter(String path, CaNeuron neuron) {
        super("Exporting to NIX @ " + path);
        this.neuron = neuron;
        this.path = path;
    }

    private class KymoExporter {
        private CaImage img;
        private Group group;
        private ImagePlus imp;

        KymoExporter(CaImage img, Group g, ImagePlus imp) {
            this.img = img;
            this.group = g;
            this.imp = imp;
        }

        boolean exportKymo(CaRoiBox roi) {
            int roiType = roi.getType();
            if (!(roiType == Roi.POLYLINE || roiType == Roi.FREELINE || roiType == Roi.RECTANGLE)) {
                IJ.error("Unsupported ROI type.");
                return false;
            }

            String prefix = group.getName();
            String name = roi.getName().toLowerCase();
            String type = roi.getRoiClass().toString();

            Point2d[] xy = CaKymoGrapher.roi2YX(roi.getRoi());
            float width = roi.getStrokeWidth();
            float[][] data = CaKymoGrapher.createRawKymoGraph(imp, xy, width);

            if (data == null) {
                return false;
            }

            NDSize shape = mkSize(data);
            float[] flat = flatten(data);

            DataArray da = block.createDataArray(prefix + ".kymo." + name, "kymo." + type, DataType.Float, shape);
            da.setData(flat, shape, new NDSize(2, 0));
            da.setLabel("calcium signal");

            CaImage.Metadata metadata = img.getMetadata();

            boolean correctMetadata =
                    metadata != null &&
                    metadata.planeInfo != null &&
                    metadata.planeInfo.length == data[0].length;

            if (correctMetadata) {
                SampledDimension dim0 =  da.appendSampledDimension(1.0);
                dim0.setLabel("location");

                RangeDimension dim = da.appendRangeDimension(metadata.ticks());
                dim.setUnit("s");
                dim.setLabel("time");
            }

            group.addDataArray(da);

            DataArray rd = saveRoiData(roi);
            group.addDataArray(rd);

            DataArray roiXY = saveRoiXY(xy, prefix + ".roi.xy." + name, "roi.xy." + name);
            group.addDataArray(roiXY);

            return true;
        }

        DataArray saveRoiData(CaRoiBox roi) {

            String type = "roi.pt." + roi.getRoiClass().toString();
            String name = group.getName() + ".roi.pt." + roi.getName().toLowerCase();

            DataArray da;

            int roiType = roi.getType();
            if (roiType == Roi.FREELINE || roiType == Roi.POLYLINE) {
                FloatPolygon p = roi.getFloatPolygon();
                da = mkRoiDataArray(p, name, type);
            } else if (roiType == Roi.RECTANGLE) {
                da = mkRoiDataArray(roi.getRoi().getBounds(), name, type);
            } else {
                return null;
            }

            float strokeWidth = roi.getStrokeWidth();

            Section root = block.getMetadata();
            Section meta = root.createSection(name, "roi");

            meta.createProperty("type", new Value(roiType));
            meta.createProperty("strokeWidth", new Value(strokeWidth));

            Color c = roi.getRoi().getStrokeColor();
            meta.createProperty("color", new Value(c.getRGB()));

            da.setMetadata(meta);

            return da;
        }

        DataArray mkRoiDataArray(FloatPolygon p, String name, String type) {

            int n = p.npoints;
            float[] x = p.xpoints;
            float[] y = p.ypoints;

            DataArray da = block.createDataArray(name, type, DataType.Float, new NDSize(new int[]{2, n}));
            da.setData(x, new NDSize(new int[]{1, n}), new NDSize(new int[]{0, 0}));
            da.setData(y, new NDSize(new int[]{1, n}), new NDSize(new int[]{1, 0}));

            SetDimension dm = da.appendSetDimension();
            dm.setLabels(Arrays.asList("x", "y"));
            SetDimension dn = da.appendSetDimension();

            return da;
        }

        DataArray mkRoiDataArray(Rectangle r, String name, String type) {
            int[] pts = new int[]{r.x, r.y, r.width, r.height};

            DataArray da = block.createDataArray(name, type, DataType.Float, new NDSize(new int[]{4}));
            da.setData(pts, new NDSize(new int[]{4}), new NDSize(new int[]{0}));

            SetDimension dm = da.appendSetDimension();
            dm.setLabels(Arrays.asList("x", "y", "width", "height"));

            return da;
        }

        DataArray saveRoiXY(Point2d[] xy, String name, String type) {
            final int n = xy.length;
            double[] data = new double[2*n];
            NDSize shape = new NDSize(new int[]{2, n});
            DataArray da = block.createDataArray(name, type, DataType.Float, shape);

            for (int i = 0; i < n; i++) {
                final Point2d p = xy[i];
                data[i  ] = p.x;
                data[i+n] = p.y;
            }

            da.setData(data, shape, new NDSize(2, 0));
            SetDimension dm = da.appendSetDimension();
            dm.setLabels(Arrays.asList("x", "y"));
            SetDimension dn = da.appendSetDimension();

            return da;
        }


    }

    private static float[] flatten(float[][] data) {
        int m = data.length;
        int n = data[0].length;

        float[] flat = new float[m*n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, flat, n*i, n);
        }

        return flat;
    }

    private static NDSize mkSize(float[][] data) {
        if (data == null) {
            return new NDSize();
        }

        return new NDSize(new int[]{data.length, data[0].length});
    }

    private void exportImage(CaImage img) {
        String name = img.getName();

        fireTaskProgress(imageProcessed, imagesTotal, "Exporting image " + name);

        Group g = block.createGroup(name, "image.ca");
        Section im = meta.createSection(name, "image.ca");
        g.setMetadata(im);

        im.createProperty("creation_time", new Value(img.getCTime()));
        im.createProperty("original_path", new Value(img.getFilePath()));
        im.createProperty("filename", new Value(img.getFileName()));
        im.createProperty("trial", new Value(img.getTrial()));
        im.createProperty("condition", new Value(img.getCondition()));

        CaImage.Metadata metadata = img.getMetadata(true);
        if (metadata != null) {
            im.createProperty("channels", new Value(metadata.channels));

            int n = metadata.planeInfo.length;
            NDSize shape = new NDSize(new int[]{n});
            DataArray cd = block.createDataArray(name + ".channels", "channel", DataType.Int8, shape);
            int[] ci = new int[n];
            for (int i = 0; i < n; i++) {
                ci[i] = metadata.planeInfo[i].channel;
            }
            cd.setData(ci, shape, new NDSize(new int[]{0}));
            cd.setLabel("channel");
            RangeDimension dim = cd.appendRangeDimension(metadata.ticks());
            dim.setLabel("time");
            dim.setUnit("s");

            g.addDataArray(cd);
        }

        ImagePlus ip =  img.openImage();

        if (ip == null) {
            imageProcessed++;
            IJ.error("Could not open Image: " + name);
            return;
        }
        
        List<CaRoiBox> rois = img.listRois();

        KymoExporter exporter = new KymoExporter(img, g, ip);
        rois.forEach(exporter::exportKymo);

        imageProcessed++;
    }

    @Override
    public void runTask() throws Exception {
        fireTaskProgress(imageProcessed, imagesTotal, "Starting export...");

        File nixfd = File.open(path, FileMode.Overwrite);

        // everything will be on that block
        block = nixfd.createBlock(neuron.getName(), "neuron");

        // save the per-neuron metadata
        meta = nixfd.createSection(neuron.getName(), "neuron");
        block.setMetadata(meta);

        String value;
        if ((value = neuron.getComment()) != null && value.length() > 0) {
            meta.createProperty("comment", new Value(value));
        }

        if ((value = neuron.getAge()) != null && value.length() > 0) {
            meta.createProperty("age", new Value(value));
        }

        if ((value = neuron.getRegion()) != null && value.length() > 0) {
            meta.createProperty("region", new Value(value));
        }

        if ((value = neuron.getCondition()) != null && value.length() > 0) {
            meta.createProperty("condition", new Value(value));
        }

        if ((value = neuron.getSex()) != null && value.length() > 0) {
            meta.createProperty("sex", new Value(value));
        }

        if ((value = neuron.getSubregion()) != null && value.length() > 0) {
            meta.createProperty("subregion", new Value(value));
        }

        if ((value = neuron.getLitter()) != null && value.length() > 0) {
            meta.createProperty("litter", new Value(value));
        }

        ArrayList<CaImage> images = neuron.getImages(true);
        imagesTotal = images.size();

        images.forEach(this::exportImage);

        meta.close();
        block.close();

        nixfd.close();
    }

}

