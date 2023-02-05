package jline.lang;

import java.io.Serializable;

public class NetworkElement implements Serializable {
    protected String name;
    public NetworkElement(String neName) {
        this.name = neName;
    }

    public String getName() { return this.name; }
}
