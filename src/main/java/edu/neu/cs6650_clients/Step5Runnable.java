package edu.neu.cs6650_clients;

import java.util.concurrent.CountDownLatch;

public class Step5Runnable extends GenaralRunnable implements Runnable {
	private int startId;
	private int iters;
	private int day;
	public Step5Runnable(int iters, ResortClient client, Profiler profiler, CountDownLatch latch, int startId, int day) {
		super( client, profiler, latch);
		this.startId = startId;
		this.iters = iters;
		this.day = day;
	}
	
	public void run() {
		try {
			long start, end;
			long threadId = Thread.currentThread().getId();
		    for(int i = 0; i++ < this.iters; ) {
		    		
		    		profiler.tryHit();
		    		try {
		    			start = System.currentTimeMillis();
			    		client.myVert(this.startId + i ,1);
			    		end = System.currentTimeMillis();
			    		this.profiler.addLatency(threadId, end- start);
				} catch (javax.ws.rs.ProcessingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    		
		    }	
		} finally {
			synchronized (this.latch) {
				this.latch.countDown();	
			}
		}
	}
}