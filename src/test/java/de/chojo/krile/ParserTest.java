package de.chojo.krile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {
    private static final ObjectMapper MAPPER = YAMLMapper.builder()
            .build();

    @Test
    public void test() throws JsonProcessingException {
        var test = """
                ---
                id: test
                tag: test tag
                ---
                random text
                more random text
                # Header
                ## Sub Header
                                
                1. List
                2. List 2
                3. List 3
                test: two
                test: three
                """;
        TagMeta tagMeta = MAPPER.readValue(test, TagMeta.class);
        Assertions.assertEquals("test", tagMeta.id());
        Assertions.assertEquals("test tag", tagMeta.tag());
    }
}
