package de.lmu.bio.calcium;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CaAbout extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;

    public CaAbout() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
// add your code here
        dispose();
    }

    public static void main(String[] args) {
        CaAbout dialog = new CaAbout();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
