package esnerda.keboola.components.result;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;


/**
 * @author David Esner
 */
/**
 * Basic abstract implementation of result writer using Super CSV
 * 
 *
 * @param <T>
 */
public abstract class AbstractBeanResultWriter<T> implements IResultWriter<T>, Closeable{
	
	protected File resFile;
	protected ICsvBeanWriter writer = null;
	protected  CellProcessor[] cellProcessors;
	protected String[] header;
	protected BufferedWriter bw;
	protected FileWriter fw;

	@Override
	public void initWriter(String path, Class<T> clazz) throws Exception {
		initHeader(clazz.newInstance());
		resFile = new File(path + File.separator + getFileName());
		fw = new FileWriter(resFile);
		bw = new BufferedWriter(fw);
		this.writer = new CsvBeanWriter(bw, CsvPreference.STANDARD_PREFERENCE);			
		cellProcessors = getProcessors(getHeader().length);
		writer.writeHeader(getHeader());		
	}

	@Override
	public void writeResult(T obj) throws Exception {
		if (obj == null) {
			return;
		}
		writer.write(obj, getHeader(), cellProcessors);		
	}

	@Override
	public void writeAllResults(List<T> objs) throws Exception {
		if (objs == null) {
			return;
		}
		for (T o : objs){
			writeResult(o);
		}		
	}

	@Override
	public List<ResultFileMetadata> writeAndRetrieveResuts(List<T> objs) throws Exception {
		writeAllResults(objs);		
		return closeAndRetrieveMetadata();
	}

	@Override
	public void close() throws IOException {
		bw.flush();
		writer.flush();
		writer.close();
		if (bw != null) {
			bw.close();
		}
		if (fw != null) {
			fw.close();
		}		
	}

	/* get cell processors with dynamic size */
	protected CellProcessor[] getProcessors(int length) {
		CellProcessor[] processors = new CellProcessor[length];
		for (int i = 0; i < length; i++) {
			processors[i] = new Optional();
		}
		return processors;
	}

	protected void initHeader(T type) {
		List<String> fieldList = new ArrayList<String>();
		for (Field field : type.getClass().getDeclaredFields()) {
			fieldList.add(field.getName());
		}
		this.header = fieldList.toArray(new String[0]);
	}

	protected String[] getHeader() throws Exception {
		if (header != null) {
			return header;
		}
		throw new Exception("Writer is not properly initialized!");
	}

	public abstract String getFileName();


}
