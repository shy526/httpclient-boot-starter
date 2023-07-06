package com.github.shy526.samples;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import com.github.shy526.http.RequestPack;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author shy526
 */

@SpringBootApplication
@Slf4j
public class SampleMapperApplication implements CommandLineRunner {

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    @Qualifier("myHttp")
    private HttpClientService myHttp;


    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String listUrl = "https://www.c5game.com/napi/trade/search/v2/items/570/search?limit=42&appId=570&page=1&sort=0";
        HttpResult httpResult = httpClientService.get(listUrl);
        String entityStr = httpResult.getEntityStr();
        JSONObject data = JSON.parseObject(entityStr).getJSONObject("data");
        JSONArray list = data.getJSONArray("list");
        URLCodec codec = new URLCodec();
        Pattern steamIdPattern = Pattern.compile("Market_LoadOrderSpread\\(\\s*(\\d+)\\s*\\)");
        for (Object o : list) {
            JSONObject item = (JSONObject) o;
            String itemName = item.getString("itemName");
            String shorName = item.getString("shortName");
            String id = item.getString("id");
            JSONObject itemInfo = item.getJSONObject("itemInfo");
            String quality = itemInfo.getString("qualityName");

            String hashName = codec.encode(item.getString("marketHashName"), CharEncoding.UTF_8).replaceAll("\\+", "%20");
            String steamUrl = "https://steamcommunity.com/market/listings/570/" + hashName;

            HttpResult steamResult = httpClientService.get(steamUrl);
            String entityStr2 = steamResult.getEntityStr();

            System.out.println(steamUrl);
            if (!StringUtils.isNotEmpty(entityStr2)) {
                System.out.println(steamUrl+":"+steamResult.getHttpStatus());
                return;
            }
            Document steamHtml = Jsoup.parse(entityStr2);
            Elements script = steamHtml.body().select("script");
            String steamId = null;
            for (Element element : script) {
                String str = element.outerHtml();
                if (!StringUtils.isNotEmpty(str)) {
                    return;
                }
                Matcher matcher = steamIdPattern.matcher(str);
                if (matcher.find()) {
                    steamId = matcher.group(1);
                }
            }
            String steamBuyUrl = "https://steamcommunity.com/market/itemordershistogram?country=CN&language=schinese&currency=23&item_nameid=%s&two_factor=0";
            HttpResult steamBuyResult = httpClientService.get(String.format(steamBuyUrl, steamId));
            JSONObject jsonObject = JSON.parseObject(steamBuyResult.getEntityStr());
            if (jsonObject != null) {
                JSONArray buArray = jsonObject.getJSONArray("buy_order_graph");
                ItemPrice buyPrice = calculatePrice(buArray, "0", "1");
                JSONArray sellArray = jsonObject.getJSONArray("sell_order_graph");
                ItemPrice sellPrice = calculatePrice(buArray, "0", "1");
            } else {
                String sellUrl = "https://steamcommunity.com/market/listings/570/%s/render/?query=&start=0&count=100&country=CN&language=schinese&currency=23";
                HttpResult httpResult1 = httpClientService.get(String.format(sellUrl, hashName));
                Document parse = Jsoup.parse(JSON.parseObject(httpResult1.getEntityStr()).getString("results_html"));
                Elements select = parse.body().select(".market_listing_price_with_fee");
                JSONArray array = new JSONArray();
                for (Element element : select) {
                    String price = element.html().replaceAll("\\s+|¥", "");
                    JSONObject addObj = new JSONObject();
                    addObj.put("price", price);
                    array.add(addObj);
                }
                ItemPrice itemPrice = calculatePrice(array, "price", null);
                System.out.println("itemPrice = " + itemPrice);
            }

            //出售
            String sellUrl = "https://www.c5game.com/napi/trade/steamtrade/sga/sell/v3/list?itemId=%s&page=1&limit=1000";
            HttpResult sellResult = httpClientService.get(String.format(sellUrl, id));
            JSONArray sellList = JSONObject.parseObject(sellResult.getEntityStr()).getJSONObject("data").getJSONArray("list");
            ItemPrice c5SellPrice = calculatePrice(sellList, "cnyPrice", null);
            //收购
            String buyUrl = "https://www.c5game.com/napi/trade/steamtrade/sga/purchase/v2/list?itemId=%s&page=1&limit=1000";
            HttpResult buyResult = httpClientService.get(String.format(buyUrl, id));
            JSONArray buyList = JSONObject.parseObject(sellResult.getEntityStr()).getJSONObject("data").getJSONArray("list");
            ItemPrice c5buyPrice = calculatePrice(sellList, "cnyPrice", "remainNum");
            Jewelry jewelry = new Jewelry();
            jewelry.setId(id);
            // jewelry.setBuyPrice(buyPrice);
            //   jewelry.setSellPrice(sellPrice);
            jewelry.setName(shorName);
            jewelry.setQuality(quality);
            Map<String, String> header = new HashMap<>();
            header.put("Platform", "2");
            //最近交易记录
            //最近30天交易记录
            String daySale = "https://www.c5game.com/napi/trade/steamtrade/sga/store/v2/price-chart-new?itemId=553484888&period=3&styleId=";
            HttpResult httpResult1 = httpClientService.get(daySale, null, header);

            String entityStr1 = httpResult1.getEntityStr();
            System.out.println("httpResult1 = " + httpResult1);
            System.out.println(jewelry);
        }

    }

    private ItemPrice calculatePrice(JSONArray prices, String priceKey, String numKey) {
        BigDecimal minPrice = BigDecimal.valueOf(Integer.MAX_VALUE);
        BigDecimal maxPrice = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalNum = BigDecimal.ZERO;
        for (Object o : prices) {
            JSONObject item = null;
            if (o instanceof JSONArray) {
                JSONArray array = (JSONArray) o;
                item = new JSONObject();
                for (int i = 0; i < array.size(); i++) {
                    item.put(String.valueOf(i), array.get(i));
                }
            } else {
                item = (JSONObject) o;
            }
            BigDecimal price = item.getBigDecimal(priceKey);
            BigDecimal num = null;
            if (numKey != null) {
                num = item.getBigDecimal(numKey);
            }
            num = num == null ? BigDecimal.ONE : num;
            totalPrice = totalPrice.add(price.multiply(num));
            totalNum = totalNum.add(num);
            if (price.compareTo(maxPrice) > 0) {
                maxPrice = price;
            } else if (price.compareTo(minPrice) < 0) {
                minPrice = price;
            }
        }
        BigDecimal avgPrice = totalPrice.divide(totalNum, 2, RoundingMode.HALF_UP);
        ItemPrice itemPrice = new ItemPrice();
        itemPrice.setAvgPrice(avgPrice);
        itemPrice.setMaxPrice(maxPrice);
        itemPrice.setMinPrice(minPrice);
        itemPrice.setNum(totalNum);
        return itemPrice;
    }

    @Data
    static class Jewelry {
        private ItemPrice buyPrice;
        private ItemPrice sellPrice;
        private String name;
        private String quality;
        private String id;
    }

    @Data
    static class ItemPrice {
        private BigDecimal maxPrice;
        private BigDecimal minPrice;

        private BigDecimal avgPrice;

        private BigDecimal num;
    }

}