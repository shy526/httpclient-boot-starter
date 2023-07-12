package com.github.shy526.samples;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Jewelry {
    private Map<String, ItemPrice>  buyPrice=new HashMap<>();
    private Map<String,ItemPrice >  sellPrice=new  HashMap<>();
    private String name;
    private String quality;

    private String marketHashName;
    private Map<String, String> idMap = new HashMap<>();

    public void putId(String key, String id) {
        idMap.put(key, id);
    }

    public String getId(String key) {
        return idMap.get(key);
    }


    public void putBuyPrice(String key, ItemPrice price) {
        buyPrice.put(key, price);
    }

    public ItemPrice getBuyPrice(String key) {
        return buyPrice.get(key);
    }

    public void putSellPrice(String key, ItemPrice price) {
        sellPrice.put(key, price);
    }

    public ItemPrice getSellPrice(String key) {
        return sellPrice.get(key);
    }
}
