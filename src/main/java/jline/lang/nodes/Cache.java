package jline.lang.nodes;

import jline.lang.*;

import java.io.Serializable;

public class Cache extends StatefulNode implements Serializable {
    public Cache(Network model, String name, int itemLevelCap) {
        super(name);
    }
}
