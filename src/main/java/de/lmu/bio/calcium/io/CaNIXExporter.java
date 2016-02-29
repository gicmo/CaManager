package de.lmu.bio.calcium.io;

import de.lmu.bio.calcium.CaTask;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.tools.CaKymoGrapher;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

import javax.vecmath.Point2d;
import java.util.ArrayList;

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

            Group g = b.createGroup(name, "image");

            Section im = meta.createSection(name, "image");
            im.createProperty("creation_time", new Value(img.getCTime()));
            g.setMetadata(im);

            ImagePlus ip = IJ.openImage(img.getFilePath());
            Roi roi = img.getRoiFg();
            if (roi == null) {
                continue;
            }

            Point2d[] xy = CaKymoGrapher.roi2YX(roi);
            float width = roi.getStrokeWidth();
            float[][] data = CaKymoGrapher.createRawKymoGraph(ip, xy, width);

            if (data == null) {
                continue;
            }

            int m = data.length;
            int n = data[0].length;

            float[] flat = new float[m*n];
            for (int i = 0; i < m; i++) {
                System.arraycopy(data[i], 0, flat, n*i, n);
            }

            NDSize shape = new NDSize(new int[]{m, n});
            DataArray da = b.createDataArray(name + "-kymo", "kymograph.fg", DataType.Float, shape);
            da.setData(flat, shape, new NDSize(2, 0));

            g.addDataArray(da);

        }

        fd.close();
    }


}

