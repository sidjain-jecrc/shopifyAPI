package edu.asu.shopifyapi;

import edu.asu.shopifyapi.rsclient.ShopifyRestClient;
import edu.asu.shopifyapi.util.JsonParserResultStore;

public class ShopifyDriver {

	public static void main(String[] args) {

		ShopifyRestClient client = new ShopifyRestClient();
		JsonParserResultStore result = client.retrieveAndProcessAllOrders();

		System.out.println("Total Order Count: " + result.getOrderCount());
		System.out.println("Unique Customers Count: " + result.getUniqueCustomerCount());
		System.out.println("Most Frequent Item Ordered: " + result.getMostFrequentOrderItem());
		System.out.println("Least Frequent Item Ordered: " + result.getLeastFrequentOrderItem());
		System.out.println("Median Order Value: " + result.getMedianOrderValue());
		System.out.println("Shortest Interval Between Orders (millis): " + result.getShortestInterval());
	}

}
