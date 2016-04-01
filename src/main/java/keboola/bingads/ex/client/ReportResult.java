/*
 */
package keboola.bingads.ex.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import keboola.bingads.ex.utils.CsvUtils;

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
        return this.lastSync;
    }

    @Override
    public File getResultFile() {
        return this.resultFile;
    }

    @Override
    public void cleanupCSV() throws ResultException {

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

        resultFile.delete();

        outFile.renameTo(resultFile);

        /*Truncate copyright notice and empty lines at the end of the file*/
        char[] currLine = null;
        int headerLength = 0;

        try {
            headerLength = CsvUtils.getHeaderLength(resultFile, ",".charAt(0), "\"".charAt(0));
        } catch (Exception ex) {
            Logger.getLogger(ReportResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        RandomAccessFile f;
        try {
            f = new RandomAccessFile(resultFile, "rw");
            long length = 0;
            //read lines from the end until the line with correct number of columns            
            do {
                currLine = readLineWithNLBackWards(f);
                length = f.getFilePointer();
            } while (headerLength != CsvUtils.getCsvColumnLength(new String(currLine), ",".charAt(0), "\"".charAt(0)));

            f.setLength(length + currLine.length + 1);
            f.close();
        } catch (FileNotFoundException ex) {
            throw new ResultException("Unable to proccess final report result data for report " + this.resultFile + " " + ex.getMessage(), 2);
        } catch (IOException ex) {
            throw new ResultException("Unable to proccess final report result data for report " + this.resultFile + " " + ex.getMessage(), 2);
        } catch (Exception ex) {
            throw new ResultException("Unable to proccess final report result data for report " + this.resultFile + " " + ex.getMessage(), 2);
        }

    }

    private char[] readLineWithNL(FileInputStream in) throws IOException {
        try {
            int hLen = 0;

            ArrayList<Character> chars = new ArrayList();
            int ch = in.read();
            chars.add((char) ch);
            //continue until NL character
            while (!isNL(ch)) {
                ch = in.read();
                chars.add((char) ch);
            }
            boolean isNl = true;
            //skip other remaining NL chars until the start of next line
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
            throw ex;

        }
    }

    /**
     * Reads line backwards from the end of a file including new line
     * characters. Sets the file pointer of RandomAccessFile object
     *
     * @param f - RandomAccessFile object
     * @return
     */
    private char[] readLineWithNLBackWards(RandomAccessFile f) throws IOException {

        long length = f.length() - 1;
        byte b;
        //set to current pointer if not at the end
        if (f.getFilePointer() < length && f.getFilePointer() > 0) {
            length = f.getFilePointer() - 1;
        }

        ArrayList<Character> chars = new ArrayList();
        f.seek(length);
        int ch = f.read();
        length--;

        chars.add((char) ch);
        //continue until NL character
        while (!isNL(ch)) {
            f.seek(length);
            ch = f.read();
            chars.add((char) ch);
            length--;
        }
        //reached new line
        boolean isNl = true;
        //skip other remaining NL chars until the start of next line
        while (isNl) {
            f.seek(length);
            ch = f.read();
            if (isNL(ch)) {
                chars.add((char) ch);
                isNl = true;
            } else {
                isNl = false;
            }
            length--;

        }
        char[] charArray = new char[chars.size()];
        for (int i = 0; i < chars.size(); i++) {
            charArray[i] = chars.get(i);
        }
        return charArray;//in.getChannel().position() - 1;

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
