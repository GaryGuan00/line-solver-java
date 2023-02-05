package jline.lang.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jline.util.Param;

public class Process implements Serializable {
    protected String name;
    protected int numParam;
    protected List<Param> params;
    public Process(String name, int numParam) {
        this.name = name;
        this.numParam = numParam;
        this.params = new ArrayList<Param>();
        for (int i = 0; i < this.numParam; i++) {
            this.params.add(new Param("NULL_PARAM", null));
        }
    }

    public int getNumParams() {
        return this.numParam;
    }

    public void setParam(int id, String name, Object value) {
        this.params.set(id-1, new Param(name, value));
    }

    public Param getParam(int id) {
        return this.params.get(id-1);
    }
}