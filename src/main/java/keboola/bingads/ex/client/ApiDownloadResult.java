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
public interface ApiDownloadResult {

    public Date getLastSync();

    public File getResultFile();

    public void cleanupCSV();

}
