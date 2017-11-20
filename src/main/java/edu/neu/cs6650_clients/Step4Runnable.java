package edu.neu.cs6650_clients;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Step4Runnable extends GenaralRunnable implements Runnable {
	private ConcurrentCsvReader reader;
	public Step4Runnable(ResortClient client, Profiler profiler, CountDownLatch latch, ConcurrentCsvReader reader) {
		super(client, profiler, latch);
		this.reader = reader;
	}
	
	public void run() {
		try {
			long start, end;
			long threadId = Thread.currentThread().getId();
			ArrayList<String> csvLines = this.reader.readLines();;
			while (csvLines.size() > 0) {					
				for(String line: csvLines) {
					profiler.tryHit();
					String[] params = line.split(",");
					int[] p = new int[params.length];
					for (int i = 0; i < params.length; i++) { p[i] = Integer.parseInt(params[i].trim()); }
					try {
						start = System.currentTimeMillis();
						client.load(p[0],p[1],p[2],p[3],p[4]);
						end = System.currentTimeMillis();
						this.profiler.addLatency(threadId, end- start);
					} catch (javax.ws.rs.ProcessingException e) {	
						System.out.println(e.getMessage());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				csvLines = this.reader.readLines();;
			}
		} finally {
			synchronized (this.latch) {
				this.latch.countDown();	
			}
		}
	}
}
