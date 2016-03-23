/*
 */
package keboola.bingads.ex.client;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class BulkResult implements ApiDownloadResult {

    private File resultFile;
    private Date lastSync;

    public BulkResult(File resultFile) throws Exception {

        FileReader freader = null;

        if (resultFile == null) {
            throw new Exception("");
        }
        this.resultFile = resultFile;
        //proccess and cleanup the result file
        try {
            String[] line;
            String[] headerLine;
            freader = new FileReader(resultFile);
            CSVReader csvreader = new CSVReader(freader, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
            //header
            line = csvreader.readNext();
            int syncPos = 0;
            //find position of sync field
            while (!line[syncPos].equals("Sync Time")) {
                syncPos++;
            }
            line = csvreader.readNext();
            while (line[syncPos].equals("")) {
                line = csvreader.readNext();
            }
            setLastSync(line[syncPos]);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BulkResult.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(BulkResult.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BulkResult.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                freader.close();
            } catch (IOException ex) {

            }
        }
        cleanupCSV();

    }

    private void setLastSync(String dateString) throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.lastSync = format.parse(dateString);
    }

    @Override
    public Date getLastSync() {
        return this.lastSync;
    }

    @Override
    public File getResultFile() {
        return this.resultFile;
    }

    @Override
    public void cleanupCSV() {

    }

}
