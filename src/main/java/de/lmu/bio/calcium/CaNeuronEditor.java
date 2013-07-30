package de.lmu.bio.calcium;

import de.lmu.bio.calcium.model.CaNeuron;

import javax.swing.*;
import java.awt.event.*;

public class CaNeuronEditor extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField age;
    private JTextField name;
    private JComboBox gender;
    private JComboBox region;
    private JEditorPane editorPane1;
    private CaNeuron neuron;

    public CaNeuronEditor(JFrame parent, CaNeuron neuron) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.neuron = neuron;
        name.setText(neuron.getName());
        age.setText(Double.toString(neuron.getAge()));
        pack();
    }

    private void onOK() {
        //FIXME ensure name.getText() makes sense
        neuron.setName(name.getText());
        neuron.setRegion((String) region.getSelectedItem());
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
