package jline.lang.sections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jline.lang.*;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class Dispatcher extends OutputSection implements Serializable {
    public Dispatcher(List<JobClass> customerClasses) {
        super("jline.Dispatcher");
    }
}
