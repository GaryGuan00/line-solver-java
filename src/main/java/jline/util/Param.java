package jline.util;

import java.io.Serializable;

public class Param implements Serializable {
    protected String name;
    protected Object value;
    public Param(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}
