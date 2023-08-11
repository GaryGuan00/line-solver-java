package jline.solvers.jmt;

public class DocumentSectionPair {
    public Object simDoc;  // Replace Object with the actual type
    public Object section;  // Replace Object with the actual type

    public DocumentSectionPair(Object simDoc, Object section){
        this.simDoc = simDoc;
        this.section = section;
    }

    public Object getSimDoc() {
        return simDoc;
    }

    public Object getSection() {
        return section;
    }
}