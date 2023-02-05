package jline.lang.constant;

import java.io.Serializable;

public enum DropStrategy implements Serializable {
    WaitingQueue,
    Drop,
    BlockingAfterService
}
