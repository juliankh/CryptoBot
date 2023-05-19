package com.cb.sandbox.shell;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;

public class ShellExp {

    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("pwd");
        Process process = pb.start();
        int exitCode = process.waitFor();
        String output = CharStreams.toString(new InputStreamReader(process.getInputStream(), Charsets.UTF_8));
        System.out.println(output);
        System.out.println(exitCode);
    }

}
