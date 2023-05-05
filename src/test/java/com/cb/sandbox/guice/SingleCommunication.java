package com.cb.sandbox.guice;

import com.google.inject.Inject;

public class SingleCommunication {

    @Inject
    private SingleCommunicator communicator;

    public void sendMessage(String message) {
        communicator.sendMessage(message);
        System.out.println("Communicator: " + communicator.toString());
    }

}
