package de.lmu.bio.calcium;

import de.lmu.bio.calcium.model.CaImage;
import de.lmu.bio.calcium.model.CaNeuron;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
        neuron.setSex (cbSex.getSelectedItem().toString());
        Object ageString = cbSex.getSelectedItem().toString();
        if (!ageString.equals("")) {
            double age = Double.parseDouble(ageString.toString());
            neuron.setAge(age);

        }
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
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setDialogTitle("Select Image files");
        fileChooser.setToolTipText("Select the image files you want to add to the new neuron");
        fileChooser.setSelectedFile(settings.getDataDir());
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "TIFF Files", "tiff", "tif"); //FIXME
        fileChooser.setFileFilter(filter);
        int ret = fileChooser.showOpenDialog(this);

        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        boolean init = files.size() == 0;
        File[] files = fileChooser.getSelectedFiles();
        if (files == null || files.length == 0)
            return;

        fileModel.addFiles(files);

        if (!init)
            return;

        //Set the neuron name based on the date
        String name = tfName.getName();
        if (name == null || name.length() == 0) {
            long lm = files[0].lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date = new Date(lm);
            name = sdf.format(date);
            tfName.setText(name);
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
