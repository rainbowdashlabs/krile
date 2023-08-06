package de.chojo.krile.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TextTest {

    @Test
    void splitByLength() {
    }

    @Test
    void toDiscordMarkdownAndSplit() {
        var text = """
                ```java
                test
                ```
                                
                Normal gap after code block
                                
                This has to be one line.\s\s
                This has to be one line as well.\\
                This line
                and this line are together.
                        
                This has to be a new section.
                ```java
                test```
                ```java
                test
                ```
                no gap after code block
                ```java
                test
                ```
                                
                                
                Very wide gap between code block and text""";
        var expected = List.of("""
                ```java
                test
                ```
                Normal gap after code block

                This has to be one line.
                This has to be one line as well.
                This line and this line are together.
                        
                This has to be a new section.
                ```java
                test``````java
                test
                ```no gap after code block
                ```java
                test
                ```
                                
                Very wide gap between code block and text""");
        List<String> result = Text.toDiscordMarkdownAndSplit(text, 2000);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(expected, result);

        result = Text.toDiscordMarkdownAndSplit(text, 100);
        Assertions.assertEquals(4, result.size());

        expected = List.of(
                """
                ```java
                test
                ```""",
                """
                
                Normal gap after code block

                This has to be one line.
                This has to be one line as well.""",
                """
                This line and this line are together.
                
                This has to be a new section.
                ```java
                test```""",
                """
                ```java
                test
                ```no gap after code block
                ```java
                test
                ```
                
                Very wide gap between code block and text"""
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void stripTextLineBreaks() {
        var text = """
                This has to be one line.\s\s
                This has to be one line as well.\\
                This line
                and this line are together.
                        
                This has to be a new section.""";
        var expected = """
                This has to be one line.
                This has to be one line as well.
                This line and this line are together.
                        
                This has to be a new section.""";
        List<String> result = Text.stripTextLineBreaks(text, 2000);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(expected, result.get(0));
    }

    @Test
    void compressCodeBlocks() {
        var blocks = """
                Test
                ```java
                test```
                ```java
                test
                ```""";
        var expected = """
                Test
                ```java
                test``````java
                test
                ```""";
        Assertions.assertEquals(expected, Text.compressCodeBlocks(blocks));
    }

    @Test
    void compressChunks() {
        List<String> chunks = List.of("", "test", "test2", "", "test");
        List<String> result = Text.compressChunks(chunks, 2000, "\n");
        Assertions.assertEquals(1, result.size());
        List<String> expected = List.of("\ntest\ntest2\n\ntest");
        Assertions.assertEquals(expected, result);
        result = Text.compressChunks(chunks, 11, "\n");
        Assertions.assertEquals(2, result.size());
        expected = List.of("\ntest\ntest2\n", "test");
        Assertions.assertEquals(expected, result);
    }
}
