/*
 */
package keboola.bingads.ex.client;

import java.io.File;
import java.util.Date;

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

}
