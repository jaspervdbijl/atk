package com.acutus.atk.util;

import com.acutus.atk.util.collection.Two;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Timer extends ArrayList{
    private List<Time> marks = new ArrayList<>();

    private Timer(String name) {
        mark(name);
    }

    public static Timer start(String name) {
        return new Timer(name);
    }

    public TimeHolders stop() {
        return marks.stream().map(a -> a.elapsed()).collect(Collectors
                .toCollection(TimeHolders::new));
    }

    public Timer mark(String name) {
        marks.add(new Time(name));
        return this;
    }

    public String toString() {
        return stop().sec();
    }

    public static interface Timeable {
        public long getDuration();
        public String sec();
        public String milli();

        String processed(int processed);
    }

    private static class Time {
        private long start = System.currentTimeMillis();
        private Long stop;
        private String name;

        public Time(String name) {
            this.name = name;
        }

        public Timeable elapsed() {
            stop = stop == null ? System.currentTimeMillis() : stop;
            return new TimeHolder(name, stop.longValue() - start);
        }

    }
    @AllArgsConstructor
    @Getter
    public static class TimeHolder implements Timeable {
        private String name;
        @Getter
        private long duration;

        public String sec() {
            return name + " took " + (duration / 1000);
        }

        public String milli() {
            return name + " took " + (duration);
        }

        @Override
        public String processed(int processed) {
            return name + ": processed " + ((duration/1000) / processed) +" per second";
        }
    }

    public static class TimeHolders extends ArrayList<Timeable> implements Timeable {

        @Override
        public long getDuration() {
            return stream().mapToLong(a -> a.getDuration()).sum();
        }

        @Override
        public String sec() {
            return stream().map(a -> a.sec()).reduce((s1,s2) -> s1+"\n"+s2).get();
        }

        @Override
        public String milli() {
            return stream().map(a -> a.milli()).reduce((s1,s2) -> s1+"\n"+s2).get();
        }


        @Override
        public String processed(int processed) {
            return stream().map(a -> a.processed(processed)).reduce((s1,s2) -> s1+"\n"+s2).get();
        }

    }
}
