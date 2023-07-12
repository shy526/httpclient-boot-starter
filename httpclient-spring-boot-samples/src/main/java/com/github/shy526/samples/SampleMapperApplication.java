package com.github.shy526.samples;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.net.URLCodec;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Map<String, Analysis> analysisMap = applicationContext.getBeansOfType(Analysis.class);
        for (Analysis value : analysisMap.values()) {
            List<Jewelry> process = value.process();
            System.out.println("process = " + process);
        }

/*        String listUrl = "https://www.c5game.com/napi/trade/search/v2/items/570/search?limit=42&appId=570&page=1&sort=0";
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
            String marketHashName = item.getString("marketHashName");
            String hashName = codec.encode(marketHashName, CharEncoding.UTF_8).replaceAll("\\+", "%20");


            //出售
            String sellUrl = "https://www.c5game.com/napi/trade/steamtrade/sga/sell/v3/list?itemId=%s&page=1&limit=1000";
            JSONArray sellList = getJSONObject(sellUrl, id).getJSONObject("data").getJSONArray("list");
            ItemPrice c5SellPrice = calculatePrice(sellList, "cnyPrice", null);
            //收购
            String buyUrl = "https://www.c5game.com/napi/trade/steamtrade/sga/purchase/v2/list?itemId=%s&page=1&limit=1000";
            JSONArray buyList =getJSONObject(buyUrl, id).getJSONObject("data").getJSONArray("list");
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
        }*/


          /*
            //steam 售卖页面
            String steamUrl = "https://steamcommunity.com/market/listings/570/%s" ;

            Document steamHtml = getDocument(steamUrl, hashName);
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
         //获取在售卖的信息
            String steamBuyUrl = "https://steamcommunity.com/market/itemordershistogram?country=CN&language=schinese&currency=23&item_nameid=%s&two_factor=0";
            JSONObject buySell = getJSONObject(steamBuyUrl, steamId);
            if (buySell != null) {
                JSONArray buArray = buySell.getJSONArray("buy_order_graph");
                ItemPrice buyPrice = calculatePrice(buArray, "0", "1");
                JSONArray sellArray = buySell.getJSONArray("sell_order_graph");
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
            }*/

    }






}