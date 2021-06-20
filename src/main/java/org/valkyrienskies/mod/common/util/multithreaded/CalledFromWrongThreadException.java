package org.valkyrienskies.mod.common.util.multithreaded;

/**
 * This exception is thrown when certain code is called from the wrong thread.
 */
public class CalledFromWrongThreadException extends RuntimeException {

    public CalledFromWrongThreadException() {
        super("Wrong thread calling code: " + Thread.currentThread());
    }

}
