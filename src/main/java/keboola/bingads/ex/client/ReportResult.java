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
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class ReportResult implements ApiDownloadResult {

    private File resultFile;
    private Date lastSync;

    public ReportResult(File resultFile, Date lastSync) throws Exception {
        this.resultFile = resultFile;
        this.lastSync = lastSync;

        cleanupCSV();

    }

    public void setLastSync(Date lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public Date getLastSync() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getResultFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cleanupCSV() {

        BufferedReader reader = null;
        String line = "";

        //create output file
        File outFile = new File(resultFile.getParent() + File.separator + "tempRes");

        FileChannel out = null;
        FileOutputStream fout = null;

        FileInputStream fis = null;
        try {
            //retrieve file header
            fis = new FileInputStream(resultFile);
            //reader = new BufferedReader(new InputStreamReader(fis));
            //move position 10 lines(info length) to the header
            for (int i = 0; i < 9; i++) {
                readLineWithNL(fis);
            }

            fout = new FileOutputStream(outFile);
            out = fout.getChannel();

            //Write the rest of the file using NIO
            FileChannel in = fis.getChannel();

            //set position to header
            long pos = in.position() - 1;

            for (long p = pos, l = in.size(); p < l;) {
                p += in.transferTo(p, l - p, out);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReportResult.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportResult.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                //reader.close();
                fis.close();
                out.close();
            } catch (IOException ex) {
            }
        }
        String fname = resultFile.getName();
        resultFile.delete();

        outFile.renameTo(resultFile);
        resultFile = outFile;

    }

    private char[] readLineWithNL(FileInputStream in) {
        try {
            int hLen = 0;

            ArrayList<Character> chars = new ArrayList();
            int ch = in.read();
            chars.add((char) ch);
            while (!isNL(ch)) {
                ch = in.read();
                chars.add((char) ch);
            }
            boolean isNl = true;
            while (isNl) {
                ch = in.read();
                if (isNL(ch)) {
                    chars.add((char) ch);
                    isNl = true;
                } else {
                    isNl = false;
                }
                hLen++;
            }
            char[] charArray = new char[chars.size()];
            for (int i = 0; i < chars.size(); i++) {
                charArray[i] = chars.get(i);
            }
            return charArray;//in.getChannel().position() - 1;
        } catch (IOException ex) {
            Logger.getLogger(ReportResult.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isNL(int character) {
        if ((character == -1)) {
            return false;
        } else {
            return ((((char) character == '\n')
                    || ((char) character == '\r')));
        }
    }

}
