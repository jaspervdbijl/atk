package com.acutus.atk.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Strings extends ArrayList<String> {

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
        return stream().reduce((s1,s2)-> s1+"\n"+s2).get();
    }
}
