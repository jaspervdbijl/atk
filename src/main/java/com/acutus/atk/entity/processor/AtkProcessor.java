package com.acutus.atk.entity.processor;

import afu.org.checkerframework.checker.signature.qual.SourceName;
import com.acutus.atk.entity.AtkFieldUtil;
import com.acutus.atk.util.Assert;
import com.acutus.atk.util.Strings;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.acutus.atk.util.AtkUtil.handle;

@SupportedAnnotationTypes(
        "com.acutus.atk.entity.processor.Atk")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class AtkProcessor extends AbstractProcessor {

    protected void warning(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    protected void info(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    @SneakyThrows
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        info("Started");

        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation).stream()
                    .forEach(e -> processElement(((TypeElement) e).getQualifiedName().toString(), e));
        }

        return true;
    }

    protected void processElement(String className, Element element) {
        String source = getElement(className, element)
                .stream()
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .get();
        info(source);
        writeFile(className, source);
    }

    protected Strings getElement(String className, Element element) {
        info("Process " + element.getSimpleName());

        Strings entity = new Strings();

        String packageName = className.substring(0, className.lastIndexOf("."));
        entity.add("package " + packageName + ";");
        entity.add("import com.acutus.atk.entity.*;");

        entity.add("public class " + element.getSimpleName() + "Imp extends com.acutus.atk.entity.AbstractAtk {");

        info("Entity " + entity);

        // add all the fields
        element.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()))
                .forEach(e -> {
                    entity.add(getField(e));
                    entity.add(getAtkField(className, e));
                    entity.add(getGetter(e));
                    entity.add(getSetter(className, e));
                });
        entity.add("}");

        info("compile " + entity);
        return entity;
    }

    protected String getField(Element element) {
        return (!element.getAnnotationMirrors().isEmpty() ?
                (element.getAnnotationMirrors().stream()
                        .map(a -> a.toString())
                        .reduce((s1, s2) -> "@" + s1 + "\n" + "@" + s2).get()
                        + "\n")
                : "")
                + String.format("protected %s %s;", element.asType().toString(), element.getSimpleName());
    }

    protected String getAtkField(String className, Element element) {
        return String.format("public AtkField<%s,%sImp> _%s = new AtkField<>(%s.class,%s,this);"
                , element.asType().toString(), className, element.getSimpleName()
                , element.asType().toString()
                , String.format("AtkFieldUtil.getFieldByName(%sImp.class,\"%s\")", className, element.getSimpleName())
        );
    }

    protected String methodName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + (fieldName.length() > 0 ? fieldName.substring(1) : "");
    }

    protected String getSetter(String className, Element element) {
        return String.format("public %sImp set%s(%s %s) {"
                        + "this._%s.set(%s);"
                        + "return this;"
                        + "};"
                , className, methodName(element.getSimpleName().toString()), element.asType().toString()
                , element.getSimpleName().toString()
                , element.getSimpleName().toString(), element.getSimpleName().toString());
    }

    protected String getGetter(Element element) {
        return String.format("public %s get%s() {"
                        + "return this._%s.get();"
                        + "};"
                , element.asType().toString(), methodName(element.getSimpleName().toString())
                , element.getSimpleName().toString());
    }


    @SneakyThrows
    protected void writeFile(String className, String source) {

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className + "Imp");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print(source);
        }
    }

}
