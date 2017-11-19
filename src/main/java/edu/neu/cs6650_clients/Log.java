package edu.neu.cs6650_clients;

public class Log {
	private  String status = null;
	private  Long timeMillis  = null;
	private  Long dbTime  = null;
	private  Long totalTime  = null;
	
	public Log(String status, Long timeMillis, Long dbTime, Long totalTime) {
		this.timeMillis = timeMillis;
		this.status = status;
		this.dbTime = dbTime;
		this.totalTime = totalTime;
	}
	
	public Log(String status, Long dbTime, Long totalTime) {
		this.status = status;
		this.dbTime = dbTime;
		this.totalTime = totalTime;
	}
	
	public Long getTimeMillis() {
		return timeMillis;
	}
	
	public String getStatus() {
		return status;
	}

	public Long getDbTime() {
		return dbTime;
	}

	public Long getTotalTime() {
		return totalTime;
	}
}
