/*
 */
package keboola.bingads.ex.state;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import esnerda.keboola.components.appstate.LastState;

public class BingLastState implements LastState {

	@JsonProperty("lastRunDate")
	private Date lastRun;

	public BingLastState(Date lastRun) {
		this.lastRun = lastRun;
	}

	public BingLastState() {
	}

	public Date getLastRun() {
		return lastRun;
	}

}
