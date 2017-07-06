/*
 */
package keboola.bingads.ex.config.tableconfig;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class ManifestBuilder {

    public static void buildManifestFile(ManifestFile file, String folderPath, String resFileName) throws IOException {
        /*Build manifest file*/
        File resFile = new File(folderPath + File.separator + resFileName + ".manifest");
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.writeValue(resFile, file);

//        String manifest = "destination: " + resFileName + "\n"
//                + "incremental: " + file.isIncremental() + "\n"
//                + "delimiter: \"" + file.getDelimiter() + "\"\n"
//                + "enclosure: \"" + file.getEnclosure() + "\"\n";
//        if (file.getPrimaryKey() != null) {
//            manifest += "primary_key: \"" + file.getPrimaryKey() + "\"";
//        }
//
//        Files.write(manifest, resFile, Charset.forName("UTF-8"));
    }
}
