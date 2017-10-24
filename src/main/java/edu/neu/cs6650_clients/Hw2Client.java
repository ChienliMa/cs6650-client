package edu.neu.cs6650_clients;

import javax.swing.text.Highlighter.Highlight;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
import javax.ws.rs.client.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javassist.compiler.ast.Variable;

import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

class ResortClient {
	private Client client;
	private String prefix;
	
	public ResortClient(String ip, String port) { 
		this.client = ClientBuilder.newClient().property("http.receive.timeout", 5000);;
		this.prefix = "http://" + ip + ":" + port + "/cs6650/assignment/hw2";
	}
	
	private WebTarget newTarget(String uri) {
		return this.client.target(this.prefix + uri);
	}
		
	public String load(int resortID, int dayNum, int skierID, int liftID, int timestamp) 
			throws javax.ws.rs.ProcessingException{
		WebTarget target  = this.newTarget("/load");
		target = target.queryParam("resortID", resortID)
				.queryParam("dayNum", dayNum)
				.queryParam("timestamp", timestamp)
				.queryParam("skierID", skierID)
				.queryParam("liftID", liftID);
		
		Response response = target.request(MediaType.TEXT_PLAIN_TYPE).post(null);
		String rval =response.readEntity(String.class); 
		response.close();
		return rval;
	}
	
	public String myVert(int skierID, int dayNum) throws javax.ws.rs.ProcessingException{
		WebTarget target  = this.newTarget("/myvert");
		target = target.queryParam("skierID", skierID).queryParam("dayNum", dayNum);
		
		Response response = target.request(MediaType.TEXT_PLAIN_TYPE).get();
		String rval =response.readEntity(String.class); 
		response.close();
		return rval;
	}
}


class Profiler {
	private ConcurrentHashMap<Long, ArrayList<Integer>> threadLaterncies = new ConcurrentHashMap<Long, ArrayList<Integer>>(); 
	private long tryCount = 0;
	
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

class ConcurrentCsvReader {
	private BufferedReader buffer = null;
	private int batchSize = 1000;
	private int count = 0;
	public ConcurrentCsvReader(String fileName, int batchSize) {
		try {
			this.buffer = new BufferedReader(new FileReader(fileName));
			this.buffer.readLine();
			this.batchSize = batchSize;
		} catch (FileNotFoundException e) {
			System.out.println("Unable to load file");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized ArrayList<String> readLines(){
		ArrayList<String> rval = new ArrayList<String>();
		if (this.buffer != null) { 
			try {
				String line = null;
				for (int i = 0; i++ < this.batchSize;) {
					line = this.buffer.readLine();
					if (line == null) {
						break;
					}
					rval.add(line);
				}
				if (rval.size() != 0) {
					this.count += rval.size();
					System.out.printf("read:  %d\n", this.count);
				}
			} catch (IOException e) {
			}
		}
		
		return rval;
	}
}
abstract class GenaralRunnable{
	  protected ResortClient client;
	  protected Profiler profiler;
	  protected CountDownLatch latch = null;
	  
	  public GenaralRunnable(ResortClient client, Profiler profiler, CountDownLatch latch) {
		this.client = client;
	    this.profiler = profiler;
	    this.latch = latch;
	  }
}

class Step4Runnable extends GenaralRunnable implements Runnable {
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
					}
				}
				csvLines = this.reader.readLines();;
			}
		} finally {
			synchronized (this.latch) {
				System.out.println(this.latch.getCount());
				this.latch.countDown();	
			}
		}
	}
}

class Step5Runnable extends GenaralRunnable implements Runnable {
	private int startId;
	private int iters;
	public Step5Runnable(int iters, ResortClient client, Profiler profiler, CountDownLatch latch, int startId) {
		super( client, profiler, latch);
		this.startId = startId;
		this.iters = iters;
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
					
				}
		    		
		    }	
		} finally {
			synchronized (this.latch) {
				System.out.println(this.latch.getCount());
				this.latch.countDown();	
			}
		}
	}
}

public class Hw2Client {
	public static void main(String [] args) {
		int readThreadCount, writeThreadCount, maxId;
		String ip, port;
		
		writeThreadCount = Integer.parseInt(args[0]);
		readThreadCount = Integer.parseInt(args[1]);
		maxId = Integer.parseInt(args[2]);
		int iters = maxId/readThreadCount;
		
		ip = args[3];
		port = args[4];
		System.out.format("client r_thread: %d, w_thread: %d, r_iters: %d, ip: %s, port: %s\n", readThreadCount, writeThreadCount, iters, ip, port);
		
		ConcurrentCsvReader csvReader = null;
		if (args.length > 5 && writeThreadCount > 0) {
			csvReader = new ConcurrentCsvReader(args[5], 1000);
			System.out.format("Loading csv: %s\n", args[4]);
		}
		
		Profiler profiler = new Profiler();
		ResortClient client = new ResortClient(ip, port);
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		int latchCount = readThreadCount + writeThreadCount;
		CountDownLatch latch = new CountDownLatch(latchCount);
		
		for(int thread_i = 0; thread_i ++< writeThreadCount;) {
			threads.add(new Thread(new Step4Runnable(client, profiler, latch, csvReader)));
		}
	
		for(int thread_i = 0; thread_i ++< readThreadCount;) {
			threads.add(new Thread(new Step5Runnable(iters, client, profiler, latch, thread_i * iters + 1)));
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
	};
}
