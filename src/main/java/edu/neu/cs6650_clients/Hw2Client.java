package edu.neu.cs6650_clients;

import java.util.concurrent.CountDownLatch;

import java.util.*;

public class Hw2Client {
	public static void main(String [] args) {
		int readThreadCount, writeThreadCount, maxId;
		String ip, port;
		
		writeThreadCount = Integer.parseInt(args[0]);
		readThreadCount = Integer.parseInt(args[1]);
		maxId = Integer.parseInt(args[2]);
		int iters = readThreadCount==0?0:(maxId/readThreadCount);
		
		ip = args[3];
		port = args[4];
		System.out.format("client r_thread: %d, w_thread: %d, r_iters: %d, ip: %s, port: %s\n", readThreadCount, writeThreadCount, iters, ip, port);
		
		ConcurrentCsvReader csvReader = null;
		if (args.length > 5 && writeThreadCount > 0) {
			csvReader = new ConcurrentCsvReader(args[5], 100);
			System.out.format("Loading csv: %s\n", args[4]);
		}
		
		Profiler profiler = new Profiler("Combined workload");
		ResortClient client = new ResortClient(ip, port);
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		int latchCount = readThreadCount + writeThreadCount;
		CountDownLatch latch = new CountDownLatch(latchCount-2);
		
		for(int thread_i = 0; thread_i ++< writeThreadCount;) {
			threads.add(new Thread(new Step4Runnable(client, profiler, latch, csvReader)));
		}
	
		for(int thread_i = 0; thread_i ++< readThreadCount;) {
			threads.add(new Thread(new Step5Runnable(iters, client, profiler, latch, thread_i * iters + 1,1)));
		}

		long start = System.currentTimeMillis();
		try {
			for (Thread thread: threads) {thread.start();}
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		
		
		System.out.format("client r_thread: %d, w_thread: %d, r_iters: %d, ip: %s, port: %s\n", readThreadCount, writeThreadCount, iters, ip, port);
		profiler.printAnalyzation((end-start));
		DashBoardClient.printMetrics(System.currentTimeMillis()-100000, System.currentTimeMillis());

	};
}
