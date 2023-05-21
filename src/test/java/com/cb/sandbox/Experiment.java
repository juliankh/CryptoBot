package com.cb.sandbox;

import com.cb.admin.ShellCommandRunner;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

public class Experiment {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException {
        ShellCommandRunner shellCommandRunner = new ShellCommandRunner();
        String[] commands = shellCommandRunner.commands("source ~/.crypto_bot_profile");
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        String rawOutput = CharStreams.toString(new InputStreamReader(process.getInputStream(), Charsets.UTF_8));
        System.out.println(exitCode);
        System.out.println("Output:\n" + rawOutput);
        System.out.println("-------------------------------------------------");

        System.getenv().entrySet().forEach(System.out::println);
        //System.out.println(System.getenv("CRYPTO_BOT_LOG_DIR"));
    }

}
