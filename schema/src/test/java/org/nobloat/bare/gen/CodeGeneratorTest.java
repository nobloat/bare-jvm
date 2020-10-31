package org.nobloat.bare.gen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodeGeneratorTest {

    @BeforeEach
    @AfterEach
    void cleanup() throws IOException {
        Path pathToBeDeleted = Path.of("org");
        if (Files.exists(pathToBeDeleted)) {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.deleteIfExists(Path.of("Messages.java"));
    }

    @Test
    void compileSchema() throws Exception {
        CodeGenerator.main(new String[]{"src/test/resources/schema2.bare", "org.example.Messages"});
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals(0, compiler.run(null, System.out, System.err, "--source-path", "../codec/src/main/java", "org/example/Messages.java"));
    }

    @Test
    void compileNumberSchema() throws Exception {
        CodeGenerator.main(new String[]{"src/test/resources/numbers.bare", "org.example.NumberMessages"});
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals(0, compiler.run(null, System.out, System.err, "--source-path", "../codec/src/main/java", "org/example/NumberMessages.java"));
    }

    @Test
    void testNestedStruct() {
        assertThrows(UnsupportedOperationException.class, () -> CodeGenerator.main(new String[]{"src/test/resources/schema.bare", "Messages"}));
    }
}