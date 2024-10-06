package com.acutus.atk.entity.processor;

import com.acutus.atk.reflection.Reflect;

import static com.acutus.atk.util.StringUtils.bytesToHex;
import static com.acutus.atk.util.StringUtils.nonNullStr;

import com.acutus.atk.util.Strings;
import com.acutus.atk.util.collection.Tuple4;
import com.google.auto.service.AutoService;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import lombok.SneakyThrows;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SupportedAnnotationTypes(
        "com.acutus.atk.entity.processor.Atk")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class AtkProcessor extends AbstractProcessor {

    public AtkProcessor() {
        super();
    }

    protected static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        if (true) return wrapper;
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {
        }
        return unwrapped != null ? unwrapped : wrapper;
    }

    protected ProcessingEnvironment getProcessingEnv() {
        return jbUnwrap(ProcessingEnvironment.class, processingEnv);
    }

    protected void warning(String msg) {
        getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    protected void error(String msg) {
        getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
        throw new RuntimeException(msg);
    }

    protected void info(String msg) {
        getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    public Stream<? extends Element> getFields(Element root) {
        return root.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()));
    }

    public Stream<? extends Element> getPrimitiveFields(Element root) {
        return getFields(root).filter(f -> ElementKind.FIELD.equals(f.getKind()) && isPrimitive(f));
    }

    public Strings getFieldAndAlternateNames(Element field) {
        Strings values = Strings.asList(field.getSimpleName().toString());
        if (field.getAnnotation(Alternate.class) != null) {
            values.addAll(List.of(field.getAnnotation(Alternate.class).value()));
        }
        return values;
    }

    public Strings getFieldNames(Element root, boolean addAlternatives) {
        Strings values = getFields(root)
                .map(f -> f.getSimpleName().toString())
                .collect(Collectors.toCollection(Strings::new));
        if (addAlternatives) {
            getFields(root).filter(f -> f.getAnnotation(Alternate.class) != null)
                    .forEach(f -> values.addAll(List.of(f.getAnnotation(Alternate.class).value())));
        }
        return values;
    }


    @SneakyThrows
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation).stream()
                    .filter(e -> e instanceof TypeElement)
                    .map(e -> (TypeElement) e)
                    .forEach(e -> {
                        validate(e);
                        processElement(roundEnv, e.getQualifiedName().toString(), e);
                    });
        }

        return true;
    }

    @SneakyThrows
    protected void processElement(RoundEnvironment roundEnv, String className, TypeElement element) {
        String source = getElement(roundEnv, className, element)
                .stream()
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .get();

        writeFile(getClassName(element), source);
    }

    protected Strings getImports(Element element) {
        Strings imports =
                Strings.asList(
                        "import com.acutus.atk.entity.*;",
                        "import static com.acutus.atk.util.AtkUtil.handle;",
                        "import java.util.stream.Collectors;",
                        "import java.lang.reflect.Field;",
                        "import com.acutus.atk.reflection.Reflect;");

        imports.addAll(getDaoClass(element).stream().map(atk -> String.format("import %s;", atk.getFirst().toString())).
                collect(Collectors.toCollection(Strings::new)));

        imports.addAll(getImportStatements(element));
        imports = imports.removeDuplicates();
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

    public Strings getImportStatements(Element element) {
        TreePath tree = Trees.instance(getProcessingEnv()).getPath(element);
        return new Strings(tree.getCompilationUnit().getImports().stream().
                map(i -> i.toString()).collect(Collectors.toSet()));
    }

    public Strings getInterfaces(Element element) {
        InterfaceScanner scanner = new InterfaceScanner();
        scanner.scan(element, null);
        return new Strings(scanner.getInterfaceTypes());
    }

    protected Element getClassElement(String className) {
        className = className.replace(".class", "");
        for (Element element : getPackageElement(className).getEnclosedElements()) {
            if (className.endsWith(element.getSimpleName().toString())) {
                return element;
            }

            for (Element innerElements : element.getEnclosedElements()) {
                if (className.endsWith(innerElements.getSimpleName().toString())) {
                    return innerElements;
                }
            }
        }

        throw new RuntimeException("Could not locate element by classname " + className);
    }

    private PackageElement getPackageElement(String className) {
        PackageElement packageElement;
        String packageName = className.substring(0, className.lastIndexOf("."));
        do {
            packageElement = getProcessingEnv().getElementUtils().getPackageElement(packageName);
            packageName = packageName != null && packageName.contains(".") ? packageName.substring(0, packageName.lastIndexOf(".")) : null;
            if (packageName == null) {
                error("Could not locate any package element for className " + className);
            }

        } while (packageElement == null);

        return packageElement;
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
        return annotations + "\n" + String.format("public class %s extends AbstractAtk<%s,%s> {",
                getClassName(element), getClassName(element), element.getSimpleName());
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

    private void assertDaoFields(Element element, Element classRoot, Atk.Match match, String ignore[]) {
        List<? extends Element> fields = getPrimitiveFields(classRoot).collect(Collectors.toList());

        // check for mismatches
        if (match.equals(Atk.Match.FULL)) {

            Strings missing = getPrimitiveFields(classRoot).filter(f -> !getRefDaoField(element, f).isPresent())
                    .map(f -> f.getSimpleName().toString())
                    .filter(f -> !List.of(ignore).contains(f))
                    .collect(Collectors.toCollection(Strings::new));

            if (!missing.isEmpty()) {
                error(getClassName(element) + ". Dao Getter mismatch. Missing " + missing.toString(","));
            }
        }
        // check for type mismatches
        for (Element field : fields) {
            getRefDaoField(element, field).ifPresent(f -> {
                boolean enumMatch = isStringEnum(field) && !isStringEnum(f) && isString(f) ||
                        isStringEnum(f) && !isStringEnum(field) && isString(field);
                if (!field.asType().toString().equals(f.asType().toString()) && !enumMatch) {
                    error("Dao Getter mismatch. Type mismatch  " + f.getSimpleName() + " " + isStringEnum(field) + " " + isStringEnum(f));
                }
            });
        }
    }

    protected List<String> extractDaoClassNames(String atkMirror) {
        atkMirror = atkMirror.substring(atkMirror.indexOf("daoClass=") + "daoClass=".length());
        if (atkMirror.startsWith("{")) {
            atkMirror = atkMirror.substring(1, atkMirror.indexOf("}"));
        } else {
            atkMirror = atkMirror.substring(0, atkMirror.contains(", ") ? atkMirror.indexOf(", ") : atkMirror.indexOf(")"));
        }
        return !atkMirror.isEmpty()
                ? Arrays.asList(atkMirror.split(",")).stream().map(s -> s.trim()).collect(Collectors.toList())
                : List.of();
    }

    protected List<Tuple4<Element, Atk.Match, Boolean, String[]>> getDaoClass(Element element) {
        Atk atk = element.getAnnotation(Atk.class);
        return atk == null || extractDaoClassNames(atk.toString()).isEmpty()
                ? List.of()
                : extractDaoClassNames(atk.toString()).stream()
                .map(c -> new Tuple4<>(getClassElement(c), atk.daoMatch(), atk.daoCopyAll(), atk.daoIgnore())).collect(Collectors.toList());
    }

    private Optional<? extends Element> getRefDaoField(Element element, Element field) {
        return getFields(element).filter(f ->
                f.getSimpleName().equals(field.getSimpleName()) ||
                        f.getAnnotation(Alternate.class) != null &&
                                List.of(f.getAnnotation(Alternate.class).value()).contains(field.getSimpleName().toString()) ||
                        field.getAnnotation(Alternate.class) != null &&
                                List.of(field.getAnnotation(Alternate.class).value()).contains(f.getSimpleName().toString())
        ).findAny();
    }

    private Element retrieveRefDaoField(Element element, Element field) {
        Optional<? extends Element> myField = getRefDaoField(element, field);
        if (!myField.isPresent()) {
            error(getClassName(element) + " could not locate any dao field match for " + field.getSimpleName() + ". Fields " + getFieldNames(element, true));
        }
        return myField.get();
    }

    protected boolean shouldWriteSetter(Element element, Element field) {
        Element myField = retrieveRefDaoField(element, field);
        AtkEdit edit = myField.getAnnotation(AtkEdit.class);
        return edit == null || edit.write();
    }

    protected String getDaoGetterAndSetter(Element element, String cName, Element field, boolean getter) {
        // check types
        Element myField = retrieveRefDaoField(element, field);
        String fName = myField.getSimpleName().toString();
        boolean myFieldSsEnum = isStringEnum(myField);
        boolean daoFieldSsEnum = isStringEnum(field);
        if (myFieldSsEnum && !daoFieldSsEnum && field.asType().toString().equals("java.lang.String")) {
            String fn = field.getSimpleName().toString().substring(0, 1).toUpperCase() + field.getSimpleName().toString().substring(1);
            return getter
                    ? String.format("%s.set%s(_%s.get() != null ? _%s.get().name() : null);", cName, fn, myField.getSimpleName(), fName)
                    : String.format("this._%s.set(%s.get%s() != null ? %s.valueOf(%s.get%s()) : null);", myField.getSimpleName(), cName, fn, myField.asType().toString(), cName, fn);
        }
        return getter
                ? String.format("%s.set%s(_%s.get());", cName, field.getSimpleName().toString().substring(0, 1).toUpperCase() + field.getSimpleName().toString().substring(1), myField.getSimpleName())
                : String.format("this._%s.set(%s.get%s());", myField.getSimpleName(), cName, field.getSimpleName().toString().substring(0, 1).toUpperCase() + field.getSimpleName().toString().substring(1));
    }

    protected Strings getDaoGetterAndSetter(Element element, boolean getter) {
        Strings values = new Strings();
        for (Tuple4<Element, Atk.Match, Boolean, String[]> atk : getDaoClass(element)) {
            assertDaoFields(element, atk.getFirst(), atk.getSecond(), atk.getFourth());
            String cName = atk.getFirst().getSimpleName().toString();
            String fName = atk.getFirst().getSimpleName().toString().substring(0, 1).toLowerCase() + atk.getFirst().getSimpleName().toString().substring(1);
            if (getter) {
                values.add("public " + atk.getFirst().getSimpleName() + " to" + atk.getFirst().getSimpleName() + "() {");
                values.add(String.format("\t%s %s  = new %s();", cName, fName, cName));
            } else {
                values.add(String.format("public " + getClassName(element) + " initFrom" + atk.getFirst().getSimpleName() + "(%s %s) {", cName, fName));
            }
            // filter fields based on name and type
            getPrimitiveFields(atk.getFirst())
                    .filter(f -> getFieldNames(element, true).containsIgnoreCase(f.getSimpleName().toString()))
                    .filter(f -> (getter || shouldWriteSetter(element, f)))
                    .forEach(f -> values.add("\t" + getDaoGetterAndSetter(element, fName, f, getter)));
            if (getter) {
                values.add("\treturn " + fName + ";");
            } else {
                values.add("\treturn this;");
            }
            values.add("}\n");
            IntStream.range(1, values.size() - 1).forEach(i -> values.set(i, "\t" + values.get(i)));
        }
        return values.prepend("\t");
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
                e.asType().toString().startsWith("java.time.Local") ||
                e.asType().toString().equalsIgnoreCase("byte[]");
    }

    /**
     * override to implement custom logic
     * validate the entity
     *
     * @param element
     */
    protected void validate(Element element) {
    }

    public String getHashCode(Element element, Strings entity) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(entity.toString("").getBytes());
        byte[] digest = md.digest();
        return bytesToHex(digest).toUpperCase();
    }

    @SneakyThrows
    protected void getElement(Strings entity, RoundEnvironment roundEnv, TypeElement rootElement, TypeElement element) {

        element.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()) && isPrimitive(f) &&
                        !shouldExcludeField(rootElement, f.getSimpleName().toString()))
                .forEach(e -> {
                    entity.add("\t" + getField(rootElement, e) + "\n");
                    entity.add("\t" + getAtkField(rootElement, e) + "\n");
                    entity.add("\t" + getGetter(rootElement, e) + "\n");
                    entity.add("\t" + getGetterNullsafe(rootElement, e) + "\n");
                    entity.add("\t" + getSetter(rootElement, e) + "\n");
                });

        // process any super classes
        String superClassName = element.getSuperclass().toString();
        if (!Object.class.getName().equals(superClassName)) {
            Optional<? extends Element> e = roundEnv.getRootElements().stream().filter(s -> s.toString().equals(superClassName)).findFirst();
            if (e.isPresent()) {
                getElement(entity, roundEnv, element, (TypeElement) e.get());
            }
        }

    }

    @SneakyThrows
    protected Strings getElement(RoundEnvironment roundEnv, String className, TypeElement element) {
        Strings entity = new Strings();

        entity.add(getPackage(className, element) + ";\n");
        entity.add(getImports(element).replace("\n", "").toString("\n"));
        entity.add(getClassNameLine(element) + "\n");
        entity.add(getStaticFields(element).append(";\n").toString(""));
        entity.add(getConstructors(element).prepend("\t").append("\n").toString(""));
        entity.add(getExtraFields(element).append(";\n").toString(""));
        entity.add(getMethods(className, element).append("\n").toString(""));
        entity.add(getDaoGetterAndSetter(element, false).append("\n").toString(""));
        entity.add(getDaoGetterAndSetter(element, true).append("\n").toString(""));

        getElement(entity, roundEnv, element, element);
        // add md5 hash
        String hash = getHashCode(element, entity);

        entity.add(String.format("\t@Override\n\tpublic String getMd5Hash() {return \"%s\";}", hash));
        // add compile time used in FE
        entity.add(String.format("\t@Override\n\tpublic String getCompileTime() {return \"%s\";}", java.time.LocalDateTime.now().toString()));
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

    protected String getStaticField(Element parent, String fieldName) {
        return String.format("\tpublic static final Field FIELD_%s = Reflect.getFields(%s.class).getByName(\"_%s\").get()"
                , fieldName.toUpperCase(), getClassName(parent), fieldName);
    }

    protected Strings getStaticFields(Element parent) {
        return parent.getEnclosedElements().stream()
                .filter(f -> ElementKind.FIELD.equals(f.getKind()) && isPrimitive(f))
                .map(e -> getStaticField(parent, e.getSimpleName().toString()))
                .distinct()
                .collect(Collectors.toCollection(Strings::new));
    }

    protected Strings getExtraFields(Element parent) {
        return new Strings();
    }

    /**
     * exclude fields if its added somewhere else
     *
     * @param element
     * @param name
     * @return
     */
    protected boolean shouldExcludeField(Element element, String name) {
        return false;
    }

    protected String getAtkField(Element parent, Field e) {
        return String.format("private transient AtkField<%s,%s> _%s = new AtkField<>(%s,this);"
                , e.getType().getName(), getClassName(parent), e.getName()
                , String.format("Reflect.getFields(%s.class).getByName(\"%s\").get()"
                        , getClassName(parent), e.getName())
        );
    }

    protected String getAtkField(Element parent, Element e) {
        return String.format("private transient AtkField<%s,%s> _%s = new AtkField<>(%s,this);"
                , e.asType().toString(), getClassName(parent), e.getSimpleName()
                , String.format("Reflect.getFields(%s.class).getByName(\"%s\").get()"
                        , getClassName(parent), e.getSimpleName())
        );
    }

    protected String methodName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + (fieldName.length() > 0 ? fieldName.substring(1) : "");
    }

    protected String getSetterExtra(Element parent, Element e) {
        return "";
    }

    protected String getSetter(Element parent, Element e) {
        return String.format("public %s set%s(%s %s) {\n"
                        + getSetterExtra(parent, e)
                        + "\tthis._%s.set(%s);"
                        + "\treturn this;"
                        + "};"
                , getClassName(parent), methodName(e.getSimpleName().toString()), e.asType().toString()
                , e.getSimpleName().toString()
                , e.getSimpleName().toString(), e.getSimpleName().toString());
    }

    protected String getGetterTemplate(Element parent, Element e, String methodPostFix, String center) {
        String type = e.asType().toString();
        return String.format("public %s get%s" + methodPostFix + "() {"
                        + center
                        + "};"
                , e.asType().toString(), methodName(e.getSimpleName().toString()));
    }

    protected String getGetter(Element parent, Element e) {
        return getGetterTemplate(parent, e, "", String.format("return this._%s.get();", e.getSimpleName().toString()));
    }

    protected String getGetterNullsafe(Element parent, Element e) {
        String nullSafe = "return this._" + e.getSimpleName().toString() + ".get();";
        if (e.asType().toString().startsWith("java.lang")) {
            try {
                Class type = Class.forName(e.asType().toString());
                if (Number.class.isAssignableFrom(type)) {
                    nullSafe = "return " + e.getSimpleName().toString() + " == null ? 0 : " + e.getSimpleName().toString() + ";";
                }
                if (String.class.equals(type)) {
                    nullSafe = "return " + e.getSimpleName().toString() + " == null ? \"\" : " + e.getSimpleName().toString() + ";";
                }
                if (Boolean.class.equals(type)) {
                    nullSafe = "return " + e.getSimpleName().toString() + " == null ? false : " + e.getSimpleName().toString() + ";";
                }
            } catch (ClassNotFoundException nfe) {
            }
        }
        return getGetterTemplate(parent, e, "NullSafe", nullSafe);
    }

    @SneakyThrows
    protected void writeFile(String className, String source) {

        JavaFileObject builderFile = getProcessingEnv().getFiler().createSourceFile(className);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print(source);
        }
    }

    public class DebugStrings extends Strings {

        @Override
        public boolean add(String s) {
//            info(s);
            return super.add(s);
        }
    }

}
