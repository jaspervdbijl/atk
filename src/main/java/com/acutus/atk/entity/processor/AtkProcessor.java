package com.acutus.atk.entity.processor;

import com.acutus.atk.reflection.Reflect;
import com.acutus.atk.reflection.ReflectFields;
import com.acutus.atk.util.Strings;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes(
        "com.acutus.atk.entity.processor.Atk")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class AtkProcessor extends AbstractProcessor {

    protected void warning(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    protected void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    protected void info(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    public Stream<? extends Element> getFields(Element root) {
        return root.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()));
    }

    public Strings getFieldNames(Element root) {
        return getFields(root)
                .map(f -> f.getSimpleName().toString())
                .collect(Collectors.toCollection(Strings::new));
    }

    @SneakyThrows
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        info("Started");
        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation).stream()
                    .forEach(e -> processElement(
                            ((TypeElement) e).getQualifiedName().toString(), e));
        }

        return true;
    }

    @SneakyThrows
    protected void processElement(String className, Element element) {
        String source = getElement(className, element)
                .stream()
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .get();

        writeFile(getClassName(element), source);
    }

    protected Strings getImports() {
        return Strings.asList("import com.acutus.atk.entity.*", "import static com.acutus.atk.util.AtkUtil.handle"
                , "import java.util.stream.Collectors", "import java.lang.reflect.Field"
                , "import com.acutus.atk.reflection.Reflect");
    }

    protected String getPackage(String className, Element element) {
        String packageName = className.substring(0, className.lastIndexOf("."));
        return "package " + packageName;
    }

    protected String getClassName(Element element) {
        Atk atk = element.getAnnotation(Atk.class);
        return atk.className().isEmpty() ? element.getSimpleName() + atk.classNameExt() :
                atk.className();
    }

    private String removeSection(String line, String remove) {
        if (line.contains(remove)) {
            int index = line.indexOf(remove);
            String header = line.substring(0, index);
            String trailer = line.substring(index + remove.length());
            if (trailer.trim().startsWith("(")) {
                trailer = trailer.substring(trailer.indexOf(")") + 1);
            }
            return header + trailer;
        }
        return line;
    }

    protected String getClassNameLine(Element element, String... removeStrings) {
        // copy all Annotations over except lombok
        String annotations = element.getAnnotationMirrors().stream()
                .map(a -> a.toString()).collect(Collectors.joining(" "))
                .replace("@lombok.NoArgsConstructor", "")
                .replace("@lombok.AllArgsConstructor", "")
                .replace("@lombok.Builder", "");
        info("getClassNameLine = " + annotations);
        for (String remove : removeStrings) {
            annotations = removeSection(annotations, remove);
        }
        // replace Atk annotation
        annotations = annotations.replace("@com.acutus.atk.entity.processor.Atk", "");
        return annotations + "\n" + String.format("public class %s extends AbstractAtk<%s,%s> {"
                , getClassName(element), getClassName(element), element.getSimpleName());
    }

    protected Strings getConstructors(Element element) {
        return Strings.asList(String.format("public %s() {}", getClassName(element)));
    }

    protected Strings getMethods(String className, Element element) {
        return Strings.asList();
    }

    /**
     * expand to include other supported field types
     *
     * @param e
     * @return
     */
    @SneakyThrows
    public boolean isPrimitive(Element e) {
        // determine if the type is a enum
        return e.asType().toString().startsWith("java.lang.") ||
                e.asType().toString().startsWith("java.time.Local");
    }

    protected Strings getElement(String className, Element element) {
        info("Process " + element.getSimpleName());

        Strings entity = new DebugStrings();

        entity.add(getPackage(className, element) + ";");
        entity.add(getImports().append(";\n").toString(""));
        entity.add(getClassNameLine(element));
        entity.add(getConstructors(element).append("\n").toString(""));
        entity.add(getExtraFields(element).append(";\n").toString(""));
        entity.add(getMethods(className, element).append("\n").toString(""));

        element.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()) && isPrimitive(f))
                .forEach(e -> {
                    entity.add(getField(element, e));
                    entity.add(getAtkField(element, e));
                    entity.add(getGetter(element, e));
                    entity.add(getSetter(element, e));
                });
        entity.add("}");

        return entity;
    }

    @SneakyThrows
    private String getSuperClass(Element parent,String superClassName) {
        Strings superFields = new Strings();
        for (Field field : Reflect.getFields(Class.forName(superClassName)).values()) {
            for (Annotation a : field.getAnnotations()) {
                superFields.add(a.toString());
            }
            superFields.add(String.format("private %s %s;",field.getType().getName(),field.getName()));
            superFields.add(getAtkField(parent,field));
        }
        info("super fields " + superFields);
        return superFields.toString("\n");
    }

    protected String getField(Element root, Element element) {
        String annotations =
                (!element.getAnnotationMirrors().isEmpty() ?
                        (element.getAnnotationMirrors().stream()
                                .map(a -> a.toString())
                                .reduce((s1, s2) -> s1 + "\n" + s2).get()
                                + "\n")
                        : "");
        String modifiers =
                (!element.getModifiers().isEmpty() ? element.getModifiers().stream()
                        .map(m -> m.toString())
                        .reduce((s1, s2) -> s1 + " " + s2).get()
                        : "");
        return String.format("%s %s %s %s;", annotations, modifiers
                , element.asType().toString(), element.getSimpleName());
    }

    protected Strings getExtraFields(Element parent) {
        return new Strings();
    }

    protected String getAtkField(Element parent, Field e) {
        return String.format("public transient AtkField<%s,%s> _%s = new AtkField<>(%s,this);"
                , e.getType().getName(), getClassName(parent), e.getName()
                , String.format("Reflect.getFields(%s.class).getByName(\"%s\").get()"
                        , getClassName(parent), e.getName())
        );
    }

    protected String getAtkField(Element parent, Element e) {
        return String.format("public transient AtkField<%s,%s> _%s = new AtkField<>(%s,this);"
                , e.asType().toString(), getClassName(parent), e.getSimpleName()
                , String.format("Reflect.getFields(%s.class).getByName(\"%s\").get()"
                        , getClassName(parent), e.getSimpleName())
        );
    }

    protected String methodName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + (fieldName.length() > 0 ? fieldName.substring(1) : "");
    }

    protected String getSetter(Element parent, Element e) {
        return String.format("public %s set%s(%s %s) {"
                        + "this._%s.set(%s);"
                        + "return this;"
                        + "};"
                , getClassName(parent), methodName(e.getSimpleName().toString()), e.asType().toString()
                , e.getSimpleName().toString()
                , e.getSimpleName().toString(), e.getSimpleName().toString());
    }

    protected String getGetter(Element parent, Element e) {
        return String.format("public %s get%s() {"
                        + "return this._%s.get();"
                        + "};"
                , e.asType().toString(), methodName(e.getSimpleName().toString())
                , e.getSimpleName().toString());
    }


    @SneakyThrows
    protected void writeFile(String className, String source) {

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print(source);
        }
    }

    public class DebugStrings extends Strings {

        @Override
        public boolean add(String s) {
            info(s);
            return super.add(s);
        }
    }

}
