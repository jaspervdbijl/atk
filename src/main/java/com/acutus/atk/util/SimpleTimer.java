package com.acutus.atk.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleTimer {
    private Long lastTime;

    public SimpleTimer mark() {
        lastTime = System.currentTimeMillis();
        return this;
    }

    public void mark(String name) {
        log.info(name + " {}",System.currentTimeMillis() - lastTime);
        lastTime = System.currentTimeMillis();
    }

}
