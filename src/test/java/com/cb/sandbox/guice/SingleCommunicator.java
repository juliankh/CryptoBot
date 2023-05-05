package com.cb.sandbox.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SingleCommunicator {

    @Inject
    protected MessageModifier modifier;

    public void sendMessage(String message) {
        System.out.println("sending message: " + modifier.modify(message));
        System.out.println("MessageModifier: " + modifier.toString());
    }

}
