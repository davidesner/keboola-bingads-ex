package esnerda.keboola.components.result;

import java.io.File;

/**
 * @author David Esner
 */
public class ResultFileMetadata {

	private final File resFile;
	private final String[] idColums;
	private final String[] columns;

	public ResultFileMetadata(File resFile, String[] idColums, String[] columns) {
		super();
		this.resFile = resFile;
		this.idColums = idColums;
		this.columns = columns;
	}

	public File getResFile() {
		return resFile;
	}

	public String[] getIdColums() {
		return idColums;
	}

	public String[] getColumns() {
		return columns;
	}

}
