package com.acutus.atk.util;

import com.acutus.atk.util.call.CallOneRet;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.acutus.atk.util.AtkUtil.handle;

@NoArgsConstructor
public class Strings extends ArrayList<String> {

    public Strings(Collection<String> collection) {
        addAll(collection);
    }

    public Strings(String collection[]) {
        addAll(Arrays.asList(collection));
    }

    public static Strings asList(String... strings) {
        return new Strings(Arrays.asList(strings));
    }

    public int indexOfIgnoreCase(String value) {
        Optional<Integer> index = IntStream.range(0,size()).filter(i -> value.toUpperCase().equals(get(i).toUpperCase()))
                .boxed().findFirst();
        return index.isPresent() ? index.get() : -1;
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

    public OptionalInt firstIndexesOfContains(String text) {
        List<Integer> indexes = indexesOfContains(text);
        return indexes.isEmpty() ? OptionalInt.empty() : OptionalInt.of(indexes.get(0));
    }

    public Strings replace(String oldV, String newV) {
        return stream().map(s -> s != null?s.replace(oldV,newV):null)
                .collect(Collectors.toCollection(Strings::new));
    }

    public Strings replace(CallOneRet<String,String> call) {
        return stream().map(s -> handle(() -> call.call(s))).collect(Collectors.toCollection(Strings::new));

    }

    public boolean equalsIgnoreOrderIgnoreCase(Strings values) {
        return values.size() == size() && !stream().filter(s -> !values.containsIgnoreCase(s)).findAny().isPresent();
    }

    public boolean equalsIgnoreOrder(Strings values) {
        return values.size() == size() && !stream().filter(s -> !values.contains(s)).findAny().isPresent();
    }

    @Override
    public Strings clone() {
        return new Strings(this);
    }

    /**
     * @param filter
     * @return a new instance with items matching filter removed
     */
    public Strings removeWhen(Predicate<String> filter) {
        Strings clone = clone();
        clone.removeIf(filter);
        return clone;
    }

    @Override
    public String toString() {
        return toString("\n");
    }

    public static void main(String[] args) {
        System.out.println(new Strings().append(";\n").toString(""));
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

    public String toString(String del) {
        return !isEmpty() ? stream().reduce((s1, s2) -> s1 + del + s2).get() : "";
    }

    public Strings toUpper() {
        return stream().map(s -> s != null ? s.toUpperCase() : s).collect(Collectors.toCollection(Strings::new));
    }

    public Strings toLower() {
        return stream().map(s -> s != null ? s.toLowerCase() : s).collect(Collectors.toCollection(Strings::new));
    }

    /**
     * return the index of an item that contains the value
     *
     * @param value
     * @return
     */
    public OptionalInt getInsideIndex(String value) {
        return IntStream.range(0, size())
                .filter(i -> get(i) != null && get(i).contains(value))
                .findAny();
    }

    /**
     * return if this contains a string with value
     *
     * @param value
     * @return
     */
    public boolean containsInside(String value) {
        return getInsideIndex(value).isPresent();
    }

    public boolean containsIgnoreCase(String value) {
        return toLower().contains(value.toLowerCase());
    }

    /**
     * transform a new strings set with call
     *
     * @param call
     * @return
     */
    public Strings transform(CallOneRet<String, String> call) {
        return stream()
                .map(s -> handle(() -> call.call(s)))
                .collect(Collectors.toCollection(Strings::new));
    }

    public Strings intersection(Strings values) {
        return stream().filter(s -> values.containsInside(s))
                .collect(Collectors.toCollection(Strings::new));
    }


}
