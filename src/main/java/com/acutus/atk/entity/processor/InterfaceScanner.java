package com.acutus.atk.entity.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner7;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class InterfaceScanner extends ElementScanner7<Void, Void> {

    private Set<String> interfaces = new HashSet<>();

    public Set<String> getInterfaceTypes() {
        return interfaces;
    }

    @Override
    public Void visitType(TypeElement e, Void p) {
        interfaces = e.getInterfaces().stream().map(in -> in.toString()).collect(Collectors.toSet());
        return super.visitType(e, p);
    }
}