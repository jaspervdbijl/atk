package com.acutus.atk.util;

public class RunException extends RuntimeException {

    public RunException(String message,Object ... args) {
        super(String.format(message,args));
    }
}
