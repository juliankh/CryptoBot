package com.cb.admin;

import com.cb.alert.Alerter;
import com.cb.db.DbReadOnlyProvider;
import com.cb.model.config.ProcessConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SafetyNetMonitorTest {

    @Mock
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Mock
    private Alerter alerter;

    @InjectMocks
    private SafetyNetMonitor safetyNetMonitor;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(dbReadOnlyProvider);
        Mockito.reset(alerter);
    }

    @Test
    public void alertIfNecessary_NoAlertExpected() {
        // engage test
        safetyNetMonitor.alertIfNecessary(Lists.newArrayList(), Maps.newTreeMap());

        // verify
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void alertIfNecessary_AlertExpected_SomeProcessesDown() {
        // setup
        String processDown1 = "processDown1";
        String processDown2 = "processDown2";
        List<String> processesDown = Lists.newArrayList(processDown2, processDown1);

        // engage test
        safetyNetMonitor.alertIfNecessary(processesDown, Maps.newTreeMap());

        // verify
        verify(alerter, times(1)).sendEmailAlert("Some Processes are DOWN", "Processes that are DOWN:\n\tprocessDown1\n\tprocessDown2");
    }

    @Test
    public void alertIfNecessary_AlertExpected_ErrorsWhileCheckingProcesses() {
        // setup
        String processWithError1 = "processWithError1";
        String processWithError2 = "processWithError2";
        String error1 = "Some Error 1";
        String error2 = "Some Error 2";
        TreeMap<String, String> errorMap = new TreeMap<>();
        errorMap.put(processWithError1, error1);
        errorMap.put(processWithError2, error2);

        // engage test
        safetyNetMonitor.alertIfNecessary(Lists.newArrayList(), errorMap);

        // verify
        verify(alerter, times(1)).sendEmailAlert("Errors While Checking Processes Running", "Processes that had problem checking if they're up:\n\tprocessWithError1=Some Error 1\n\tprocessWithError2=Some Error 2");
    }

    @Test
    public void alertIfNecessary_AlertExpected_SomeProcessesDownAndErrorsWhileCheckingProcesses() {
        // setup
        String processDown1 = "processDown1";
        String processDown2 = "processDown2";
        List<String> processesDown = Lists.newArrayList(processDown2, processDown1);

        String processWithError1 = "processWithError1";
        String processWithError2 = "processWithError2";
        String error1 = "Some Error 1";
        String error2 = "Some Error 2";
        TreeMap<String, String> errorMap = new TreeMap<>();
        errorMap.put(processWithError1, error1);
        errorMap.put(processWithError2, error2);

        // engage test
        safetyNetMonitor.alertIfNecessary(processesDown, errorMap);

        // verify
        verify(alerter, times(1)).sendEmailAlert("Some Processes are DOWN & Errors While Checking Processes Running", "Processes that are DOWN:\n\tprocessDown1\n\tprocessDown2\n\nProcesses that had problem checking if they're up:\n\tprocessWithError1=Some Error 1\n\tprocessWithError2=Some Error 2");
    }

    @Test
    public void checkProcessWithSubTokens_OutputSetContainsProcessString() {
        // setup
        String processString = "processString1";
        Set<String> outputSet = Sets.newHashSet(Lists.newArrayList("another", "yet another", processString));
        List<String> processesNotRunning = Lists.newArrayList();

        // engage test
        safetyNetMonitor.checkProcessWithSubTokens(processString, outputSet, processesNotRunning);

        // verify
        assertEquals(0, processesNotRunning.size());
        assertFalse(Sets.newHashSet(processesNotRunning).contains(processString));
    }

    @Test
    public void checkProcessWithSubTokens_OutputSetNotContainsProcessString() {
        // setup
        String processString = "processString1";
        Set<String> outputSet = Sets.newHashSet(Lists.newArrayList("another", "yet another"));
        List<String> processesNotRunning = Lists.newArrayList();

        // engage test
        safetyNetMonitor.checkProcessWithSubTokens(processString, outputSet, processesNotRunning);

        // verify
        assertEquals(1, processesNotRunning.size());
        assertTrue(Sets.newHashSet(processesNotRunning).contains(processString));
    }

    @Test
    public void checkProcessWithoutSubTokens_OutputSize1_ProcessFound() {
        // setup
        String token = "token1";
        List<String> output = Lists.newArrayList(token);
        List<String> processesNotRunning = Lists.newArrayList();
        TreeMap<String, String> processesWithProblemChecking = Maps.newTreeMap();

        // engage test
        safetyNetMonitor.checkProcessWithoutSubTokens(token, output, processesNotRunning, processesWithProblemChecking);

        // verify
        assertTrue(processesNotRunning.isEmpty());
        assertTrue(processesWithProblemChecking.isEmpty());
    }

    @Test
    public void checkProcessWithoutSubTokens_OutputSize1_ProcessNotFound() {
        // setup
        String token = "token1";
        List<String> output = Lists.newArrayList("some other process");
        List<String> processesNotRunning = Lists.newArrayList();
        TreeMap<String, String> processesWithProblemChecking = Maps.newTreeMap();

        // engage test
        safetyNetMonitor.checkProcessWithoutSubTokens(token, output, processesNotRunning, processesWithProblemChecking);

        // verify
        assertEquals(1, processesNotRunning.size());
        assertEquals(token, processesNotRunning.get(0));
        assertTrue(processesWithProblemChecking.isEmpty());
    }

    @Test
    public void checkProcessWithoutSubTokens_OutputSize0() {
        // setup
        String token = "token1";
        List<String> output = Lists.newArrayList();
        List<String> processesNotRunning = Lists.newArrayList();
        TreeMap<String, String> processesWithProblemChecking = Maps.newTreeMap();

        // engage test
        safetyNetMonitor.checkProcessWithoutSubTokens(token, output, processesNotRunning, processesWithProblemChecking);

        // verify
        assertEquals(1, processesNotRunning.size());
        assertEquals(token, processesNotRunning.get(0));
        assertTrue(processesWithProblemChecking.isEmpty());
    }

    @Test
    public void checkProcessWithoutSubTokens_OutputSizeMoreThen1() {
        // setup
        String token = "token1";
        List<String> output = Lists.newArrayList("output line 1", "output line 2");
        List<String> processesNotRunning = Lists.newArrayList();
        TreeMap<String, String> processesWithProblemChecking = Maps.newTreeMap();

        // engage test
        safetyNetMonitor.checkProcessWithoutSubTokens(token, output, processesNotRunning, processesWithProblemChecking);

        // verify
        assertTrue(processesNotRunning.isEmpty());
        assertEquals(1, processesWithProblemChecking.size());
        assertEquals("When checking if process [" + token + "] is running, only 1 output is expected, but got [2]", processesWithProblemChecking.get(token));
    }

    @Test
    @SneakyThrows
    public void activeProcessAndSubProcessMap() {
        // setup
        String token1 = "ActiveProcessToken1";
        String token2 = "ActiveProcessToken2";
        ProcessConfig config1 = new ProcessConfig().setId(1).setProcessToken(token1).setProcessSubToken(null).setActive(true);
        ProcessConfig config2 = new ProcessConfig().setId(2).setProcessToken(token2).setProcessSubToken("ActiveProcessSubtoken1").setActive(true);
        ProcessConfig config3 = new ProcessConfig().setId(3).setProcessToken(token2).setProcessSubToken("ActiveProcessSubtoken2").setActive(true);
        TreeMap<String, List<ProcessConfig>> activeProcessConfigMap = new TreeMap<>() {{
            put(token1, Lists.newArrayList(config1));
            put(token2, Lists.newArrayList(config2, config3));
        }};
        when(dbReadOnlyProvider.activeProcessConfigMap()).thenReturn(activeProcessConfigMap);

        // engage test
        TreeMap<String, TreeSet<String>> result = safetyNetMonitor.activeProcessAndSubProcessMap();

        // verify
        assertEquals(2, result.size());
        assertEquals(Collections.emptySet(), result.get("ActiveProcessToken1"));
        assertEquals(Sets.newTreeSet(Arrays.asList("ActiveProcessSubtoken1", "ActiveProcessSubtoken2")), result.get("ActiveProcessToken2"));
    }

}
