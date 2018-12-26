package com.acutus.atk.entity.processor;

import lombok.SneakyThrows;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessorHelper {

    public static String GET_FIELDS = "public AtkFieldList getFields() {\n" +
            "        return (AtkFieldList) getRefFields().stream()\n" +
            "                .map(f -> (AtkField) handle(() -> ((Field) f).get(this)))\n" +
            "                .collect(Collectors.toCollection(AtkFieldList::new));\n" +
            "    }";

    @SneakyThrows
    public static String getSourcePath(ProcessingEnvironment pEnv) {
        Filer filer = pEnv.getFiler();
        FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "tmp", (Element[]) null);
        Path projectPath = Paths.get(resource.toUri()).getParent().getParent();
        resource.delete();
        String path = projectPath.resolve("src").toString();
        return path.substring(0, path.length() - "target/src".length());
    }

}
