package de.lmu.bio.calcium;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;
import de.lmu.bio.calcium.ui.CaDialogUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CaNewNeuronDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox templateSelector;
    private JTextField tfName;
    private JFormattedTextField tfAge;
    private JComboBox cbSex;
    private JComboBox cbRegion;
    private JList fileList;
    private JEditorPane tfComment;
    private JButton addFiles;
    private JButton removeFiles;
    private JTextField tfPrefix;
    private JComboBox cbCondition;
    private JComboBox cbSubRegion;
    private JTextField litter;
    private JComboBox cbExperiment;

    CaTemplateModel templateModel;

    private ArrayList<File> files = new ArrayList<File>();
    private FileModel fileModel;

    private CaNeuron neuron = null;
    private CaSettings settings;

    public CaNewNeuronDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        templateModel = new CaTemplateModel();
        templateSelector.setModel(templateModel);

        fileModel = new FileModel();
        fileList.setModel(fileModel);

        //Action Listeners
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFiles();
            }
        });
        removeFiles.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeFiles();
            }
        });

        //prefs
        settings = CaSettings.get();

        String defaultTemplate = settings.getTemplate();
        int idx = templateModel.findFileByName(defaultTemplate);
        templateSelector.setSelectedIndex(idx);

        //all done
        pack();
    }

    private void onOK() {
        CaTemplate template = getSelectedTemplate();
        String name = tfName.getText();
        if (name.equals("")) {
            name = "Unnamed";
        }

        if (template != null) {
            try {
                template.parse();
                neuron = template.loadNeuron();
                neuron.setName(name);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else {
            neuron = new CaNeuron(name);
        }

        neuron.setComment(tfComment.getText());
        neuron.setCommonFilePrefix(tfPrefix.getText());
        neuron.setRegion(cbRegion.getSelectedItem().toString());
        neuron.setSex(cbSex.getSelectedItem().toString());
        neuron.setAge(tfAge.getText());
        neuron.setCondition(cbCondition.getSelectedItem().toString());
        neuron.setSubregion(cbSubRegion.getSelectedItem().toString());
        neuron.setLitter(litter.getText());
        neuron.setExperiment(cbExperiment.getSelectedItem().toString());

        for (File f : files) {
            CaImage image = new CaImage(f.getAbsolutePath());
            neuron.add(image);
        }

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public CaNeuron getNeuron() {
        return neuron;
    }

    public CaTemplate getSelectedTemplate() {
        int index = templateSelector.getSelectedIndex();
        File templateFile = templateModel.getFileAt(index);

        if (templateFile == null) {
            return null;
        }

        return new CaTemplate(templateFile);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(9, 3, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(8, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(10, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Name");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(81, 16), null, 0, false));
        tfName = new JTextField();
        panel3.add(tfName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        tfAge = new JFormattedTextField();
        panel3.add(tfAge, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Age");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(81, 16), null, 0, false));
        cbSex = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("");
        defaultComboBoxModel1.addElement("male");
        defaultComboBoxModel1.addElement("female");
        cbSex.setModel(defaultComboBoxModel1);
        panel3.add(cbSex, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Sex");
        panel3.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(81, 16), null, 0, false));
        cbRegion = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("MSO");
        defaultComboBoxModel2.addElement("unspecified");
        cbRegion.setModel(defaultComboBoxModel2);
        panel3.add(cbRegion, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Region");
        panel3.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(81, 16), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Comment");
        panel3.add(label5, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(81, 16), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(81, 22), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(8, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(-1, 80), null, 0, false));
        tfComment = new JEditorPane();
        scrollPane1.setViewportView(tfComment);
        final JLabel label6 = new JLabel();
        label6.setText("Condition");
        panel3.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbCondition = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Control");
        defaultComboBoxModel3.addElement("Noisebox");
        cbCondition.setModel(defaultComboBoxModel3);
        panel3.add(cbCondition, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Sub-Region");
        panel3.add(label7, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Litter");
        panel3.add(label8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbSubRegion = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("lateral");
        defaultComboBoxModel4.addElement("medial");
        defaultComboBoxModel4.addElement("N/A");
        cbSubRegion.setModel(defaultComboBoxModel4);
        panel3.add(cbSubRegion, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        litter = new JTextField();
        panel3.add(litter, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Experiment");
        panel3.add(label9, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbExperiment = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel5 = new DefaultComboBoxModel();
        defaultComboBoxModel5.addElement("Development");
        defaultComboBoxModel5.addElement("Pharmacology");
        cbExperiment.setModel(defaultComboBoxModel5);
        panel3.add(cbExperiment, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setBackground(new Color(-1));
        label10.setFont(new Font(label10.getFont().getName(), label10.getFont().getStyle(), 16));
        label10.setForeground(new Color(-16777216));
        label10.setText("New Neuron");
        contentPane.add(label10, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(-1, 40), 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:grow,top:3dlu:noGrow,center:d:noGrow"));
        contentPane.add(panel4, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, new Dimension(-1, 20), 0, false));
        final JSeparator separator1 = new JSeparator();
        CellConstraints cc = new CellConstraints();
        panel4.add(separator1, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.FILL));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, cc.xy(3, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
        final JLabel label11 = new JLabel();
        label11.setText("Files");
        panel4.add(label11, cc.xywh(1, 1, 1, 3));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:9px:grow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:max(m;4px):noGrow"));
        contentPane.add(panel5, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Template");
        panel5.add(label12, cc.xywh(1, 1, 1, 3));
        final JSeparator separator2 = new JSeparator();
        panel5.add(separator2, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.FILL));
        final Spacer spacer4 = new Spacer();
        panel5.add(spacer4, cc.xy(3, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
        templateSelector = new JComboBox();
        panel5.add(templateSelector, cc.xy(3, 5));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:grow,top:3dlu:noGrow,center:d:grow"));
        contentPane.add(panel6, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, new Dimension(-1, 20), 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Label");
        panel6.add(label13, cc.xywh(1, 1, 1, 3));
        final JSeparator separator3 = new JSeparator();
        panel6.add(separator3, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.FILL));
        final Spacer spacer5 = new Spacer();
        panel6.add(spacer5, cc.xy(3, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
        final JScrollPane scrollPane2 = new JScrollPane();
        contentPane.add(scrollPane2, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, 200), null, 0, false));
        fileList = new JList();
        scrollPane2.setViewportView(fileList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        contentPane.add(toolBar1, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addFiles = new JButton();
        addFiles.setText("+");
        toolBar1.add(addFiles);
        removeFiles = new JButton();
        removeFiles.setText("-");
        toolBar1.add(removeFiles);
        final JLabel label14 = new JLabel();
        label14.setText("Common prefix");
        contentPane.add(label14, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfPrefix = new JTextField();
        contentPane.add(tfPrefix, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        label6.setLabelFor(cbCondition);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    //File List
    //-----------------------------

    protected class FileModel extends AbstractListModel {
        @Override
        public int getSize() {
            return files.size();
        }

        @Override
        public Object getElementAt(int index) {
            File f = files.get(index);
            return f.getName();
        }

        public void addFiles(File[] newFiles) {

            int start = files.size();
            int end = start + newFiles.length;

            for (File f : newFiles) {
                files.add(f);
            }

            fireIntervalAdded(this, start, end);
        }

        public void removeFiles(int start, int end) {

            for (int i = end; i >= start; i--) {
                files.remove(i);
            }

            fireIntervalRemoved(this, start, end);
        }

        public String getCommonPrefix() {
            String name = files.get(0).getName();
            int prefixLen = name.length();
            for (int i = 1; i < files.size() && prefixLen > 0; i++) {
                String curName = files.get(i).getName();

                int minLen = Math.min(curName.length(), name.length());
                int j;
                for (j = 0; j < minLen; j++) {
                    if (name.charAt(j) != curName.charAt(j))
                        break;
                }

                if (j < prefixLen) {
                    prefixLen = j;
                }
            }

            if (prefixLen == 0) {
                return "";
            }

            return name.substring(0, prefixLen);
        }
    }


    private void addFiles() {
        boolean init = files.size() == 0;

        File[] files = CaDialogUtils.getImageFiles(this);
        if (files == null || files.length == 0)
            return;

        fileModel.addFiles(files);

        if (!init)
            return;


        String name = tfName.getName();
        if (name == null || name.length() == 0) {
            //Set the neuron name based on the date
            if (CaSettings.get().getMakeNameFromFolder()) {
                tfName.setText(files[0].getParentFile().getName());
            } else {
                long lm = files[0].lastModified();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Date date = new Date(lm);
                name = sdf.format(date);
                tfName.setText(name);
            }
        }

        //Set the common file prefix
        String prefix = fileModel.getCommonPrefix();
        tfPrefix.setText(prefix);

    }

    public void removeFiles() {
        int index[] = fileList.getSelectedIndices();

        if (index == null || index.length == 0)
            return;

        int start = index[index.length - 1];
        int end = index[index.length - 1];

        for (int i = 1; i < index.length; i++) {
            int cur = index[(index.length - (1 + i))];

            if (Math.abs(cur - end) > 1) {
                fileModel.removeFiles(start, end);
                end = cur;
            }

            start = cur;
        }

        fileModel.removeFiles(start, end);
    }

    public static void main(String[] args) {
        CaNewNeuronDialog dialog = new CaNewNeuronDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
