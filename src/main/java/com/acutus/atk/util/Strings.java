package com.acutus.atk.util;

import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor
public class Strings extends ArrayList<String> {

    public Strings(Collection<String> collection) {
        addAll(collection);
    }

    public static Strings asList(String... strings) {
        return new Strings(Arrays.asList(strings));
    }

    /**
     * return all the indexes of strings that contains the text
     * @param text
     * @return
     */
    public List<Integer> indexesOfContains(String text) {
        return IntStream.range(0,size()).filter(i -> get(i).contains(text))
                .boxed().collect(Collectors.toList());
    }

    public Optional<Integer> firstIndexesOfContains(String text) {
        List<Integer> indexes = indexesOfContains(text);
        return indexes.isEmpty()?Optional.empty():Optional.of(indexes.get(0));
    }

    public Strings replace(String oldV, String newV) {
        return stream().map(s -> s != null?s.replace(oldV,newV):null)
                .collect(Collectors.toCollection(Strings::new));
    }

    @Override
    public String toString() {
        return toString("\n");
    }

    public String toString(String del) {
        return stream().reduce((s1,s2)-> s1+del+s2).get();
    }

    public Strings prepend(String value) {
        return new Strings(stream().map(s -> value + s).collect(Collectors.toList()));
    }

    public Strings append(String value) {
        return new Strings(stream().map(s -> s + value).collect(Collectors.toList()));
    }

    public Strings plus(String... values) {
        return plus(Arrays.asList(values));
    }

    public Strings plus(Collection<String> values) {
        Strings strings = new Strings(this);
        strings.addAll(values);
        return strings;
    }


}
