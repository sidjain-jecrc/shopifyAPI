package edu.asu.shopifyapi.util;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParserResultStore {

	private static long numOfOrders = 0;

	private static Map<String, Long> customerOrderTimeMap = new HashMap<String, Long>();
	private static Map<Long, Long> productQuantityMap = new HashMap<Long, Long>();
	private static ArrayList<BigDecimal> orderPriceList = new ArrayList<BigDecimal>();

	private static long mostFrequentItem = 0;
	private static long maxProductCountInOrders = Long.MIN_VALUE;
	private static long leastFrequentItem = 0;
	private static long minProductCountInOrders = Long.MAX_VALUE;
	private static long shortestIntervalBtwOrders = Long.MAX_VALUE;

	private static JSONParser parser = null;

	public JsonParserResultStore() {
		parser = new JSONParser();
	}

	public void processOrderJson(String jsonString) {

		try {

			JSONObject rootObject = (JSONObject) parser.parse(jsonString);
			JSONArray orders = (JSONArray) rootObject.get("orders");

			for (int i = 0; i < orders.size(); i++) {

				JSONObject order = (JSONObject) orders.get(i);

				String custEmail = (String) order.get("email");
				String orderProcessedTime = (String) order.get("created_at");
				ZonedDateTime orderTime = ZonedDateTime.parse(orderProcessedTime);
				long orderTimeMillis = orderTime.toInstant().toEpochMilli();

				if (!customerOrderTimeMap.containsKey(custEmail)) {
					customerOrderTimeMap.put(custEmail, orderTimeMillis);
				} else {
					long currentInterval = customerOrderTimeMap.get(custEmail) - orderTimeMillis;
					if (currentInterval < shortestIntervalBtwOrders) {
						shortestIntervalBtwOrders = currentInterval;
					}
					customerOrderTimeMap.put(custEmail, orderTimeMillis);
				}

				String totalPrice = (String) order.get("total_price");
				orderPriceList.add(new BigDecimal(totalPrice));

				JSONArray products = (JSONArray) order.get("line_items");
				for (int j = 0; j < products.size(); j++) {

					JSONObject product = (JSONObject) products.get(j);
					long productId = (Long) product.get("product_id");
					long productQuantity = (Long) product.get("quantity");

					if (productQuantityMap.containsKey(productId)) {
						productQuantityMap.put(productId, productQuantityMap.get(productId) + productQuantity);

						long productCountInOrders = productQuantityMap.get(productId);
						if (productCountInOrders > maxProductCountInOrders) {
							maxProductCountInOrders = productCountInOrders;
							mostFrequentItem = productId;
						}

						if (productCountInOrders < minProductCountInOrders) {
							minProductCountInOrders = productCountInOrders;
							leastFrequentItem = productId;
						}

					} else {
						productQuantityMap.put(productId, productQuantity);
					}
				}

				numOfOrders++;
			}

		} catch (ParseException ex) {
			System.out.println("Parsing Exception: " + ex.getMessage());
		}
	}

	public long getOrderCount() {
		return numOfOrders;
	}

	public long getUniqueCustomerCount() {
		return (long) customerOrderTimeMap.size();
	}

	public long getMostFrequentOrderItem() {
		return mostFrequentItem;
	}

	public long getLeastFrequentOrderItem() {
		return leastFrequentItem;
	}

	public BigDecimal getMedianOrderValue() {

		Collections.sort(orderPriceList);
		int mid = (int) (numOfOrders / 2);

		if (numOfOrders % 2 == 0) {
			return orderPriceList.get(mid);
		} else {
			BigDecimal sum = orderPriceList.get(mid).add(orderPriceList.get(mid - 1));
			BigDecimal divisor = new BigDecimal(2.0);
			return sum.divide(divisor, 2);
		}
	}

	public long getShortestInterval() {
		return shortestIntervalBtwOrders;
	}

}
