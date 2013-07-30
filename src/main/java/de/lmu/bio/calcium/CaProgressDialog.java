package de.lmu.bio.calcium;

import javax.swing.*;
import java.awt.event.*;

public class CaProgressDialog extends JDialog implements CaTask.Observer {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JProgressBar progress;
    private JLabel message;
    private JLabel title;
    CaTask task;

    public CaProgressDialog(CaTask task) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

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

        this.task = task;
        this.title.setText(task.getName());
        this.message.setText("");
        task.addObserver(this);
        pack();
    }

    @Override
    public void taskProgress(int itemsProcessed, int ofTotalItems, String message) {
        this.message.setText(message);
        this.progress.setMaximum(ofTotalItems);
        this.progress.setValue(itemsProcessed);

    }

    @Override
    public void taskFinished(boolean success, Exception e) {
        dispose();
        this.task = null;
    }

    private void onCancel() {

    }

    public static void main(String[] args) {
        CaProgressDialog dialog = new CaProgressDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
