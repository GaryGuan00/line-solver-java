package jline.lang;


import jline.lang.distributions.Distribution;
import org.apache.commons.lang3.SerializationUtils;

public class ItemEntry extends Entry{
    protected int cardinality;
    protected Distribution popularity;

    public ItemEntry(String name, LayeredNetwork model, int cardinality, Distribution distribution) {
        super(model, name);
        this.cardinality = cardinality;
        if(distribution.isDiscrete()){
            this.popularity = SerializationUtils.clone(distribution);
        }
    }

    @Override
    public  void on(Task newParent){
        newParent.addEntry(this);
        this.parent = newParent;
    }
}
