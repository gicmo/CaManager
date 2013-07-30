package de.lmu.bio.calcium.model;

public class CaGroup extends CaTreeNode {

    private boolean allowImages;
    public CaGroup (String Name) {
        super (Name);
    }

    public CaGroup (String name, boolean allowImages) {
        super(name);
        this.allowImages = allowImages;
    }

    public String getName() {
        return (String) getUserObject();
    }

    public boolean allowImages() {
        return allowImages;
    }

    public void setAllowImages(boolean value) {
        allowImages = value;
    }
}
