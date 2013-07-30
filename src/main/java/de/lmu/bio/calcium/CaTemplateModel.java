package de.lmu.bio.calcium;

import javax.swing.*;
import java.io.File;

/**
* Created with IntelliJ IDEA.
* User: gicmo
* Date: 3/20/13
* Time: 2:09 PM
* To change this template use File | Settings | File Templates.
*/ //Template List
//-----------------------------
public class CaTemplateModel extends AbstractListModel implements ComboBoxModel {
    File[] templates;
    String selection;

    public CaTemplateModel() {
        templates = CaTemplate.listTemplates();

    }

    @Override
    public int getSize() {
        return 1 + (templates != null ? templates.length : 0);
    }

    @Override
    public Object getElementAt(int index) {

        if (index == 0) {
            return null;
        }

        return templates[index-1].getName();
    }

    public File getFileAt(int index) {

        if (index < 1) {
            return null;
        }

        return templates[index-1];
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (String) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selection;
    }

    public int findFileByName(String name) {
        if (name == null)
            return -1;

        //ignore entry 0 which represents no template
        for (int i = 0; i < getSize()-1; i++) {
            if (templates[i].getName().equals(name)) {
                return i+1;
            }
        }

        return -1;
    }
}
