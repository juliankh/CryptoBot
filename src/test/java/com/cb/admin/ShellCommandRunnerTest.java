package com.cb.admin;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShellCommandRunnerTest {

    @Test
    public void processDescription() {
        ShellCommandRunner shellCommandRunner = new ShellCommandRunner();
        String processToken = "KrakenOrderBookBridgeDriver";
        assertEquals("KrakenOrderBookBridgeDriver SOL-USD 05", shellCommandRunner.processDescription("  501 76309 76222   0  2:26PM ttys001    3:59.65 /usr/bin/java -cp ./CryptoBot-1.0-SNAPSHOT-jar-with-dependencies.jar com.cb.driver.kraken.KrakenOrderBookBridgeDriver SOL-USD 05", "KrakenOrderBookBridgeDriver"));
        assertEquals("KrakenOrderBookBridgeDriver", shellCommandRunner.processDescription("  501 76309 76222   0  2:26PM ttys001    3:59.65 /usr/bin/java -cp ./CryptoBot-1.0-SNAPSHOT-jar-with-dependencies.jar com.cb.driver.kraken.KrakenOrderBookBridgeDriver", "KrakenOrderBookBridgeDriver"));
    }

}
