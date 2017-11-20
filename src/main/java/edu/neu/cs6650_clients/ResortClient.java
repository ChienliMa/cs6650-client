package edu.neu.cs6650_clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ResortClient {
	private Client client;
	private String prefix;
	
	public ResortClient(String ip, String port) {
		String uri = "/cs6650/resort";
		this.client = ClientBuilder.newClient().property("http.receive.timeout", 5000);
		if (!port.equals("null")) {
			this.prefix = "http://" + ip + ":" + port + uri;
		} else {
			this.prefix = "http://"  + ip + uri;
		}
		
	}
	
	private WebTarget newTarget(String uri) {
		return this.client.target(this.prefix + uri);
	}
		
	public String load(int resortID, int dayNum, int skierID, int liftID, int timestamp) 
			throws Exception{
		WebTarget target  = this.newTarget("/load");
		target = target.queryParam("resortID", resortID)
				.queryParam("dayNum", dayNum)
				.queryParam("timestamp", timestamp)
				.queryParam("skierID", skierID)
				.queryParam("liftID", liftID);
		
		Response response = target.request(MediaType.TEXT_PLAIN_TYPE).post(null);
		if (response.getStatus()/500 == 5) {
			System.out.println(response.getEntity());
			response.close();
			throw new Exception("500 server error");
		}
		String rval =response.readEntity(String.class); 
		response.close();
		return rval;
	}
	
	public String myVert(int skierID, int dayNum) throws Exception{
		WebTarget target  = this.newTarget("/myvert");
		target = target.queryParam("skierID", skierID).queryParam("dayNum", dayNum);
		
		Response response = target.request(MediaType.TEXT_PLAIN_TYPE).get();
		if (response.getStatus()/500 == 5) {
			System.out.println(response.getEntity());
			response.close();
			throw new Exception("500 server error");
		}
		String rval =response.readEntity(String.class); 
		response.close();
		return rval;
	}
}
