package edu.neu.cs6650_clients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class Profiler {
	private ConcurrentHashMap<Long, ArrayList<Integer>> threadLaterncies = new ConcurrentHashMap<Long, ArrayList<Integer>>(); 
	private long tryCount = 0;
	private String taksName;
	
	public Profiler(String taksName) {
		this.taksName = taksName;
	}
	
	public synchronized void tryHit() {
		this.tryCount += 1;
	}
	
	public void addLatency(long threadId, long latency) {
		if (this.threadLaterncies.get(threadId) == null) {
			this.threadLaterncies.put(threadId, new ArrayList<Integer>());
		}
		this.threadLaterncies.get(threadId).add((int)(latency));
	}
	
	public void printAnalyzation(long wallTime) {
		ArrayList<Integer> latencies = new ArrayList<Integer>();
		for (Long threadId: this.threadLaterncies.keySet()) {
		    latencies.addAll(this.threadLaterncies.get(threadId));
		}
		
		Collections.sort(latencies);
		
		long totalTime = 0;
		for (long latency: latencies) totalTime += latency;
		System.out.printf("==== TASK : %s ====\n", this.taksName);
		System.out.printf("wall time: %.3f s\n", wallTime/1000.0);
		System.out.printf("requests tried: %d \n", this.tryCount);
		System.out.printf("requests successed: %d \n", latencies.size());
		System.out.printf("throughput: %d req/s\n", latencies.size()*1000/wallTime);
		System.out.format("mean: %d ms\n", totalTime/latencies.size());
		System.out.format("mdian: %d ms\n", latencies.get(latencies.size()/2));
		System.out.format("95th percentile: %d ms\n", latencies.get((int) (latencies.size()*0.95)));
		System.out.format("99th percentile: %d ms\n", latencies.get((int) (latencies.size()*0.99)));
	}
}