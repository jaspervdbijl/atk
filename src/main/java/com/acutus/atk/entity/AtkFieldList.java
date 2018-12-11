package com.acutus.atk.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AtkFieldList extends ArrayList<AtkField> {

    public AtkFieldList getChanged() {
        return stream().filter(f -> f.isChanged()).collect(Collectors.toCollection(AtkFieldList::new));
    }

    public List<String> getNames() {
        return stream().map(f -> f.getField().getName()).collect(Collectors.toList());
    }

    public String toString() {
        Optional<String> value = stream().map(f -> f.toString()).reduce((s1, s2)->s1+"\n\t"+s2);
        return value.isPresent()?value.get():"";
    }

}
