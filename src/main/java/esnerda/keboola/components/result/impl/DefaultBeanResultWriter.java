package esnerda.keboola.components.result.impl;

import java.util.Collections;
import java.util.List;

import esnerda.keboola.components.result.AbstractBeanResultWriter;
import esnerda.keboola.components.result.ResultFileMetadata;

/**
 * @author David Esner
 */
public class DefaultBeanResultWriter<T> extends AbstractBeanResultWriter<T> {

	private final String fileName;
	private final String[] idCols;
	public DefaultBeanResultWriter(String resFileName, String [] idCols) {
		this.fileName = resFileName;
		this.idCols = idCols;
	}
	@Override
	public List<ResultFileMetadata> closeAndRetrieveMetadata() throws Exception {
		close();
		return Collections.singletonList(new ResultFileMetadata(resFile, idCols, null));
	}

	@Override
	public String getFileName() {
		return fileName;
	}

}
