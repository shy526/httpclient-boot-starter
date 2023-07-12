package com.github.shy526.samples;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.shy526.http.HttpClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class C5GameAnalysis extends Analysis {

    private final static String LIST_URL = "https://www.c5game.com/napi/trade/search/v2/items/570/search?limit=500&appId=570&page=%s&sort=0";

    private final static String SELL_URL = "https://www.c5game.com/napi/trade/steamtrade/sga/sell/v3/list?itemId=%s&page=1&limit=1000";

    private final static String BUY_URL = "https://www.c5game.com/napi/trade/steamtrade/sga/purchase/v2/list?itemId=%s&page=1&limit=1000";

    private final static String DAY_SALE_URL = "https://www.c5game.com/napi/trade/steamtrade/sga/store/v2/price-chart-new?itemId=553484888&period=3&styleId=";

    private final static URLCodec CODEC = new URLCodec();

    @Autowired
    public C5GameAnalysis(HttpClientService httpClientService) {
        super(httpClientService);
    }

    @Override
    public List<Jewelry> process() {
        List<Jewelry> result = new ArrayList<>();
        for (int page = 1; ; page++) {
            String url = String.format(LIST_URL, page);
            JSONObject urlResult = getJSONObject(url);
            JSONObject data = urlResult.getJSONObject("data");
            if (data==null){
                log.error(url+"-> data is null");
                continue;
            }
            JSONArray jsonArray = data.getJSONArray("list");
            if (jsonArray.isEmpty()) {
                break;
            }
            for (Object o : jsonArray) {
                JSONObject item = (JSONObject) o;
                String itemName = item.getString("itemName");
                String shorName = item.getString("shortName");
                String id = item.getString("id");
                JSONObject itemInfo = item.getJSONObject("itemInfo");
                String quality = itemInfo.getString("qualityName");
                String marketHashName = item.getString("marketHashName");
                try {
                    String hashName = CODEC.encode(marketHashName, CharEncoding.UTF_8).replaceAll("\\+", "%20");
                } catch (UnsupportedEncodingException e) {
                }
                Jewelry jewelry = new Jewelry();
                jewelry.setQuality(quality);
                jewelry.setName(shorName);
                jewelry.setMarketHashName(marketHashName);
                jewelry.putId(this.getClass().getSimpleName(), id);
                result.add(jewelry);
            }
            sleep();
        }
        for (Jewelry jewelry : result) {
            sellBuyProcess(jewelry);
            sleep();
        }
        return result;
    }

    @Override
    public void sellBuyProcess(Jewelry jewelry) {
        String key = this.getClass().getSimpleName();
        JSONArray temp = getJSONObject(SELL_URL, jewelry.getId(key)).getJSONObject("data").getJSONArray("list");
        ItemPrice sellPrice = calculatePrice(temp, "cnyPrice", null);
        temp= getJSONObject(BUY_URL, jewelry.getId(key)).getJSONObject("data").getJSONArray("list");
        ItemPrice buyPrice = calculatePrice(temp, "cnyPrice", "remainNum");
        jewelry.putSellPrice(key,sellPrice);
        jewelry.putBuyPrice(key,buyPrice);
    }

}
