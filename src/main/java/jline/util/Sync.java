package jline.util;

import java.util.HashMap;
import java.util.Map;

import jline.lang.NetworkEvent;

public class Sync {

	//Index starts from 0
	public Map<Integer, NetworkEvent> active;
	public Map<Integer, NetworkEvent> passive;
	
	public Sync() {
		active = new HashMap<Integer, NetworkEvent>();
		passive = new HashMap<Integer, NetworkEvent>();
	}
}