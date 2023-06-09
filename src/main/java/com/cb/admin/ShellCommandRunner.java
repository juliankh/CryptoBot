package com.cb.admin;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Singleton;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Singleton
public class ShellCommandRunner {

    public Pair<Integer, List<String>> currentlyRunning(String token) {
        String[] commands = commands(token);
        log.info("Will run shell command: [" + String.join(" ", commands) + "]");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            String rawOutput = CharStreams.toString(new InputStreamReader(process.getInputStream(), Charsets.UTF_8));
            log.info("For token [" + token + "] -- Exit code: [" + exitCode + "]; Output: " + (StringUtils.isBlank(rawOutput) ? "<nothing>" : "\n" + rawOutput));
            String[] outputLines = StringUtils.split(rawOutput, "\n");
            List<String> output = Arrays.stream(outputLines).parallel().map(outputLine -> processDescription(outputLine, token)).sorted().toList();
            return Pair.of(exitCode, output);
        } catch (Exception e) {
            throw new RuntimeException("Problem checking if process(s) [" + token + "] currently running", e);
        }
    }

    public String processDescription(String outputLine, String processToken) {
        String[] lineParts = StringUtils.splitByWholeSeparator(outputLine, processToken);
        String contentAfterProcessToken = lineParts.length > 1 ? lineParts[lineParts.length - 1] : "";
        return processToken + contentAfterProcessToken;
    }

    public String[] commands(String token) {
        return new String[]{"/bin/bash", "-c", "/bin/ps -ef | grep -v grep | grep java | grep CryptoBot | grep '" + token + "'"};
    }

}
