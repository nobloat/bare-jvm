package org.nobloat.bare.gen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nobloat.bare.TestUtil;
import org.nobloat.bare.dsl.AstParser;
import org.nobloat.bare.dsl.Lexer;
import org.nobloat.bare.dsl.Scanner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeGeneratorTest {


    @BeforeEach
    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get("Messages.java"));
        Files.deleteIfExists(Paths.get("Messages.class"));
    }


    @Test
    void compileSchema() throws Exception {
        try (var is = TestUtil.openFile("schema2.bare"); var scanner = new Scanner(is); var sourceFile = new FileOutputStream("Messages.java")) {
            Lexer lexer = new Lexer(scanner);
            AstParser parser = new AstParser(lexer);
            var types = parser.parse();
            new CodeGenerator(null, types, sourceFile).createJavaTypes();
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals(0, compiler.run(null, System.out, System.err, "--source-path", "src/main/java", "Messages.java"));
    }

}