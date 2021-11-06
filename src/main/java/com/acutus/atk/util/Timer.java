package com.acutus.atk.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Timer {
    private long start = System.currentTimeMillis();

    public static Timer start() {
        return new Timer();
    }

    public TimeHolder elapsed() {
        return new TimeHolder(System.currentTimeMillis() - start);
    }

    public TimeHolder processed(int processed) {
        return new TimeHolder(elapsed().duration / processed);
    }

    @AllArgsConstructor
    @Getter
    public static class TimeHolder {
        private long duration;

        public long sec() {
            return duration / 1000;
        }
    }
}
