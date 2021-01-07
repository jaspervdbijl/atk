package com.acutus.atk.entity.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner7;
import java.util.HashSet;
import java.util.Set;

public class ImportScanner extends ElementScanner7<Void, Void> {

    private Set<String> types = new HashSet<>();

    public Set<String> getImportedTypes() {
        return types;
    }

    @Override
    public Void visitType(TypeElement e, Void p) {
        for (TypeMirror interfaceType : e.getInterfaces()) {
            types.add(interfaceType.toString());
        }
        types.add(e.getSuperclass().toString());
        return super.visitType(e, p);
    }
}