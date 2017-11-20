package edu.neu.cs6650_clients;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConcurrentCsvReader {
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