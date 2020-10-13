package org.nobloat.bare.gen;

import org.nobloat.bare.dsl.Ast;
import org.nobloat.bare.dsl.AstParser;
import org.nobloat.bare.dsl.Lexer;
import org.nobloat.bare.dsl.Scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CodeGenerator {

    public static void createJavaTypes(String packageName, List<Ast.Type> types, OutputStream outputStream) throws Exception {
        CodeWriter writer = new CodeWriter(outputStream);


        writer.write("package " + packageName + ";");

        for (var type : types) {
            //TODO: switch on types
            writer.write(classProlog(type.name));
            writer.write(classEpilog());
        }

        writer.close();
    }


    public static String classProlog(String classname) {
        return "public static class " + classname + " {";
    }

    public static String classEpilog() {
        return "}";
    }

    public static void main(String[] args) throws Exception {
        try (var is = openFile("schema.bare"); var scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser parser = new AstParser(lexer);
            CodeGenerator.createJavaTypes("com.example", parser.parse(), System.out);

        }
    }

    public static InputStream openFile(String name) throws FileNotFoundException {
        String path = "src/test/resources";
        File file = new File(path);
        return new FileInputStream(file.getAbsolutePath() + "/" + name);
    }
}
