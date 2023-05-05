package com.cb.sandbox.guice;

import com.google.inject.Inject;

public class Communication {

    @Inject
    private Communicator communicator;

    public void sendMessage(String message) {
        communicator.sendMessage(message);
    }

}
