package edu.neu.cs6650_clients;

import java.util.concurrent.CountDownLatch;

public abstract class GenaralRunnable{
	  protected ResortClient client;
	  protected Profiler profiler;
	  protected CountDownLatch latch = null;
	  
	  public GenaralRunnable(ResortClient client, Profiler profiler, CountDownLatch latch) {
		this.client = client;
	    this.profiler = profiler;
	    this.latch = latch;
	  }
}