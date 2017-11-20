package edu.neu.cs6650_clients;

import java.util.concurrent.CountDownLatch;

import java.util.*;

public class Hw3Client {
	public static void main(String [] args) {
//		step1(args);
		step3(args);
	};
	
	private static void step1(String [] args) {
		int readThreadCount, writeThreadCount, maxId;
		String ip, port;
		
		writeThreadCount = Integer.parseInt(args[0]);
		readThreadCount = Integer.parseInt(args[1]);
		maxId = Integer.parseInt(args[2]);
		int iters = readThreadCount==0?0:(maxId/readThreadCount);
		ip = args[3];
		port = args[4];
		
		ResortClient client = new ResortClient(ip, port);
		System.out.format("client r_thread: %d, w_thread: %d, r_iters: %d, ip: %s, port: %s\n", readThreadCount, writeThreadCount, iters, ip, port);
		String testFile =  "/Users/ChienliMa/Desktop/cs6650/HW3/Day999.csv";
		
		long start, end;
		
		start = System.currentTimeMillis();
		Profiler loadProfiler = new Profiler("POST");
		load(client, loadProfiler, testFile, writeThreadCount);
		end = System.currentTimeMillis();
		
		System.out.format("client r_thread: %d, w_thread: %d, r_iters: %d, ip: %s, port: %s\n", readThreadCount, writeThreadCount, iters, ip, port);
		loadProfiler.printAnalyzation((end-start));
		start = System.currentTimeMillis();
		Profiler getProfiler = new Profiler("GET");
		read(client, getProfiler, maxId, readThreadCount,1);
		end = System.currentTimeMillis();
		getProfiler.printAnalyzation((end-start));
	}
	
	private static void step3(String [] args) {
		int readThreadCount, writeThreadCount, maxId;
		String ip, port;
		
		writeThreadCount = Integer.parseInt(args[0]);
		readThreadCount = Integer.parseInt(args[1]);
		maxId = Integer.parseInt(args[2]);
		int iters = readThreadCount==0?0:(maxId/readThreadCount);
		ip = args[3];
		port = args[4];
		
		Profiler postProfiler = new Profiler("STEP3  POST");
		Profiler getProfiler = new Profiler("STEP3  GET");
		ResortClient client = new ResortClient(ip, port);
		System.out.format("client r_thread: %d, w_thread: %d, r_iters: %d, ip: %s, port: %s\n", readThreadCount, writeThreadCount, iters, ip, port);
		String fileTemplate =  "/Users/ChienliMa/Desktop/cs6650/HW3/Day%d.csv";
		long start = System.currentTimeMillis();
		
		long postWallTime = 0l;
		long getWallTime = 0l;
		for (int i = 3;i < 6; i+= 1) {
			long taskStart = System.currentTimeMillis();
			load(client, postProfiler, String.format(fileTemplate, i), writeThreadCount);
			postWallTime += (System.currentTimeMillis() - taskStart);
			
			taskStart = System.currentTimeMillis();
			read(client, getProfiler, maxId, readThreadCount,i-1);
			getWallTime += (System.currentTimeMillis() - taskStart);
		}
		
		long end = System.currentTimeMillis();
		
		System.out.format("client r_thread: %d, w_thread: %d, r_iters: %d, ip: %s, port: %s\n", readThreadCount, writeThreadCount, iters, ip, port);
		postProfiler.printAnalyzation(postWallTime);
		
		getProfiler.printAnalyzation(getWallTime);
		
		System.out.println("");
		DashBoardClient.printMetrics(start, end);
	}
	
	private static void read(ResortClient client, Profiler profiler, int maxId, int threadCount, int day) {
		int latchCount = threadCount;
		CountDownLatch latch = new CountDownLatch(latchCount);
		ArrayList<Thread> threads = new ArrayList<Thread>();
		int iters = maxId/threadCount;
		for(int thread_i = 0; thread_i ++< threadCount;) {
			threads.add(new Thread(new Step5Runnable(iters, client, profiler, latch, thread_i * iters + 1, day)));
		}

		try {
			for (Thread thread: threads) {thread.start();}
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static void load(ResortClient client, Profiler profiler, String filePath, int threadCount) {
		ConcurrentCsvReader csvReader = null;
		csvReader = new ConcurrentCsvReader(filePath, 100);
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		CountDownLatch latch = new CountDownLatch(threadCount);
		for(int thread_i = 0; thread_i ++< threadCount;) {
			threads.add(new Thread(new Step4Runnable(client, profiler, latch, csvReader)));
		}
	
		try {
			for (Thread thread: threads) {thread.start();}
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
