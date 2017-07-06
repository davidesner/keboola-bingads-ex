/*
 */
package keboola.bingads.ex.client;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class BulkResult implements ApiDownloadResult {
	
	private final static int ACC_ID_POSITION = 2;

    private File resultFile;
    private Date lastSync;
    private Long accId;

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
            //set accId
            line = csvreader.readNext();
            this.accId = new Long(line[ACC_ID_POSITION]);
            

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
        final String lineSep=System.getProperty("line.separator");
        File file2 = new File(resultFile.getAbsolutePath()+"1.csv");
        try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(resultFile)));
             BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2)));){
            

           
            String line = null;
            //modify header
			line = br.readLine();
			bw.write(line + "," + "accId" + lineSep);
			for (line = br.readLine(); line != null; line = br.readLine()) {
				bw.write(line + "," + accId + lineSep);
			}

        }catch(Exception e){
            System.out.println("Failed to modify files.");
            System.exit(2);
        }
        resultFile.delete();
        resultFile = file2;
    }

}
