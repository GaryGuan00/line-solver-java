// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.solvers;

import jline.lang.JobClass;
import jline.lang.nodes.Station;

// Class for handles for the mean performance metrics
public class SolverHandles {

  public static class Handle {

    public String type;
    public Station station;
    public JobClass jobClass;
    public boolean isDisabled;
    public boolean isTransient;
  }

  public Handle Q;
  public Handle U;
  public Handle R;
  public Handle T;
  public Handle A;
  public Handle Qt;
  public Handle Ut;
  public Handle Tt;
}
