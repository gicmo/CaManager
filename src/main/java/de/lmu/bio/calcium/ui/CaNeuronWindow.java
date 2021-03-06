package de.lmu.bio.calcium.ui;

import de.lmu.bio.calcium.*;
import de.lmu.bio.calcium.io.*;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.model.CaRoiBox;
import de.lmu.bio.calcium.model.CaTreeNode;
import de.lmu.bio.calcium.tools.CaAutoAligner;
import de.lmu.bio.calcium.tools.CaDriftEstimator;
import de.lmu.bio.calcium.tools.CaKymoGrapher;
import de.lmu.bio.calcium.tools.CaRoiCloner;
import de.lmu.bio.calcium.utils.CaAlgorithms;
import de.lmu.bio.calcium.utils.CaImageUtils;
import ij.*;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.process.FloatProcessor;
import org.netbeans.swing.outline.OutlineModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CaNeuronWindow extends JFrame implements CaOutline.Delegate, ImageListener, WindowListener, FocusListener {

    protected CaOutline outline;
    protected DefaultTreeModel treeModel;
    protected MenuBar menuBar;
    protected JPopupMenu neuronPopup;

    HashMap<CaImage, CaImageWindow> windowMap = new HashMap<CaImage, CaImageWindow>();
    CaSettings settings;

    public CaNeuronWindow() {
        super("Neuron");

        settings = CaSettings.get();

        CaNeuron neuron = new CaNeuron("Unnamed");
        treeModel =  new DefaultTreeModel(neuron);
        outline = new CaOutline(treeModel);
        setNeuron(neuron);

        getContentPane().add(new JScrollPane(outline), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setSize(450, 400);
        setVisible(true);

        DataRender dataProvider = new DataRender();

        outline.setDelegate(this);
        outline.setRenderDataProvider(dataProvider);
        ImagePlus.addImageListener(this);

        //
        JToolBar toolbar = new JToolBar();
        JButton addFiles = new JButton("+");
        addFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFiles();
            }
        });
        toolbar.add(addFiles);
        JButton removeFiles = new JButton("-");
        removeFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeFiles();
            }
        });
        toolbar.add(removeFiles);

        getContentPane().add(toolbar, BorderLayout.SOUTH);

        //
        addFocusListener(this);
        addWindowListener(this);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        menuBar = createMenu();
        setMenuBar(menuBar);

        neuronPopup = createOutlinePopupMenu();

        //
        ImageJ ij = IJ.getInstance();
        if (ij!=null) {
            Image img = ij.getIconImage();
            if (img!=null)
                try {setIconImage(img);} catch (Exception e) {
                    System.err.print(e);
                }
        }

        pack();
        WindowManager.addWindow(this);

    }

    @MenuEntry(entryid = 23)
    public void showAbout() {
        CaAbout dlg = new CaAbout();
        dlg.pack();
        dlg.setVisible(true);
    }

    @MenuEntry(entryid = 5)
    public void removeFiles() {
        int rows[] = outline.getSelectedRows();

        OutlineModel model = outline.getOutlineModel();
        Arrays.sort(rows);

        for (int i = rows.length; i > 0; i--) {
            int row = rows[i - 1];
            int rowInModel = outline.convertRowIndexToModel(row);
            Object o = model.getValueAt(rowInModel, 0);

            // only remove images
            if (!(o instanceof CaImage))
                continue;

            CaTreeNode node = (CaTreeNode) o;
            treeModel.removeNodeFromParent(node);
        }
    }

    @MenuEntry(entryid = 4)
    public void addFiles() {

        File[] files = CaDialogUtils.getImageFiles(this);

        if (files == null || files.length == 0)
            return;

        CaNeuron neuron = (CaNeuron) treeModel.getRoot();
        for (File f : files) {
            CaImage image = new CaImage(f.getAbsolutePath());
            //neuron.add(image); //FIXME: insert at current selection
            treeModel.insertNodeInto(image, neuron, 0);
        }
    }

    public CaNeuron getNeuron() {
        return (CaNeuron) treeModel.getRoot();
    }

    public void setNeuron(CaNeuron neuron) {
        if (neuron == null)
            return;

        treeModel =  new DefaultTreeModel(neuron);
        outline.setTreeModel(treeModel);
        treeModel.reload();
        setTitle("[Ca] Neuron: " + neuron.getName());
    }

    public CaImageWindow getImagePlus(CaImage img) {
        if (windowMap.containsKey(img)) {
            return windowMap.get(img);
        }

        System.err.println("Image not open!");
        return null;
    }

    public void showImage(CaImage image) {
        synchronized (this) {
            CaImageWindow wnd = getImagePlus(image);

            if (wnd == null) {
                wnd = CaImageWindow.createWindow(image, treeModel);

                if (wnd != null) {
                    windowMap.put(image, wnd);
                    treeModel.nodeChanged(image);
                }
            }

            if (wnd != null) {
                WindowManager.setWindow(wnd);
                wnd.toFront();
            }
        }
    }

    public CaImage findClosestRoi(CaImage target) {
        long tmatch = Long.MAX_VALUE;
        CaImage found = null;

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        Enumeration<DefaultMutableTreeNode> en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();
            if (!(node instanceof CaImage)) {
                continue;
            }
            CaImage img = (CaImage) node;

            if (img.getRoiCount() < 1)
                continue;

            long tdelta = Math.abs(target.getMTime() - img.getMTime());

            if (tmatch > tdelta) {
                tmatch = tdelta;
                found = img;
            }
        }

        return found;
    }

    @MenuEntry(entryid = 3)
    public void editNeuronProperties() {
        CaNeuron neuron = getNeuron();
        CaNeuronEditor editor = new CaNeuronEditor(this, neuron);
        editor.setVisible(true);
        treeModel.nodeChanged(neuron);
    }


    @MenuEntry(entryid = 0)
    public void create() {

        CaNewNeuronDialog dlg = new CaNewNeuronDialog();
        dlg.setVisible(true);
        CaNeuron neuron = dlg.getNeuron();

        if (neuron != null) {
            setNeuron(neuron);
        }
    }

    @MenuEntry(entryid = 2)
    public void save() {

        CaNeuron neuron = getNeuron();
        String filename = neuron.getName() + ".nix";

        FileDialog fd = new FileDialog(this, "Save Neuron", FileDialog.SAVE);
        fd.setMultipleMode(false);
        String path = settings.getDataDir().getAbsolutePath();

        if (!new java.io.File(path).exists()) {
            path = OpenDialog.getLastDirectory();
        }

        fd.setFile(filename);
        fd.setDirectory(path);
        fd.setFilenameFilter((dir, name) -> name.endsWith(".nix"));

        fd.setVisible(true);

        File[] ret = fd.getFiles();
        if (ret == null || ret.length == 0)
            return;

        path = ret[0].getAbsolutePath();

        try {
            startTask();

            CaTask exporter;

            if (path.endsWith(".hdf5")) {
                exporter = new CaNIXExporter(path, neuron);
            } else {
                exporter = new CaNIXExporter(path, neuron);
            }

            CaProgressDialog dlg = new CaProgressDialog(exporter);
            exporter.start();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
            finishTask(exporter); //FIXME exception

        } catch (Exception e) {
            e.printStackTrace();
            //IJ.log(e.getStackTrace().toString());
        }
    }

    @MenuEntry(entryid = 1)
    public void load() {

        FileDialog fd = new FileDialog(this, "Open File", FileDialog.LOAD);
        fd.setMultipleMode(false);
        fd.setDirectory(settings.getDataDir().getAbsolutePath());
        fd.setFilenameFilter((dir, name) -> name.endsWith(".h5") || name.endsWith(".hdf5") || name.endsWith(".nix"));

        fd.setVisible(true);

        File[] ret = fd.getFiles();
        if (ret == null || ret.length == 0)
            return;

        String path = ret[0].getAbsolutePath();

        CaImporter importer;

        if (path.endsWith("h5") || path.endsWith(".hdf5")) {
            importer = new CaH5Importer(path);
        } else {
            importer = new CaNIXImporter(path);
        }

        startTask();
        CaProgressDialog dlg = new CaProgressDialog(importer);
        importer.start();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        CaNeuron neuron = importer.getNeuron();
        setNeuron(neuron);
        finishTask(importer);
    }

    private CaImage askUserForImage(String title, String label) {
        CaNeuron neuron = getNeuron();
        GenericDialog gd = new GenericDialog(title +  " - Select Image");
        ArrayList<CaImage> images = neuron.getImages(true);

        if (images.size() < 1) {
            IJ.showMessage("No Images", "No images :( Need some!");
            return null;
        }

        String[] names = images.stream().map(CaImage::getName).toArray(String[]::new);
        gd.addChoice(label + " image: ", names, names[0]);
        gd.showDialog();

        if (gd.wasCanceled()) {
            return null;
        }

        int idx = gd.getNextChoiceIndex();
        return images.get(idx);
    }

    @MenuEntry(entryid = 24)
    public void copyROIs() {

        CaImage source = askUserForImage("Synchronize ROIs", "Source");

        if (source == null) {
            return;
        }

        System.err.print("Using source image: " + source.getName());

        GenericDialog gd = new GenericDialog("Synchronize ROIs - Select Source ROI");

        String[] rois = source.listRois().stream().map(CaRoiBox::getName).toArray(String[]::new);
        boolean[] bs = new boolean[rois.length];
        Boolean[] l = source.listRois().stream().map(CaRoiBox::isBackground).toArray(Boolean[]::new);
        IntStream.range(0, l.length).forEach(x -> bs[x] = l[x]);

        gd.addCheckboxGroup(rois.length, 1, rois, bs);
        gd.showDialog();

        if (gd.wasCanceled())
            return;

        ArrayList<CaImage> images = getNeuron().getImages(true);

        Point shift = new Point(0, 0);
        for (String id : rois) {
            boolean cloneRoi = gd.getNextBoolean();
            if (!cloneRoi) {
                continue;
            }
            for (CaImage img : images) {
                if (img == source) {
                    continue;
                }

                Roi r = source.getRoi(id);
                img.setRoi(CaRoiCloner.cloneMove(r, shift), id);
                treeModel.nodeStructureChanged(img);
            }
        }
    }

    @MenuEntry(entryid = 25)
    public void addRectROI() {

        CaImage target = askUserForImage("Add Rect ROI", "Target");

        if (target == null) {
            return;
        }

        CaNeuron neuron = getNeuron();

        GenericDialog gd = new GenericDialog("Add Rect ROI - Rect configuration");

        java.util.List<String> nl = target.listRois().stream().map(CaRoiBox::getName).collect(Collectors.toList());;

        //must be index zero for the code below to work correctly
        nl.add(0, "<Image>");

        String[] names = nl.toArray(new String[nl.size()]);

        gd.addNumericField("Rect size", 10, 0);
        gd.addChoice("Relative to: ", names, names[names.length > 1 ? 1 : 0]);
        gd.addNumericField("Offset  X", 10, 0);
        gd.addNumericField("Offset  Y", 10, 0);
        gd.showDialog();

        if (gd.wasCanceled())
            return;

        int size = (int) gd.getNextNumber();

        int idx = gd.getNextChoiceIndex();
        int x = (int) gd.getNextNumber();
        int y = (int) gd.getNextNumber();

        //x, y are relative to an ROI
        if (idx > 0) {
            CaRoiBox rb = target.getRoiBox(names[idx]);
            Rectangle r = rb.getRoi().getBounds();
            x += r.x + r.width / 2.0;
            y += r.y + r.height / 2.0;
        }

        Roi roi = new Roi(x, y, size, size);
        CaRoiBox box = target.maybeAddRoi(roi);
        if (box != null) {
            treeModel.nodeStructureChanged(target);
        }
    }

    @MenuEntry(entryid = 26)
    public void duplicateROI() {

        CaImage target = askUserForImage("Duplicate ROIs", "Target");

        if (target == null) {
            return;
        }

        GenericDialog gd = new GenericDialog("Duplicate ROIs - Select Source ROI");
        System.err.print("Using target image: " + target.getName());

        String[] names = target.listRois().stream().map(CaRoiBox::getName).toArray(String[]::new);
        gd.addChoice("Source ROIs: ", names, names[0]);
        gd.addNumericField("Offset  X", 10, 0);
        gd.addNumericField("Offset  Y", 10, 0);

        gd.showDialog();
        if (gd.wasCanceled())
            return;

        int idx = gd.getNextChoiceIndex();
        Roi old = target.getRoi(names[idx]);

        int x = (int) gd.getNextNumber();
        int y = (int) gd.getNextNumber();

        Roi rnew = CaRoiCloner.cloneMove(old, new Point(x, y));
        rnew.setName("NewRoi");
        CaRoiBox box = target.maybeAddRoi(rnew);
        if (box != null) {
            treeModel.nodeStructureChanged(target);
        }
    }

    @MenuEntry(entryid = 6)
    public void alignROIs() {
        try {
            int slice = 1;

            ImagePlus curImg = WindowManager.getCurrentImage();
            if (curImg != null) {
                slice = curImg.getCurrentSlice();
            }

            GenericDialog gd = new GenericDialog("Auto Align");
            String[] orders = {"Descending", "Ascending"};
            gd.addChoice("Order: ", orders, orders[0]);
            gd.addNumericField("Slice", slice, 0);
            gd.showDialog();
            if (gd.wasCanceled())
                return;

            int idx = gd.getNextChoiceIndex();
            slice = (int) gd.getNextNumber();

            startTask();
            CaAutoAligner aligner = new CaAutoAligner(treeModel, idx == 0, slice);
            CaProgressDialog dlg = new CaProgressDialog(aligner);
            aligner.start();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
            finishTask(aligner);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @MenuEntry(entryid = 9)
    public void estimateIntraDrift() {
        try {

            startTask();
            CaDriftEstimator estimator = new CaDriftEstimator(treeModel);
            CaProgressDialog dlg = new CaProgressDialog(estimator);
            estimator.start();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
            finishTask(estimator);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @MenuEntry(entryid = 11)
    public void estimateIntraDriftSingle() {
        try {
            CaImage image;

            int rows[] = outline.getSelectedRows();

            if (rows.length > 1) {
                IJ.showMessage("Can only correct one image at a time");
                return;
            }

            OutlineModel model = outline.getOutlineModel();
            int rowInModel = outline.convertRowIndexToModel(rows[0]);
            Object o = model.getValueAt(rowInModel, 0);

            // only remove images
            if (!(o instanceof CaImage)) {
                IJ.showMessage("Selection not an Image");
                return;
            }

            image = (CaImage) o;

            startTask();
            CaDriftEstimator estimator = new CaDriftEstimator(treeModel, image);
            CaProgressDialog dlg = new CaProgressDialog(estimator);
            estimator.start();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
            finishTask(estimator);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @MenuEntry(entryid = 10)
    public void correctIntraDrift() {
        try {

            CaImage image;

            int rows[] = outline.getSelectedRows();

            if (rows.length > 1) {
                IJ.showMessage("Can only correct one image at a time");
                return;
            }

            OutlineModel model = outline.getOutlineModel();
            int rowInModel = outline.convertRowIndexToModel(rows[0]);
            Object o = model.getValueAt(rowInModel, 0);

            // only remove images
            if (!(o instanceof CaImage)) {
                IJ.showMessage("Selection not an Image");
                return;
            }

            image = (CaImage) o;
            showImage(image);

            IJ.runPlugIn("de.lmu.bio.calcium.CaDriftCorrector_", "");

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @MenuEntry(entryid = 12)
    public void calcdffMenu() {
        calcdFF(true);
    }

    public void calcdFF(boolean checkWindow) {
        try {


            CaImage image = null;

            if (checkWindow) {
                ImageWindow curImg = WindowManager.getCurrentWindow();
                if (curImg instanceof CaImageWindow) {
                    CaImageWindow imgWnd = (CaImageWindow) curImg;
                    image = imgWnd.getCaImage();
                }
            }

            if (image == null) {
                int rows[] = outline.getSelectedRows();
                if (rows.length > 1) {
                    IJ.showMessage("Can only correct one image at a time");
                    return;
                }

                OutlineModel model = outline.getOutlineModel();
                int rowInModel = outline.convertRowIndexToModel(rows[0]);
                Object o = model.getValueAt(rowInModel, 0);

                // only remove images
                if (o == null || !(o instanceof CaImage)) {
                    IJ.showMessage("Selection not an Image");
                    return;
                }

                image = (CaImage) o;
            }

            Roi fg = image.getRoiFg();
            if (fg == null) {
                IJ.showMessage("Need to have foreground Roi done");
                return;
            }

            ImagePlus imp;
            if (windowMap.containsKey(image)) {
                CaImageWindow wnd = windowMap.get(image);
                imp  = wnd.getImagePlus();
            } else {
                imp = image.openImage();
            }

            int baseline = 10;
            int start = 1;
            int tNumber = imp.getImageStackSize();
            GenericDialog gd = new GenericDialog("Calc dF/F");
            gd.addNumericField("t-Basline [N]", baseline, 0);
            gd.addNumericField("t-Start   [#]", start, 0);
            gd.addNumericField("t-Length  [N]", tNumber, 0);
            gd.showDialog();
            if (gd.wasCanceled())
                return;

            baseline = (int) gd.getNextNumber();
            start = (int) gd.getNextNumber() - 1; //human to computer
            tNumber = (int) gd.getNextNumber();

            float[][] kymo = CaKymoGrapher.createRawKymoGraph(imp, fg);
            float[][] dff = CaAlgorithms.calcDFF(kymo, baseline, start, tNumber);
            FloatProcessor fp = new FloatProcessor(dff);
            ImagePlus kymoImp = new ImagePlus("dF/F:" + image.getName(), fp);
            fp.setColorModel(CaImageUtils.getHeatmap());
            kymoImp.show();


        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }


    @MenuEntry(entryid = 13)
    public void traceToRoi() {
        try {

            ImageWindow curWnd = WindowManager.getCurrentWindow();
            curWnd.toFront();
            IJ.runPlugIn("de.lmu.bio.calcium.CaTraceToRoi_", "");

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @MenuEntry(entryid = 7)
    public void showSettings() {
        CaSettingsDialog dlg = new CaSettingsDialog();
        dlg.setVisible(true);
    }

    @MenuEntry(entryid = 8)
    public void doQuit() {
        System.exit(0);
    }

    //------------------------------------------------------
    private void startTask() {
        outline.setEnabled(false);
    }

    private void finishTask(CaTask task) {
        if (!task.isSuccess()) {
            IJ.showMessage("Error! " + task.getError());
        }
        outline.setEnabled(true);
    }
    //------------------------------------------------------
    public void showKymoGraphFg() {
        IJ.showMessage("Not implemented currently. Sorry.");
    }

    // de.lmu.bio.calcium.ui.CaOutline.Delegate
    //------------------------------------------------------
    @Override
    public void doubleClicked(int row, Object object) {

        if (!outline.isEnabled())
            return;

        synchronized (this) {
            if (object instanceof CaImage) {
                CaImage image = (CaImage) object;
                showImage(image);
            } else if (object instanceof CaNeuron) {
                CaNeuron neuron = (CaNeuron) object;
                CaNeuronEditor editor = new CaNeuronEditor(this, neuron);
                editor.setVisible(true);
                treeModel.nodeChanged(neuron);
            } else if (object instanceof CaRoiBox) {

                CaRoiBox box = (CaRoiBox) object;
                CaImage img = (CaImage) box.getParent();

                ImagePlus imp;
                CaImageWindow wnd = getImagePlus(img);
                if (wnd != null) {
                    imp = wnd.getImagePlus();
                } else {
                    imp = img.openImage();
                }

                float[][] data = CaKymoGrapher.createRawKymoGraph(imp, box.getRoi());
                FloatProcessor fp = new FloatProcessor(data);
                ImagePlus kymoImp = new ImagePlus("Kymo Graph:" + img.getName() +
                        " [" +  box.getName() + "]", fp);
                fp.setColorModel(CaImageUtils.getHeatmap());
                kymoImp.show();
            }
        }
    }

    @Override
    public void popupTriggered(CaOutline outline, Point p) {
        int row = outline.rowAtPoint(p);
        OutlineModel m = outline.getOutlineModel();
        Object o = m.getValueAt(row, 0);

        if (!(o instanceof CaImage))
            return;

        outline.setRowSelectionInterval(row, row);
        System.err.println(o.getClass());
        neuronPopup.show(outline, p.x, p.y);
    }

    // ImageListener
    //------------------------------------------------------
    @Override
    public void imageClosed(ImagePlus imp) {
        Set<Map.Entry<CaImage, CaImageWindow>> kvs = windowMap.entrySet();
        for (Map.Entry<CaImage, CaImageWindow> e : kvs) {
            if (e.getValue().getImagePlus() == imp) {
                System.out.println("Removing image");
                windowMap.remove(e.getKey());
                treeModel.nodeChanged(e.getKey());
                break;
            }
        }
    }

    @Override
    public void imageOpened(ImagePlus imp) { }
    @Override
    public void imageUpdated(ImagePlus imp) { }

    //------------------------------------------------------------------------------------------------------------------
    protected class DataRender extends CaOutline.CaOutlineDataProvider {

        @Override
        public boolean isHtmlDisplayName(Object object) {
            return (object instanceof CaImage) && windowMap.containsKey(object);
        }

        @Override
        public String getDisplayName(Object object) {
            if (object instanceof CaImage && windowMap.containsKey(object)) {
                return "<b>" + object.toString() + "</b>";
            }
            return null;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public void windowActivated(WindowEvent e) {
        if (IJ.isMacintosh() && IJ.getInstance() !=null) {
            IJ.wait(10); // may be needed for Java 1.4 on OS X
            setMenuBar(menuBar);
        }
        WindowManager.setWindow(this);
    }

    public void focusGained(FocusEvent e) {
        WindowManager.setWindow(this);
    }
    public void windowClosing(WindowEvent e) {
        System.err.println("Window closing!");

    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void focusLost(FocusEvent e) {}

    // Menu
    //-----------------------------------------------------------
    public void createMenuItem(JPopupMenu parent, String name, OLPopupMenuHandler handler) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(handler);
        parent.add(item);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MenuEntry {
        int entryid() default -1;
    }

    public class MenuBuilder {
        Class<CaNeuronWindow> klass;
        HashMap<Integer, Method> map = new HashMap<Integer, Method>();
        MenuBar bar;
        Menu    parent;


        public MenuBuilder(MenuBar _bar) {
            klass = CaNeuronWindow.class;

            Method[] allMethods = klass.getDeclaredMethods();
            for (Method m : allMethods) {
                MenuEntry me = m.getAnnotation(MenuEntry.class);
                if (me != null)  {
                    map.put(me.entryid(), m);
                }
            }

            bar = _bar;
        }

        public ReflectionMenuHandler createHandler(int id) {
            Method m = map.get(id);
            return new ReflectionMenuHandler(m);
        }

        public void createMenuItem(String name, int id) {
            MenuItem item = new MenuItem(name);
            item.addActionListener(createHandler(id));
            parent.add(item);
        }

        public void createSeparator() {
            parent.addSeparator();
        }

        public void createMenu(String name) {
            parent = new Menu(name);
            bar.add(parent);
        }
    }


    class ReflectionMenuHandler implements ActionListener {
        Method method = null;

        ReflectionMenuHandler(Method _method) {
            method = _method;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                method.invoke(CaNeuronWindow.this);
            } catch (Exception ex) {
                ex.printStackTrace(); //FIXME
            }
        }
    }

    public MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();

        MenuBuilder mb = new MenuBuilder(menuBar);
        mb.createMenu("File");
        mb.createMenuItem("New", 0);
        mb.createMenuItem("Open", 1);
        mb.createMenuItem("Save", 2);
        mb.createSeparator();
        mb.createMenuItem("About", 23);
        mb.createSeparator();
        mb.createMenuItem("Quit", 8);
        mb.createMenu("Neuron");
        mb.createMenuItem("Properties", 3);
        mb.createSeparator();
        mb.createMenuItem("Add files", 4);
        mb.createMenuItem("Remove files", 5);
        mb.createMenu("Tools");
        mb.createMenuItem("Synchronize ROIs", 24);
        mb.createMenuItem("Trace to ROI", 13);
        mb.createMenuItem("Align ROIs", 6);
        mb.createMenuItem("Add Rectangular ROI", 25);
        mb.createMenuItem("Duplicate ROI", 26);
        mb.createSeparator();
        mb.createMenuItem("Estimate drift [All]", 9);
        mb.createMenuItem("Estimate drift [Single]", 11);
        mb.createMenuItem("Correct drift", 10);
        mb.createSeparator();
        mb.createMenuItem("Calc dF/F", 12);
        mb.createSeparator();
        mb.createMenuItem("Settings", 7);

        return menuBar;
    }


    public JPopupMenu createOutlinePopupMenu() {
        OLPopupMenuHandler handler = new OLPopupMenuHandler();

        JPopupMenu popup = new JPopupMenu();
        createMenuItem(popup, "calc dF/F", handler);
        return popup;
    }

    protected class OLPopupMenuHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals("calc dF/F")) {
                calcdFF(false);
            }
        }
    }

}
