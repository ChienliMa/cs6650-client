package edu.neu.cs6650_clients;

import java.util.ArrayList;
import java.util.List;

public class DashBoardClient {
	public static void printMetrics(Long start, Long end) {
		// send extra log to dump logs
        List<Log> logs = new ArrayList<Log>();
        logs = LogDao.getLogWithinTime(start, end);
        System.out.println(LogAnalyzer.analyze(logs, end - start)); 
	}
	
	public static void main(String [] args) {
		DashBoardClient.printMetrics(0l, 100l);
	}
}
