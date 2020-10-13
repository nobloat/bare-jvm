package org.nobloat.bare.gen;

import org.nobloat.bare.dsl.Ast;

import java.io.OutputStream;
import java.util.List;

public class CodeGenerator {

    public static void createJavaTypes(String packageName, List<Ast.Type> types, OutputStream outputStream) {
        CodeWriter writer = new CodeWriter(outputStream);


        writer.write("package " + packageName + ";");

        for(var type : types) {
            //TODO: switch on types
            writer.write(classProlog(type.name));
            writer.write(classEpilog());
        }

    }



    public static String classProlog(String classname) {
        return "public static class " + classname + " {";
    }

    public static String classEpilog() {
        return "}";
    }
}
