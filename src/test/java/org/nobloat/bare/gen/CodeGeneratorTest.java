package org.nobloat.bare.gen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeGeneratorTest {
    @BeforeEach
    @AfterEach
    void cleanup() throws IOException {
        //Files.deleteIfExists(Paths.get("Messages.java"));
        //Files.deleteIfExists(Paths.get("Messages.class"));
    }

    @Test
    void compileSchema() throws Exception {
        CodeGenerator.main(new String[]{"bare-jvm", "src/test/resources/schema2.bare", "Messages.java"});
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals(0, compiler.run(null, System.out, System.err, "--source-path", "src/main/java", "Messages.java"));
    }

    @Test
    void compileNumberSchema() throws Exception {
        CodeGenerator.main(new String[]{"bare-jvm", "src/test/resources/numbers.bare", "Messages.java"});
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals(0, compiler.run(null, System.out, System.err, "--source-path", "src/main/java", "Messages.java"));
    }

}