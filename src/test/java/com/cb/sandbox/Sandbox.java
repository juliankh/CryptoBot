package com.cb.sandbox;

import java.io.File;
import java.text.NumberFormat;

public class Sandbox {

    public static void main(String[] args) {
        File f = new File("/");
        System.out.println(NumberFormat.getNumberInstance().format(f.getTotalSpace()));
        System.out.println(NumberFormat.getNumberInstance().format(f.getFreeSpace()));
        System.out.println(NumberFormat.getNumberInstance().format(f.getUsableSpace()));
    }

}
