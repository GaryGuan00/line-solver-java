package jline.lang;

import java.io.Serializable;

public class Element implements Serializable {
    protected String name;

    public Element(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
