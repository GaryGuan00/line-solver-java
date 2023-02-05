package jline.lang.sections;

import java.io.Serializable;

import jline.lang.NetworkElement;

public abstract class Section extends NetworkElement implements Serializable {
    String className;
    public Section(String className) {
        super("jline.Section");
        this.className = className;
    }
}
