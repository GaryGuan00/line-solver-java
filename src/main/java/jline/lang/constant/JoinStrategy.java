package jline.lang.constant;

import java.io.Serializable;

public enum JoinStrategy implements Serializable {
	STD,
	PARTIAL,
	Quorum,
	Guard
}
