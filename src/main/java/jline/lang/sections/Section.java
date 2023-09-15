package jline.lang.sections;

import java.io.Serializable;

import jline.lang.NetworkElement;

public abstract class Section extends NetworkElement implements Serializable {
    String className;
    public Section(String className) {
        super("Section");
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
