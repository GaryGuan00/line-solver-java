package jline.lang;

import java.io.Serializable;

public class NodeAttribute implements Serializable {
    private boolean ishost;
    private int idx;

    public NodeAttribute(){

    }

    public int getIdx() {
        return idx;
    }

    public boolean getIsHost() {
        return ishost;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setIsHost(boolean ishost) {
        this.ishost = ishost;
    }
}
