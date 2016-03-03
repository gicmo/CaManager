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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.process.FloatPolygon;
import org.g_node.nix.*;

public class CaNIXExporter extends CaTask {
    private CaNeuron neuron;
    private String path;
    protected int imagesTotal = 0;
    protected int imageProcessed = 0;

    public CaNIXExporter(String path, CaNeuron neuron) {
        super("Exporting to NIX @ " + path);
        this.neuron = neuron;
        this.path = path;
    }

    //

    class KymoExporter {
        private Block block;
        private Group group;
        private ImagePlus imp;

        public KymoExporter(Block block, Group g, ImagePlus imp) {
            this.block = block;
            this.group = g;
            this.imp = imp;
        }

        public boolean exportKymo(CaRoiBox roi) {
            int roiType = roi.getType();
            if (!(roiType == Roi.POLYLINE || roiType == Roi.FREELINE)) {
                IJ.error("Unsupported ROI type.");
                return false;
            }

            String prefix = group.getName();
            String name = roi.getName().toLowerCase();

            Point2d[] xy = CaKymoGrapher.roi2YX(roi.getRoi());
            float width = roi.getStrokeWidth();
            float[][] data = CaKymoGrapher.createRawKymoGraph(imp, xy, width);

            if (data == null) {
                return false;
            }

            NDSize shape = mkSize(data);
            float[] flat = flatten(data);

            DataArray da = block.createDataArray(prefix + ".kymo." + name, "kymo." + name, DataType.Float, shape);
            da.setData(flat, shape, new NDSize(2, 0));

            //FIXME: dimensions

            group.addDataArray(da);

            DataArray rd = saveRoiData(roi);
            group.addDataArray(rd);

            DataArray roiXY = saveRoiXY(xy, prefix + ".roi.xy." + name, "roi.xy." + name);
            group.addDataArray(roiXY);

            return true;
        }

        public DataArray saveRoiData(CaRoiBox roi) {

            String type = "roi.pt." + roi.getName().toLowerCase();
            String name = group.getName() + type;

            int roiType = roi.getType();
            float strokeWidth = roi.getStrokeWidth();
            FloatPolygon p = roi.getFloatPolygon();

            int n = p.npoints;
            float[] x = p.xpoints;
            float[] y = p.ypoints;

            DataArray da = block.createDataArray(name, type, DataType.Float, new NDSize(new int[]{2, n}));
            da.setData(x, new NDSize(new int[]{1, n}), new NDSize(new int[]{0, 0}));
            da.setData(y, new NDSize(new int[]{1, n}), new NDSize(new int[]{1, 0}));

            SetDimension dm = da.appendSetDimension();
            dm.setLabels(Arrays.asList("x", "y"));
            SetDimension dn = da.appendSetDimension();

            Section root = block.getMetadata();
            Section meta = root.createSection(name, "roi");

            meta.createProperty("type", new Value(roiType));
            meta.createProperty("strokeWidth", new Value(strokeWidth));

            return da;
        }

        public DataArray saveRoiXY(Point2d[] xy, String name, String type) {
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

    static float[] flatten(float[][] data) {
        int m = data.length;
        int n = data[0].length;

        float[] flat = new float[m*n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, flat, n*i, n);
        }

        return flat;
    }

    static NDSize mkSize(float[][] data) {
        if (data == null) {
            return new NDSize();
        }

        return new NDSize(new int[]{data.length, data[0].length});
    }

    @Override
    public void runTask() throws Exception {
        fireTaskProgress(imageProcessed, imagesTotal, "Starting export...");

        File fd = File.open(path, FileMode.Overwrite);

        // everything will be on that block
        Block b = fd.createBlock(neuron.getName(), "neuron");

        // save the per-neuron metadata
        Section meta = fd.createSection(neuron.getName(), "neuron");
        b.setMetadata(meta);

        String value;
        if ((value = neuron.getComment()) != null) {
            meta.createProperty("comment", new Value(value));
        }

        //FIXME: convert to string
        //if ((value = neuron.getAge()) != null) {
        //    meta.createProperty("age", new Value(value));
        //}

        if ((value = neuron.getRegion()) != null) {
            meta.createProperty("region", new Value(value));
        }

        ArrayList<CaImage> images = neuron.getImages(true);
        imagesTotal = images.size();

        for (CaImage img : images) {
            String name = img.getName();

            fireTaskProgress(imageProcessed, imagesTotal, "Exporting image " + name);

            Group g = b.createGroup(name, "image.ca");

            Section im = meta.createSection(name, "image.ca");
            im.createProperty("creation_time", new Value(img.getCTime()));
            g.setMetadata(im);

            ImagePlus ip = IJ.openImage(img.getFilePath());

            if (ip == null) {
                IJ.error("Could not open Image: " + name);
                continue;
            }

            List<CaRoiBox> rois = img.listRois();

            KymoExporter exporter = new KymoExporter(b, g, ip);
            rois.forEach(exporter::exportKymo);

        }

        fd.close();
    }




}

