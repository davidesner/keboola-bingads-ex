/*
 */
package keboola.bingads.ex.state;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class JsonlStateWriter {

    public static void writeStateFile(String resultStateFilePath, LastState lstate) throws IOException {

        final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        File stateFile = new File(resultStateFilePath);
        mapper.writeValue(stateFile, lstate);
    }

}
