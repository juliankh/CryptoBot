package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbReadOnlyProvider;
import com.cb.injection.module.MainModule;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class SafetyNetMonitor {

    private static final Set<Integer> EXPECTED_EXIT_CODES = Sets.newHashSet(0, 1);

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private ShellCommandRunner shellCommandRunner;

    @Inject
    private AlertProvider alertProvider;

    public static void main(String[] args) {
        SafetyNetMonitor monitor = MainModule.INJECTOR.getInstance(SafetyNetMonitor.class);
        monitor.monitor();
    }

    public void monitor() {
        TreeMap<String, TreeSet<String>> configs = configs();
        List<String> processesNotRunning = new ArrayList<>();
        TreeMap<String, String> processesWithProblemChecking = new TreeMap<>();
        configs.forEach((token, subTokens) -> {
            log.info("-------------------------------------------------------------------------");
            Pair<Integer, List<String>> result = shellCommandRunner.currentlyRunning(token);
            int exitCode = result.getLeft();
            List<String> output = result.getRight();
            log.info("For token [" + token + "] refined output: " + (CollectionUtils.isEmpty(output) ? "<nothing>" : "\n\t" + output.parallelStream().sorted().collect(Collectors.joining("\n\t"))));
            if (!EXPECTED_EXIT_CODES.contains(exitCode)) {
                String error = "When checking if any process for token [" + token + "] is running, got unexpected exit code [" + exitCode + "]";
                processesWithProblemChecking.put(token, error);
                log.error(error);
            } else {
                if (CollectionUtils.isEmpty(subTokens)) {
                    // there are no subtokens, so only 1 process to look for as specified by the token
                    checkProcessWithoutSubTokens(token, output, processesNotRunning, processesWithProblemChecking);
                } else {
                    // subtokens exist, therefore we'll look for processes by combining token + subtoken for each subtoken
                    Set<String> outputSet = new HashSet<>(output);
                    subTokens.parallelStream().map(subToken -> token + " " + subToken).forEach(processString -> {
                        checkProcessWithSubTokens(processString, outputSet, processesNotRunning);
                    });
                }
            }
        });
        log.info("-------------------------------------------------------------------------");
        alertIfNecessary(processesNotRunning, processesWithProblemChecking);
    }

    private TreeMap<String, TreeSet<String>> configs() {
        TreeMap<String, TreeSet<String>> activeSafetyNetMap = dbReadOnlyProvider.activeSafetyNetConfigMap();
        log.info("Safety Nets:\n\t" + activeSafetyNetMap.entrySet().parallelStream()
                .map(entry -> entry.getKey() + (CollectionUtils.isNotEmpty(entry.getValue()) ? "\n\t\t" + entry.getValue().parallelStream().map(Objects::toString).sorted().collect(Collectors.joining("\n\t\t")) : ""))
                .sorted()
                .collect(Collectors.joining("\n\t")));
        return activeSafetyNetMap;
    }

    // TODO: unit test
    public void checkProcessWithoutSubTokens(String token, List<String> output, List<String> processesNotRunning, TreeMap<String, String> processesWithProblemChecking) {
        if (output.size() == 1) {
            String outputString = output.get(0);
            if (!outputString.contains(token)) {
                log.info("Process [" + token + "] is NOT running");
                processesNotRunning.add(token);
            } else {
                log.info("Process [" + token + "] is running, which is good");
            }
        } else if (output.size() == 0) {
            log.info("Process [" + token + "] is NOT running");
            processesNotRunning.add(token);
        } else {
            String error = "When checking if process [" + token + "] is running, only 1 output is expected, but got [" + output.size() + "]";
            processesWithProblemChecking.put(token, error);
            log.error(error + ":\n\t" + output.parallelStream().sorted().collect(Collectors.joining("\n\t")));
        }
    }

    // TODO: unit test
    public void checkProcessWithSubTokens(String processString, Set<String> outputSet, List<String> processesNotRunning) {
        if (outputSet.contains(processString)) {
            log.info("Process [" + processString + "] is running, which is good");
        } else {
            log.info("Process [" + processString + "] is NOT running");
            processesNotRunning.add(processString);
        }
    }

    // TODO: unit test
    public void alertIfNecessary(List<String> processesNotRunning, TreeMap<String, String> processesWithProblemChecking) {
        if (CollectionUtils.isEmpty(processesNotRunning) && MapUtils.isEmpty(processesWithProblemChecking)){
            log.info("ALL GOOD - all processes that are expected to be up, are indeed up and running.");
        } else {
            StringBuilder subjectSb = new StringBuilder();
            StringBuilder detailsSb = new StringBuilder();
            if (CollectionUtils.isNotEmpty(processesNotRunning)) {
                subjectSb.append("Some Processes are DOWN");
                detailsSb.append("Processes that are DOWN:\n\t").append(processesNotRunning.parallelStream().sorted().collect(Collectors.joining("\n\t")));
            }
            if (MapUtils.isNotEmpty(processesWithProblemChecking)) {
                if (!subjectSb.isEmpty()) {
                    subjectSb.append(" - ");
                }
                subjectSb.append("Errors While Checking Processes Running");
                if (!detailsSb.isEmpty()) {
                    detailsSb.append("\n\n");
                }
                detailsSb.append("Processes that had problem checking if they're up:\n\t").append(processesWithProblemChecking.entrySet().parallelStream().map(Objects::toString).sorted().collect(Collectors.joining("\n\t")));
            }
            String subject = subjectSb.toString();
            String details = detailsSb.toString();
            log.error(details);
            alertProvider.sendEmailAlert(subject, details);
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbReadOnlyProvider.cleanup();
    }

}
