package com.cb.common;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;

public class GeneralDelegate {

    public String hostname() {
        return hostname(Integer.MAX_VALUE);
    }

    @SneakyThrows
    public String hostname(int lengthLimit) {
        String hostname = InetAddress.getLocalHost().getHostName();
        return StringUtils.substring(hostname, 0, lengthLimit);
    }

}
