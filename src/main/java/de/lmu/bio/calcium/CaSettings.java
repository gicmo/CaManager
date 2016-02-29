package de.lmu.bio.calcium;

import java.io.File;
import java.util.prefs.Preferences;


public class CaSettings {
    private Preferences prefs;
    static CaSettings singleton = null;

    public static synchronized CaSettings get()  {
        if (singleton == null) {
            singleton = new CaSettings();
        }

        return singleton;
    }

    private CaSettings() {
        prefs = Preferences.userNodeForPackage (CaManager_.class);
    }


    public File getDataDir() {
        String defaultDir = prefs.get("DataDir", System.getProperty("user.home"));
        return new File(defaultDir);
    }

    public void setDataDir(File f) {
        String path = null;
        if (f != null) {
            path = f.getAbsolutePath();
        }

        prefs.put("DataDir", path);
    }

    public String getTemplate() {
        return prefs.get("DefaultTemplate", "");
    }

    public void setTemplate(CaTemplate template) {
        String value = template != null ? template.getFilename() : "";
        prefs.put("DefaultTemplate", value);
    }

    public void setTemplate(File template) {
        String value = template != null ? template.getName() : "";
        prefs.put("DefaultTemplate", value);
    }
}
