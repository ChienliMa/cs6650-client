package edu.neu.cs6650_clients;

import java.net.URI;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ClientErrorException;

/**
 * Hello world!
 *
 */
import javax.ws.rs.client.*;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.xml.crypto.Data;

import javassist.expr.NewArray;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.concurrent.*;
import java.util.zip.Adler32;
import java.util.*;

class MyClient {
	private Client client;
	private int n_threads, n_iters;
	private String ip, port;
	private WebTarget target;
	private ArrayList<Integer> latencies;
	
	public MyClient(int n_threads, int n_iters, String ip, String port) {
		this.n_threads = n_threads;
		this.n_iters = n_iters;
		this.ip = ip;
		this.port = port;
		
		this.client = ClientBuilder.newClient();
		this.target = client.target("http://" + this.ip + ":" + this.port);
		this.latencies = new ArrayList<Integer>();
		
		System.out.format("client nthread: %d, niters: %d, ip: %s, port: %s\n", n_threads, n_iters, ip, port);
	}
	
	private void printMsgAndTime(String msg) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.print(msg);
		System.out.println(dateFormat.format(date));
	}
	
	private Runnable getTask() {
		return new Runnable() {
			public void run() {
				long start, end;
				WebTarget target = client.target("http://" + ip + ":" + port + "/cs6650/assignment/hw1/");
				long id = System.currentTimeMillis();
				for (int i = 0; i < n_iters; i += 1) {
//					System.out.format("thread: %d, iter: %d \r", id, i);
					Response response = null;
					start = System.currentTimeMillis();
					try {
						response = target.request(MediaType.TEXT_PLAIN_TYPE).get();
						end = System.currentTimeMillis();
						addLaterncy((int)(end-start));
					} catch (Exception e) {
					} finally {
						if (response != null) {
							response.close();							
						}
					}
					
					start = System.currentTimeMillis();
					try {
						response = target.request(MediaType.TEXT_PLAIN_TYPE)
								.post(Entity.entity("some randmo text", MediaType.TEXT_PLAIN));
						end = System.currentTimeMillis();
						addLaterncy((int)(end-start));
					} catch (Exception e) {
					} finally {
						if (response != null) {
							response.close();							
						}
					}
				}
			}
		};
	}
	
	private synchronized void addLaterncy(int laterncy) {
		this.latencies.add(laterncy);
	}
	
	private synchronized void analysePerformance() {
		Collections.sort(latencies);
		
		long totalTime = 0;
		for (int latency: latencies) totalTime += latency;


		System.out.format("mean: %d ms\n", totalTime/latencies.size());
		System.out.format("mdian: %d ms\n", latencies.get(latencies.size()/2));
		System.out.format("95th percentile: %d ms\n", latencies.get((int) (latencies.size()*0.95)));
		System.out.format("99th percentile: %d ms\n", latencies.get((int) (latencies.size()*0.99)));
		
	}
	
	public <T> T postText(Object requestEntity, Class<T> responseType) throws ClientErrorException{
		return this.target.request(MediaType.TEXT_PLAIN)
					.post(Entity.entity(requestEntity, MediaType.TEXT_PLAIN), 
							responseType);
	}
	
	public String getStatus() throws ClientErrorException{
		return this.target.request(MediaType.TEXT_PLAIN).get(String.class);
	}
	
	public void run() {
		
		long start = System.currentTimeMillis();
		printMsgAndTime("Client starts at: ");

		ArrayList<Thread> threads = new ArrayList<Thread>();
		for(int i = 0; i<this.n_threads; i += 1) {
			threads.add(new Thread(getTask()));
		}
		
		for (Thread thread: threads) {
			thread.start();
		}
		printMsgAndTime("All threads running at: ");
		
		for (Thread thread:threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.format("Requests sent: %d\n", n_threads * n_iters * 2);
		System.out.format("Requests successed: %d\n", latencies.size());
		System.out.format("Total wall time: %d s\n", (end-start)/1000);
		analysePerformance();
	}
}

public class HW1Client {
	public static void main(String [] args) {
		MyClient myClient = new MyClient(100, 100, "52.53.211.82", "8080");
		myClient.run();
	};
}
