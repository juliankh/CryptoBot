package com.cb.sandbox.guice;

public class CommunicatorImpl implements Communicator {

    @Override
    public void sendMessage(String message) {
        System.out.printf("sending message: " + message);
    }

}
