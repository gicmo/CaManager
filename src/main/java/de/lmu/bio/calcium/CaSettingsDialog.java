package de.lmu.bio.calcium;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class CaSettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtDataDir;
    private JLabel lblDataDir;
    private JButton btnChooseDataDir;
    private JComboBox cbTemplate;

    private CaSettings settings;
    private CaTemplateModel templateModel;

    public CaSettingsDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


        settings = CaSettings.get();

        File dir = settings.getDataDir();
        txtDataDir.setText(dir.getAbsolutePath());

        //Action handling
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
        btnChooseDataDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelectDir();
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


        templateModel = new CaTemplateModel();
        cbTemplate.setModel(templateModel);

        String defaultTemplate = settings.getTemplate();
        int idx = templateModel.findFileByName(defaultTemplate);
        cbTemplate.setSelectedIndex(idx);

        this.pack();
    }

    private void onSelectDir() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle("Select data directory");
        fileChooser.setToolTipText("Select default directory for open and save dialogs");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int ret = fileChooser.showDialog(this, "Select directory");
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String path = fileChooser.getSelectedFile().getAbsolutePath();
        txtDataDir.setText(path);

    }

    private void onOK() {
        settings.setDataDir(new File(txtDataDir.getText()));

        int index = cbTemplate.getSelectedIndex();
        File templateFile = templateModel.getFileAt(index);
        settings.setTemplate(templateFile);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        CaSettingsDialog dialog = new CaSettingsDialog();

        dialog.setVisible(true);
        System.exit(0);
    }
}
