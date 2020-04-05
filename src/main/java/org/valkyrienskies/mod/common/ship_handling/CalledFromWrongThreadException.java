package org.valkyrienskies.mod.common.ship_handling;

/**
 * This exception is thrown when certain code is called from the wrong thread.
 **/

class CalledFromWrongThreadException extends IllegalArgumentException {

    public CalledFromWrongThreadException(String msg) {
        super(msg);
    }

}
