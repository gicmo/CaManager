package de.lmu.bio.calcium.tools;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class CaKymoGrapher {
//    private CaNeuron neuron;
//    private ArrayList<CaImage> changedImages = new ArrayList<CaImage>();
//
//    public CaKymoGrapher(CaNeuron neuron) {
//        super("de.lmu.bio.calcium.tools.CaKymoGrapher");
//        this.neuron = neuron;
//    }
//
//    @Override
//    public void run() {
//         ArrayList<CaImage> images = new ArrayList<CaImage>();
//
//        Enumeration<DefaultMutableTreeNode> en = neuron.depthFirstEnumeration();
//        while (en.hasMoreElements()) {
//            DefaultMutableTreeNode node = en.nextElement();
//            if (!(node instanceof CaImage)) {
//                continue;
//            }
//            CaImage img = (CaImage) node;
//
//            if (img.getRoiBg() == null && img.getRoiFg() == null)
//                continue;
//
//            images.add(img);
//        }
//
//        for (CaImage image : images) {
//            CaKymoGraph kg = createKymoGraph(image, CaKymoGraph.Type.Foreground);
//            System.err.println("Created KG.");
//            image.add(kg);
//            changedImages.add(image);
//        }
//    }
//
//    public ArrayList<CaImage> getChangedImages() {
//        return changedImages;
//    }

    private static double[] createPlotForStack(ImageStack stack, Roi roi, int z) {

        ImageProcessor cip = stack.getProcessor(z+1);
        cip.setInterpolate(true);
        ImagePlus temp = new ImagePlus("<temp>" + z+1, cip);
        temp.setRoi(roi);
        ProfilePlot pp = new ProfilePlot(temp, false);
        return pp.getProfile();
    }

    public static void setValues(float[][] target, int x, double[] source) {
        for (int j = 0; j < source.length; j++) {
            target[j][x] = (float) source[j];
        }
    }

    public static float[][] createRawKymoGraph(ImagePlus imp, Roi roi) {
        ImageStack stack = imp.getImageStack();

        int stackSize = stack.getSize();
        double[] profile = createPlotForStack(stack, roi, 0);

        float[][] data = new float[profile.length][stackSize];
        setValues(data, 0, profile);

        for (int i = 1; i < stackSize; i++) {
            profile = createPlotForStack(stack, roi, i);
            setValues(data, i, profile);
        }
        return data;
    }

}
