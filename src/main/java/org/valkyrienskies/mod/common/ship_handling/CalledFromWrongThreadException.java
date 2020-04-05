package org.valkyrienskies.mod.common.ship_handling;

/**
 * This exception is thrown when certain code is called from the wrong thread.
 **/

public class CalledFromWrongThreadException extends RuntimeException {

    public CalledFromWrongThreadException(String msg) {
        super(msg);
    }

}
