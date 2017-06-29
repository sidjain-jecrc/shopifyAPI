package edu.asu.shopifyapi.rsclient;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import edu.asu.shopifyapi.util.IShopifyConfig;
import edu.asu.shopifyapi.util.JsonParserResultStore;

/**
 * @author Siddharth
 * 
 *         This class is the REST client that connects with Shopify API Endpoins
 * 
 *
 */
public class ShopifyRestClient {

	private Client client = null;
	private WebResource webResource = null;
	private JSONParser parser = null;

	public ShopifyRestClient() {
		client = Client.create();
		webResource = client.resource(IShopifyConfig.BASE_URI);
		parser = new JSONParser();
	}

	public JsonParserResultStore retrieveAndProcessAllOrders() {

		int page = 1;
		boolean hasOrders = true;
		JsonParserResultStore resultStore = new JsonParserResultStore();

		while (hasOrders) {
			String jsonString = getAllOrders(page++);
			hasOrders = hasOrdersToProcess(jsonString);

			if (hasOrders) {
				resultStore.processOrderJson(jsonString);				
			} else {
				break;
			}
		}
		return resultStore;
	}

	private String getAllOrders(int page) {

		String jsonString = null;

		try {

			WebResource.Builder webBuilder = webResource.path(IShopifyConfig.ALL_ORDER_ENDPOINT)
					.queryParam("page", String.valueOf(page))
					.queryParam("status", IShopifyConfig.STATUS_ORDER)
					.queryParam("limit", IShopifyConfig.LIMIT_RESULT)
					.accept(MediaType.APPLICATION_JSON)
					.header(IShopifyConfig.TOKEN_KEY, IShopifyConfig.TOKEN_PASSWORD);

			ClientResponse response = webBuilder.get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			jsonString = response.getEntity(String.class);

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}

		return jsonString;
	}

	private boolean hasOrdersToProcess(String jsonString) {

		try {
			JSONObject rootObject = (JSONObject) parser.parse(jsonString);
			JSONArray orders = (JSONArray) rootObject.get("orders");

			if (orders.size() > 0) {
				return true;
			}

		} catch (ParseException e) {
			System.out.println("Parsing Exception: " + e.getMessage());
		}
		return false;
	}

}
