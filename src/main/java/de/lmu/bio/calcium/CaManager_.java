package de.lmu.bio.calcium;

import de.lmu.bio.calcium.ui.CaNeuronWindow;
import de.lmu.bio.calcium.utils.ResourceExtractor;
import ij.*;
import ij.plugin.PlugIn;


public class CaManager_ implements PlugIn {

    static {

        String osid = System.getProperty("os.name");
        String hdflib = null;

        if (osid.equals("Mac OS X")) {
            hdflib = "macosx/libjhdf5.jnilib";
        } else if (osid.startsWith("Windows")) {
            String archStr = System.getProperty("os.arch");
            System.err.println(archStr);
            String arch = (archStr.endsWith("64") ? "64" : "32");
            hdflib = "win" + arch + "/jhdf5.dll";
            System.err.println(hdflib);
        } else {
            IJ.showMessage("Platform not supported. Oh oh!");
        }

        ResourceExtractor extractor = new ResourceExtractor("/hdf5/"+ hdflib);

        if (!extractor.exists()) {
            extractor.extract();
        }
        System.err.println("H5 Lib path:" + extractor.getPath());
        System.setProperty("ncsa.hdf.hdf5lib.H5.hdf5lib", extractor.getPath());
    }

    // main plugin entrance function
    public void run(String arg) {
        CaNeuronWindow wnd = new CaNeuronWindow();

        if (arg.equals("new")) {
            wnd.create();
        }
    }

//    de.lmu.bio.calcium.model.CaGroup cpp = new de.lmu.bio.calcium.model.CaGroup("SIN");
//    de.lmu.bio.calcium.model.CaGroup p3 = new de.lmu.bio.calcium.model.CaGroup("AP3");
//    de.lmu.bio.calcium.model.CaGroup p4 = new de.lmu.bio.calcium.model.CaGroup("AP1");
//    neuron.add(p3);
//    neuron.add(p4);
//    p4.setAllowImages(true);
//    cpp.setAllowImages(true);
//    p3.setAllowImages(true);
//    neuron.add(cpp);
//
//    de.lmu.bio.calcium.model.CaImage image = new de.lmu.bio.calcium.model.CaImage("file:///Users/gicmo/Dropbox/Delwen - Christian/0603f/LA_ProtocolCombination_06.03.2012_18_25_26.ome.tif");
//    neuron.add(image);
//    neuron.setCommonFilePrefix("LA_ProtocolCombination_06.03.2012_");


    //------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = CaManager_.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        //ImagePlus image = IJ.openImage("file:///Users/gicmo/Pictures/Neuroscience/mri-stack.tif");
        //image.show();
        //System.err.println(url);
        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }

}

