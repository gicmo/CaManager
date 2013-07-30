package de.lmu.bio.calcium.io;

import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import ncsa.hdf.object.Group;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class CaBundleExporter extends CaH5Exporter {
    private File bundleDir;


    public CaBundleExporter(String path, CaNeuron neuron) {
        super(path, neuron);
    }

    @Override
    protected void saveImage(CaImage image, Group h5Group) throws Exception {
        super.saveImage(image, h5Group);
        File in = image.getFile();
        File out = new File(bundleDir, in.getName());

        fireTaskProgress("Copying " + in.getName());
        FileUtils.copyFile(in, out, true);
    }
}
