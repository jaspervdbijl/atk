package com.acutus.atk.entity;

import com.acutus.atk.reflection.ReflectFields;
import com.acutus.atk.util.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AtkFieldList<T extends AtkField> extends ArrayList<T> {

    // keep a static class name index
    public AtkFieldList() {
    }

    public AtkFieldList(Collection<T> collection) {
        super.addAll(collection);
    }

    public AtkFieldList<T> getChanged() {
        return stream().filter(f -> f.isChanged()).collect(Collectors.toCollection(AtkFieldList::new));
    }

    public AtkFieldList<T> getSet() {
        return stream().filter(f -> f.isSet()).collect(Collectors.toCollection(AtkFieldList::new));
    }

    public AtkFieldList<T> excludeIgnore() {
        return stream().filter(f -> !f.isIgnore()).collect(Collectors.toCollection(AtkFieldList::new));
    }


    public AtkFieldList<T> restoreSet() {
        stream().filter(f -> f.get() != null).forEach(f -> f.setSet(true));
        return this;
    }

    public void initFrom(AtkFieldList fields) {
        for (AtkField field : this) {
            field.initFrom((AtkField) fields.getByName(field.getField().getName()).get());
        }
    }


    public Strings getNames() {
        return stream().map(f -> f.getField().getName()).collect(Collectors.toCollection(Strings::new));
    }

    public String toString() {
        Optional<String> value = stream().map(f -> f.toString()).reduce((s1, s2)->s1+"\n\t"+s2);
        return value.isPresent()?value.get():"";
    }

    public Optional<T> getByName(String name) {
        return stream().filter(f -> f.getField().getName().equals(name)).findAny();
    }

    public ReflectFields toRefFields() {
        return new ReflectFields(stream().map(f -> f.getField()).collect(Collectors.toList()));
    }

    public AtkFieldList addField(T field) {
        add(field);
        return this;
    }

}
