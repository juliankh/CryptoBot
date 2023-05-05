package com.cb.sandbox.guice;

import com.google.inject.AbstractModule;

public class BasicModule extends AbstractModule {

    @Override
    protected void configure() {
        //bind(Communicator.class).to(CommunicatorImpl.class);
        bind(SingleCommunication.class);
    }

}
