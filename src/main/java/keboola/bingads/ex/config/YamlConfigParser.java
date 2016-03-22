/*
 */
package keboola.bingads.ex.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class YamlConfigParser {

    public static KBCConfig parseFile(File file) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(file, KBCConfig.class);
    }

    public static Object parseFile(File file, Class type) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(file, type);
    }
}
