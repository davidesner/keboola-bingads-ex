/*
 */
package keboola.bingads.ex.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class FileHandler {

    public static void deleteFile(String filePath) throws IOException {
        File file = new File(filePath);
        //make sure file exists
        if (!file.exists()) {
            throw new IOException("File not exists");
        }
        if (file.isDirectory()) {
            //directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {
                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    deleteFile(fileDelete.getAbsolutePath());
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                }
            }

        } else {
            //if file, then delete it
            file.delete();
        }
    }

    public static void deleteFiles(List<String> filePaths) throws IOException {
        for (String fPath : filePaths) {
            File file = new File(fPath);
            deleteFile(fPath);

        }
    }

    public static void deleteFilesInFolder(Collection<String> fileNames, String folder) throws IOException {
        for (String fname : fileNames) {
            File file = new File(folder + File.separator + fname);
            deleteFile(folder + File.separator + fname);

        }
    }
}
