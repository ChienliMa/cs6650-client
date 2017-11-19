package edu.neu.cs6650_clients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogAnalyzer {
    public static String analyze(List<Log> logs, long wallTime) {
        int errorCount = 0;
        ArrayList<Long> dbLatencies = new ArrayList<Long>();
        ArrayList<Long> latencies = new ArrayList<Long>();
        for (Log log: logs) {
            switch (log.getStatus()) {
                case "ERROR":
                    errorCount += 1;
                    break;
                case "SUCC":
                    dbLatencies.add(log.getDbTime());
                    latencies.add(log.getTotalTime());
                    break;
            }
        }
        
        Collections.sort(latencies);
        Collections.sort(dbLatencies);
                
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("requests received: %d \n", latencies.size() + errorCount));
        stringBuilder.append(String.format("requests failed: %d \n", errorCount));
        stringBuilder.append(String.format("requests successed: %d \n", latencies.size()));
        stringBuilder.append(String.format("successful req throughput: %d req/s\n", latencies.size()*1000/wallTime));
        stringBuilder.append("\n");
        
        long totalTime = 0;
        for (Long latency: latencies) totalTime += latency;
        stringBuilder.append(String.format("service mean: %d ms\n", totalTime/latencies.size()));
        stringBuilder.append(String.format("service mdian: %d ms\n", latencies.get(latencies.size()/2)));
        stringBuilder.append(String.format("service 95th percentile: %d ms\n", latencies.get((int) (latencies.size()*0.95))));
        stringBuilder.append(String.format("service 99th percentile: %d ms\n", latencies.get((int) (latencies.size()*0.99))));
        stringBuilder.append("\n");
        
        totalTime = 0;
        for (Long latency: dbLatencies) totalTime += latency;
        stringBuilder.append(String.format("db mean: %d ms\n", totalTime/dbLatencies.size()));
        stringBuilder.append(String.format("db mdian: %d ms\n", dbLatencies.get(dbLatencies.size()/2)));
        stringBuilder.append(String.format("db 95th percentile: %d ms\n", dbLatencies.get((int) (dbLatencies.size()*0.95))));
        stringBuilder.append(String.format("db 99th percentile: %d ms\n", dbLatencies.get((int) (dbLatencies.size()*0.99))));
        return stringBuilder.toString();
    }
}