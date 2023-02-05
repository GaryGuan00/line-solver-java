
package jline.lang;

import java.util.HashMap;
import java.util.Map;

public class Entry extends LayeredNetworkElement{
    protected Task parent;
    protected Map<Integer,String> replyActivity = new HashMap();//TODO:K/V type
    private double openArrivalRate;
    protected JLineMatrix scheduling = new JLineMatrix(0,0,0);

    public Entry(LayeredNetwork model, String name) {
        super(name);
        this.openArrivalRate = 0.0;
        model.entries.put(model.entries.size(),this);
        this.model = model;
    }

    public  void on(Task newParent){//TODO
        newParent.addEntry(this);
        this.parent = newParent;
    }
}

