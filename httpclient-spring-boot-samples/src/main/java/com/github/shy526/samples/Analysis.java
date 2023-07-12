package com.github.shy526.samples;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class Analysis {

    protected HttpClientService httpClientService;

    public Analysis(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    public abstract List<Jewelry> process();

    public abstract void sellBuyProcess(Jewelry jewelry);


    /**
     * 解析网页
     *
     * @param urlFormat
     * @param params
     * @return
     */
    protected Document getDocument(String urlFormat, String... params) {
        String url = String.format(urlFormat, params);
        String entityStr = "";
        try {
            log.error(url);
            HttpResult httpResult = httpClientService.get(url);
            if (httpResult.getHttpStatus() != 200) {
                log.error(url + " -> " + httpResult.getHttpStatus());
            }
            String temp = httpResult.getEntityStr();
            entityStr = temp == null ? entityStr : temp;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Jsoup.parse(entityStr);
    }


    /**
     * 解析网页
     *
     * @param urlFormat
     * @param params
     * @return
     */
    protected JSONObject getJSONObject(String urlFormat, String... params) {
        String url = String.format(urlFormat, params);
        String entityStr = "";
        try {
            log.error(url);
            HttpResult httpResult = httpClientService.get(url);
            if (httpResult.getHttpStatus() != 200) {
                log.error(url + " -> " + httpResult.getHttpStatus());
            }
            String temp = httpResult.getEntityStr();
            entityStr = temp == null ? entityStr : temp;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return JSON.parseObject(entityStr);
    }


    protected ItemPrice calculatePrice(JSONArray prices, String priceKey, String numKey) {
        BigDecimal minPrice = BigDecimal.valueOf(Integer.MAX_VALUE);
        BigDecimal maxPrice = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalNum = BigDecimal.ZERO;
        if (prices.isEmpty()) {
            return null;
        }
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

    protected void sleep() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
        }
    }
}
