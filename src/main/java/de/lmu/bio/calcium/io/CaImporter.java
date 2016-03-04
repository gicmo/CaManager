package de.lmu.bio.calcium.io;

import de.lmu.bio.calcium.CaTask;
import de.lmu.bio.calcium.model.CaNeuron;

public abstract class CaImporter extends CaTask {

    public CaImporter(String name) {
        super(name);
    }

    public abstract CaNeuron getNeuron();
}
