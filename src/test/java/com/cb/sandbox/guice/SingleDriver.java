package com.cb.sandbox.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SingleDriver {

    public static void main(String[] args){
        Injector injector = Guice.createInjector(new BasicModule());
        SingleCommunication comms = injector.getInstance(SingleCommunication.class);
        SingleCommunication2 comms2 = injector.getInstance(SingleCommunication2.class);
        comms.sendMessage("hey hey");
        comms2.sendMessage("hey hey");
    }

}
