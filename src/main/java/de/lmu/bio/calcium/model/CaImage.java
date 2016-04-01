package de.lmu.bio.calcium.model;

import de.lmu.bio.calcium.tools.CaRoiCloner;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.Opener;
import loci.common.DateTools;
import loci.common.services.ServiceFactory;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import ome.units.UNITS;
import ome.units.quantity.Time;
import org.apache.commons.io.FilenameUtils;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CaImage extends CaTreeNode {

    private File file;
    private long mtime;

    private int trial;
    private String condition;

    //live metadata
    private int nslices = -1;
    private Point drift = null;

    public CaImage(String path) {

        if (path.startsWith("file://")) {
            file = new File(path.substring(7));
        } else {
            file = new File(path);
        }

        mtime = file.lastModified();
        String filename = file.getName();
        setName(filename);
    }

    private void setName(String filename) {
        filename = stripExtension(filename);
        this.setUserObject(filename);
    }

    private static String stripExtension(String filename, int maxExLen) {
        int index = FilenameUtils.indexOfExtension(filename);
        if (index == -1 || (maxExLen > 0 && (filename.length() - (index+1)) > maxExLen)) {
            return filename;
        } else {
            String exe = filename.substring(index + 1);
            String name = filename.substring(0, index);
            if (exe.equalsIgnoreCase("tif") || exe.equalsIgnoreCase("tiff")) {
                name = stripExtension(name, 3);
            }

            return name;
        }
    }

    public static String stripExtension(String filename) {
        if (filename == null)
            return null;

        return stripExtension(filename, 5);
    }


    public String getFilePath() {
        return file.getAbsolutePath();
    }

    public String getFileName() { return file.getName(); }

    public File getFile() {
        return file;
    }

    public long getMTime() {
        return mtime;
    }

    public String getName() {
        return (String) getUserObject();
    }


    public int getNslices() {
        return nslices;
    }

    public void setNslices(int nslices) {
        this.nslices = nslices;
    }

    public Point getDrift() {
        return drift;
    }

    public void setDrift(Point drift) {
        this.drift = drift;
    }

    public long getCTime() {
        if (metadata != null && metadata.ctime > 0) {
            return metadata.ctime;
        }

        return mtime;
    }

    @Override
    public Object clone() {
        CaImage copy;
        copy = (CaImage)super.clone();
        copy.file = file;
        copy.mtime = mtime;

        return copy;
    }

    public Roi getRoi(String name) {

        CaRoiBox box = getRoiBox(name);
        if (box == null) {
            return null;
        }

        return box.getRoi();
    }

    public CaRoiBox getRoiBox(String name) {
        CaRoiBox result = null;

        if (children == null) {
            return null;
        }

        for (Object child : children) {
            if (!(child instanceof CaRoiBox)) {
                continue;
            }

            CaRoiBox box = (CaRoiBox) child;

            if (box.getName().equals(name)) {
                result = box;
                break;
            }

        }

        return result;
    }

    public java.util.List<CaRoiBox> listRois() {

        if (children == null) {
            return new ArrayList<>();
        }

        AbstractList<?> ol = (AbstractList<?>) children;

        return ol.stream()
                .filter(o -> o instanceof CaRoiBox)
                .map(b -> (CaRoiBox) b)
                .collect(Collectors.toCollection(ArrayList<CaRoiBox>::new));
    }

    public int getRoiCount() {

        if (children == null) {
            return 0;
        }

        AbstractList<?> ol = (AbstractList<?>) children;
        return (int) ol.stream().filter(o -> o instanceof CaRoiBox).count();
    }

    public void setRoi(Roi value, String name) {
        CaRoiBox box = getRoiBox(name);

        if (value != null) {
            if (box == null) {
                box = new CaRoiBox(value);
                add(box);
            } else {
                box.setRoi(value);
            }
        } else {
            if (box != null) {
                remove(box);
            }
        }
    }

    public Roi getRoiFg() {
        return getRoi("FG");
    }

    public void setRoiFg(Roi value) {
        setRoi(value, "FG");
    }

    public Roi getRoiBg() {
        return getRoi("BG");
    }

    public void setRoiBg(Roi value) {
        setRoi(value, "BG");
    }

    public void copyRois(CaImage source) {
       copyRois(source, null);
    }

    public void copyRois(CaImage source, Point shift) {
        if (source == null || source.children == null) {
            return;
        }

        for (Object child : source.children) {
            if (!(child instanceof CaRoiBox)) {
                continue;
            }

            CaRoiBox box = (CaRoiBox) child;
            Roi dolly = cloneRoi(box.getRoi(), shift);
            setRoi(dolly, box.getName());
        }
    }

    protected Roi cloneRoi(Roi source, Point shift) {

        Roi p;

        if (shift != null) {
            p  = CaRoiCloner.cloneMove(source, shift);
        } else {
            p =  (Roi) source.clone();
        }

        return p;
    }

    public int getTrial() {
        return trial;
    }

    public void setTrial(int trial) {
        this.trial = trial;
    }


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {

        TreeNode root = this.getRoot();
        String name = super.toString();
        if (name != null && root != null && root instanceof CaNeuron) {
            CaNeuron neuron = (CaNeuron) root;
            String prefix = neuron.getCommonFilePrefix();
            if (prefix != null && name.startsWith(prefix)) {
                name = name.substring(prefix.length());
            }
        }

        return name;
    }

    public ImagePlus openImage() {
        Opener imageOpener = new Opener();
        imageOpener.setSilentMode(true);
        ImagePlus imp = imageOpener.openImage(getFilePath());
        if (imp != null) {
            nslices = imp.getNSlices();
        }
        readMetadata();
        return imp;
    }

    /* "Metadata" */
    private Metadata metadata;

    public Metadata getMetadata() {
        return metadata;
    }

    public Metadata getMetadata(boolean readIfNotThere) {
        if (metadata == null && readIfNotThere) {
            readMetadata();
        }

        return metadata;
    }

    // Should probably only be used by the Importers
    public void setMetadata(Metadata md) {
        metadata = md;
    }

    public boolean haveMetadata() {
        return metadata != null;
    }


    public class PlaneInfo {
        public int timePoint;
        public int channel;
        public int focal;
        public double deltaT;
    }

    public class Metadata {
        public long ctime;

        public PlaneInfo[] planeInfo;

        public int channels;
        public int timePoints;

        public double[] ticks() {

            if (planeInfo == null) {
                return new double[0];
            }

            double[] ticks = new double[metadata.planeInfo.length];
            for (int i = 0; i < ticks.length; i++) {
                ticks[i] = metadata.planeInfo[i].deltaT;
            }

            return ticks;
        }

    }


    public boolean readMetadata() {
        metadata = new Metadata();

        try {

            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            IMetadata meta = service.createOMEXMLMetadata();

            IFormatReader reader = new ImageReader();
            reader.setMetadataStore(meta);
            reader.setId(this.getFilePath());

            //TODO: what if we have more then 1 series?
            reader.setSeries(0);
            final int series = reader.getSeries();

            if (meta == null) {
                return false;
            }

            metadata.channels = reader.getSizeC();
            metadata.timePoints = reader.getSizeT();

            if (meta.getImageAcquisitionDate(series) != null) {
                String creationDate = meta.getImageAcquisitionDate(series).getValue();
                if (creationDate != null) {
                    metadata.ctime = DateTools.getTime(creationDate, DateTools.ISO8601_FORMAT);
                }
            }

            final int planeCount = meta.getPlaneCount(series);
            metadata.planeInfo = new PlaneInfo[planeCount];

            // not 100% sure the logic is correct here,
            //  seems to be for our files, but need to read the bio-formats
            //  docs
            for (int i = 0; i < planeCount; i++) {
                PlaneInfo ci = metadata.planeInfo[i] = new PlaneInfo();

                ci.channel = meta.getPlaneTheC(series, i).getValue();
                ci.focal = meta.getPlaneTheZ(series, i).getValue();
                ci.timePoint = meta.getPlaneTheT(series, i).getValue();

                meta.getPlaneDeltaT(series, i);

                Time deltaT = meta.getPlaneDeltaT(series, i);
                if (deltaT == null) {
                    continue;
                }

                ci.deltaT =  deltaT.value(UNITS.S).doubleValue();
            }

        } catch (Exception e) {
            e.printStackTrace();
            metadata = null;
            return false;
        }

        return true;
    }

}
