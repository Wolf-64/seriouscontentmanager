package com.wlf.common.util;

import javafx.concurrent.WorkerStateEvent;

import java.io.IOException;

public class ErrorHandler {
    public static void defaultWorkerStateEventError(WorkerStateEvent wse) {
        //LOGGER.error(wse.getSource().getException().getLocalizedMessage(), wse.getSource().getException());
        try {
            throw new IOException(wse.getSource().getException());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
