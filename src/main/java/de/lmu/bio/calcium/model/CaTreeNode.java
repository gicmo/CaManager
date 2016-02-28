package de.lmu.bio.calcium.model;

import javax.swing.tree.DefaultMutableTreeNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class CaTreeNode extends DefaultMutableTreeNode{


    public CaTreeNode(Object defaultUserObject) {
        super (defaultUserObject);
    }

    public CaTreeNode() {
        super("");
    }

    final protected transient Object propertyLock = new Object();
    private PropertyChangeSupport kvoSupport = new PropertyChangeSupport(this);

    protected void notifyPropertyChange(String name, Object oldVlaue, Object newValue) {
        kvoSupport.firePropertyChange(name, oldVlaue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized (propertyLock) {
            kvoSupport.addPropertyChangeListener(listener);
        }
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        synchronized (propertyLock) {
            kvoSupport.addPropertyChangeListener(propertyName, listener);
        }
    }
}
