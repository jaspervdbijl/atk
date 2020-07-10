package com.acutus.atk.entity.processor;

import com.acutus.atk.reflection.Reflect;
import com.acutus.atk.util.Assert;
import com.acutus.atk.util.AtkUtil;
import com.acutus.atk.util.Strings;
import com.acutus.atk.util.collection.Two;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.acutus.atk.util.AtkUtil.convertStackTraceToString;
import static com.acutus.atk.util.AtkUtil.getGenericType;

@SupportedAnnotationTypes(
        "com.acutus.atk.entity.processor.Atk")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class AtkProcessor extends AbstractProcessor {

    protected void warning(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    protected void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
        throw new RuntimeException(msg);
    }

    protected void info(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    public Stream<? extends Element> getFields(Element root) {
        return root.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()));
    }

    public Stream<? extends Element> getPrimitiveFields(Element root) {
        return getFields(root).filter(f -> ElementKind.FIELD.equals(f.getKind()) && isPrimitive(f));
    }

    public Stream<? extends Element> getPrimitiveAndCollectionFields(Element root) {
        return getFields(root).filter(f -> ElementKind.FIELD.equals(f.getKind()) ||
                f.asType().toString().startsWith("java.util.") && f.asType().toString().contains("List"));
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

        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation).stream()
                    .forEach(e -> processElement(roundEnv,
                            ((TypeElement) e).getQualifiedName().toString(), e));
        }

        return true;
    }

    @SneakyThrows
    protected void processElement(RoundEnvironment roundEnv,String className, Element element) {
        String source = getElement(roundEnv,className, element)
                .stream()
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .get();

        writeFile(getClassName(element), source);
    }

    protected Strings getImports(Element element) {
        Strings imports =  Strings.asList("import com.acutus.atk.entity.*", "import static com.acutus.atk.util.AtkUtil.handle"
                , "import java.util.stream.Collectors", "import java.lang.reflect.Field"
                , "import com.acutus.atk.reflection.Reflect");
        // append all the DAO classes
        getDaoClass(element).ifPresent(atk -> imports.add("import " + atk.getFirst().toString()));
        return imports;
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

    public Strings getImportStatements(RoundEnvironment roundEnvironment) {
        ImportScanner scanner = new ImportScanner();
        scanner.scan(roundEnvironment.getRootElements(), null);
        return new Strings(scanner.getImportedTypes());
    }

    protected Element getClassElement(String className) {
        // find by package

        String packageName = className.substring(0, className.lastIndexOf("."));
        Optional<? extends Element> classE =
                processingEnv.getElementUtils().getPackageElement(packageName)
                        .getEnclosedElements().stream().filter(e -> className.endsWith(e.getSimpleName().toString())).findAny();
        Assert.isTrue(classE.isPresent(), "Could not locate element by classname " + className);
        return classE.get();
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

    private boolean containsAlternate(Strings names, Element field) {
        Alternate alternate = field.getAnnotation(Alternate.class);
        return alternate != null && !Strings.asList(alternate.value()).intersection(names).isEmpty();
    }

    protected boolean isStringEnum(Element field) {
        return field.getAnnotationMirrors().toString().contains("javax.persistence.EnumType.STRING");
    }

    protected boolean isString(Element field) {
        return field.asType().toString().equals("java.lang.String");
    }

    private void assertDaoFields(Element element,Element classRoot,Atk.Match match ) {
        List<? extends Element> fields = getPrimitiveFields(classRoot).collect(Collectors.toList());

        // check for mismatches
        if (match.equals(Atk.Match.FULL)) {

            Optional<? extends Element> notFound = fields.stream().filter(f ->
                            !getFieldNames(element).containsIgnoreCase(f.getSimpleName().toString()) &&
                                    containsAlternate(getFieldNames(element), f)
                    ).findAny();
            if (notFound.isPresent()) {
                error(getClassName(element) + ". Dao Getter mismatch. Missing " + notFound.get().getSimpleName());
            }
        }
        // check for type mismatches
        for (Element field : fields) {
            getFields(element).filter(f -> f.getSimpleName().equals(field.getSimpleName())).findAny().ifPresent(f -> {
                boolean enumMatch = isStringEnum(field) && !isStringEnum(f)  && isString(f) ||
                        isStringEnum(f) && !isStringEnum(field)  && isString(field);
                if (!field.asType().toString().equals(f.asType().toString()) && !enumMatch) {
                    error("Dao Getter mismatch. Type mismatch  " + f.getSimpleName() + " " + isStringEnum(field) + " " + isStringEnum(f));
                }
            });
        }
    }

    protected String extractDaoClassName(String atkMirror) {
        atkMirror = atkMirror.substring(atkMirror.indexOf("daoClass=")+"daoClass=".length());
        return atkMirror.contains(",") ? atkMirror.substring(0,atkMirror.indexOf(",")) : atkMirror.substring(atkMirror.indexOf(")"));
    }

    protected Optional<Two<Element, Atk.Match>> getDaoClass(Element element) {
        Atk atk = element.getAnnotation(Atk.class);
        return atk == null || "java.lang.Void".equals(extractDaoClassName(atk.toString()))
                ? Optional.empty()
                : Optional.of(new Two<>(getClassElement(extractDaoClassName(atk.toString())), atk.daoMatch()));
    }

    protected String getDaoGetterAndSetter(Element element, String cName, Element field, boolean getter) {
        // get
        Alternate alternate = field.getAnnotation(Alternate.class);
        Strings alternateNames = alternate != null ? Strings.asList(alternate.value())
                .intersection(getFieldNames(element)) : new Strings();
        if (!getFieldNames(element).contains(field.getSimpleName().toString()) &&
                (alternateNames.isEmpty() || !getFieldNames(element).contains(alternateNames.get(0)))) {
            error(getClassName(element) + " could not locate any dao field match for " + field.getSimpleName() + ". Fields " + getFieldNames(element));
        }
        final String fName = getFieldNames(element).contains(field.getSimpleName().toString())
                ? field.getSimpleName().toString()
                : alternateNames.get(0);
        // check types
        Element myField = getFields(element).filter(f -> f.getSimpleName().toString().equals(fName)).findFirst().get();
        boolean myFieldSsEnum = isStringEnum(myField);
        boolean daoFieldSsEnum = isStringEnum(field);
        if (myFieldSsEnum && !daoFieldSsEnum && field.asType().toString().equals("java.lang.String")) {
            String fn = field.getSimpleName().toString().substring(0, 1).toUpperCase() + field.getSimpleName().toString().substring(1);
            return getter
                    ? String.format("%s.set%s(_%s.get() != null ? _%s.get().name() : null);", cName, fn, fName, fName)
                    : String.format("this._%s.set(%s.get%s() != null ? %s.valueOf(%s.get%s()) : null);", fName, cName, fn, myField.asType().toString(), cName, fn);
        }
        return getter
                ? String.format("%s.set%s(_%s.get());", cName, fName.substring(0, 1).toUpperCase() + field.getSimpleName().toString().substring(1), fName)
                : String.format("this._%s.set(%s.get%s());", fName, cName, fName.substring(0, 1).toUpperCase() + fName.substring(1));
    }

    protected Strings getDaoGetterAndSetter(Element element, boolean getter) {

        Optional<Two<Element, Atk.Match>> atk = getDaoClass(element);
        if (atk.isPresent()) {
            assertDaoFields(element,atk.get().getFirst(),atk.get().getSecond());
            Strings values = new Strings();
            String cName = atk.get().getFirst().getSimpleName().toString();
            String fName = atk.get().getFirst().getSimpleName().toString().substring(0, 1).toLowerCase() + atk.get().getFirst().getSimpleName().toString().substring(1);
            if (getter) {
                values.add("public " + atk.get().getFirst().getSimpleName() + " to" + atk.get().getFirst().getSimpleName() + "() {");
                values.add(String.format("%s %s  = new %s();", cName, fName, cName));
            } else {
                values.add(String.format("public " + getClassName(element) + " initFrom" + atk.get().getFirst().getSimpleName() + "(%s %s) {", cName, fName));
            }
            // filter fields based on name and type
            getPrimitiveAndCollectionFields(atk.get().getFirst())
                    .filter(f -> getFieldNames(element).containsIgnoreCase(f.getSimpleName().toString()))
                    .forEach(f -> values.add(getDaoGetterAndSetter(element, fName, f, getter)));
            if (getter) {
                values.add("return " + fName + ";");
            } else {
                values.add("return this;");
            }
            values.add("}");
            IntStream.range(1, values.size() - 1).forEach(i -> values.set(i, "\t" + values.get(i)));
            return values.prepend("\t");
        } else {
            return new Strings();
        }
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

    protected Strings getElement(RoundEnvironment roundEnv,String className, Element element) {

        Strings entity = new DebugStrings();
        entity.add(getPackage(className, element) + ";\n\n");
        entity.add(getImports(element).append(";\n").toString(""));
        entity.add(getClassNameLine(element) + "\n");
        entity.add(getConstructors(element).append("\n").toString(""));
        entity.add(getStaticFields(element).append(";\n").toString(""));
        entity.add(getExtraFields(element).append(";\n").toString(""));
        entity.add(getMethods(className, element).append("\n").toString(""));
        entity.add(getDaoGetterAndSetter(element, false).append("\n").toString(""));
        entity.add(getDaoGetterAndSetter(element, true).append("\n").toString(""));

        element.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()) && isPrimitive(f) &&
                        !shouldExcludeField(element,f.getSimpleName().toString()))
                .forEach(e -> {
                    entity.add("\t" + getField(element, e) + "\n");
                    entity.add("\t" + getAtkField(element, e) + "\n");
                    entity.add("\t" + getGetter(element, e) + "\n");
                    entity.add("\t" + getSetter(element, e) + "\n");
                });
        entity.add("}");

        return entity;
    }

    @SneakyThrows
    private String getSuperClass(Element parent, String superClassName) {
        Strings superFields = new Strings();
        for (Field field : Reflect.getFields(Class.forName(superClassName))) {
            for (Annotation a : field.getAnnotations()) {
                superFields.add(a.toString());
            }
            superFields.add(String.format("private %s %s;", field.getType().getName(), field.getName()));
            superFields.add(getAtkField(parent, field));
        }
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
                , element.asType().toString(), element.getSimpleName()).replace("\n", "\n\t");
    }

    protected String getStaticField(Element parent,String fieldName) {
        return String.format("\tpublic static final Field FIELD_%s = Reflect.getFields(%s.class).getByName(\"_%s\").get()"
                , fieldName.toUpperCase(), getClassName(parent), fieldName);
    }

    protected Strings getStaticFields(Element parent) {
        return parent.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()) && isPrimitive(f))
                .map(e -> getStaticField(parent,e.getSimpleName().toString()))
                .distinct()
                .collect(Collectors.toCollection(Strings::new));
    }

    protected Strings getExtraFields(Element parent) {
        return new Strings();
    }

    /**
     * exclude fields if its added somewhere else
     * @param element
     * @param name
     * @return
     */
    protected boolean shouldExcludeField(Element element, String name) {
        return false;
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
