package esnerda.keboola.components.result;

import java.util.List;

/**
 * @author David Esner
 */
public interface IResultWriter<T> {
	public void initWriter(String path, Class<T> clazz) throws Exception;
	public void writeResult(T obj) throws Exception;
	public void writeAllResults(List<T>objs) throws Exception;
	public List<ResultFileMetadata> closeAndRetrieveMetadata() throws Exception;	
	public List<ResultFileMetadata> writeAndRetrieveResuts(List<T> objs) throws Exception;

	
}
