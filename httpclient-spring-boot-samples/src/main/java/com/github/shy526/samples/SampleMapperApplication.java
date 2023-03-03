package com.github.shy526.samples;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        List<JSONObject> jdCategory = new ArrayList<>();
        String categoryUrl = "https://dc.3.cn/category/get?callback=getCategoryCallback";
        String categoryStr =getHttpStr(categoryUrl);
        //region 提取分类数据
        Matcher callbackMatcher = parseJsonp(categoryStr, "getCategoryCallback");
        if (callbackMatcher == null) return;
        JSONObject data = JSON.parseObject(callbackMatcher.group(1));
        jdCategory = data.getJSONArray("data").toJavaList(JSONObject.class);
        //endregion

        List<JSONObject> categorys = parseCategory(jdCategory);


        /*
         * 怕封ip 以下面的为例子 一个类别中的一页为例
         * 家用电器/冰箱
         * list.jd.com/list.html?cat=737,794,878
         */
        Document document = parseHtml("https://list.jd.com/list.html?cat=737,794,878&page=1", null);

        if (document == null) {
            return;
        }
        Elements productElements = document.select("#J_goodsList").select("li");
        List<JSONObject> productInfos = parseProductInfo(productElements);

        //提取异步参数
        Map<String, String> myParams = getSyncParams(document);
        if (myParams.isEmpty()) {
            return;
        }

        String url = "https://list.jd.com/listNew.php";
        Document syncDocument = parseHtml(url, myParams);
        productInfos.addAll(parseProductInfo(syncDocument.select("li")));
        String detailsUrlFormat = "https://item-soa.jd.com/getWareBusiness?callback=jQuery2947888&skuId=%s&shopId=%s&venderId=%s";
        for (JSONObject item : productInfos) {
            // Document productDocument = parseHtml("https:" + item, null);
            /**
             * 抓取库存-sku
             * https://cd.jd.com/stocks?callback=jQuery8510611&type=getstocks&skuIds=100024204895
             */
            String shopId = item.getString("shopId");
            String detailsUrl = String.format(detailsUrlFormat, item.getString("skuId"), shopId, shopId);
            String detailsStr = getHttpStr(detailsUrl);
            Matcher detailsMatcher = parseJsonp(detailsStr, "jQuery2947888");
            JSONObject details = JSONObject.parseObject(detailsMatcher.group(1));
            item.put("details", details);
            System.out.println("callbackMatcher = " + details);
            break;

        }
    }

    private List<JSONObject> parseCategory(List<JSONObject> jdCategory) {
        List<JSONObject> category = new ArrayList<>();
        for (JSONObject item : jdCategory) {
            JSONObject itemJson = new JSONObject();
            itemJson.put("id", item.getString("id"));
            //leftPut 左侧投放目标页面
            itemJson.put("leftPut", getJdCategoryItem(item.getJSONArray("p")));
            //business 商家
            itemJson.put("business", getJdCategoryItem(item.getJSONArray("b")));
            //顶部投放也 titlePut
            itemJson.put("titlePut", getJdCategoryItem(item.getJSONArray("t")));
            //获取子分类
            itemJson.put("subCategory", getSubCategory(item));
            System.out.println("itemJSON = " + itemJson);
            category.add(itemJson);
        }
        return category;
    }


    /**
     * 获取网络字符串
     * @param  url
     * @return str
     */
    private String getHttpStr(String url) {
        String str = "";
        try (HttpResult httpResult = httpClientService.get(url)) {
            str = httpResult.getEntityStr();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * JSONP数据提取
     * @param categoryStr JSONP字符串
     * @param callbackFunc 函数名
     * @return Matcher
     */
    private  Matcher parseJsonp(String categoryStr, String callbackFunc) {
        Pattern callbackPattern = Pattern.compile(callbackFunc + "*\\((.*)\\)");
        Matcher callbackMatcher = callbackPattern.matcher(categoryStr);

        if (!callbackMatcher.find()) {
            return null;
        }
        return callbackMatcher;
    }

    private List<JSONObject> parseProductInfo(Elements productElements) {
        List<JSONObject> productList = new ArrayList<>();
        for (Element productElement : productElements) {
            String skuId = productElement.attr("data-sku");
            String shopId = productElement.select(".p-img>div").attr("data-venid");
            String href = productElement.select(".p-img>a").attr("href");
            JSONObject product = new JSONObject();
            product.put("skuId", skuId);
            product.put("shopId", shopId);
            product.put("url", "https:" + href);
            productList.add(product);

        }

        return productList;
    }

    /**
     * 提取页面中异步加载的参数
     *
     * @param document
     * @return
     */
    private static Map<String, String> getSyncParams(Document document) {
        Map<String, String> myParams = new HashMap<>();
        Elements scriptElements = document.getElementsByTag("script");
        Pattern scriptPattern = Pattern.compile(".*LogParm=(\\{.*\\})\\;searchUnit.*");
        JSONObject syncParams = null;
        for (Element scriptElement : scriptElements) {
            String scriptStr = scriptElement.toString().replaceAll("\\s+", "");
            Matcher matcher = scriptPattern.matcher(scriptStr);
            if (matcher.find()) {
                syncParams = JSON.parseObject(matcher.group(1));
                break;
            }

        }
        if (syncParams == null) {
            return myParams;
        }
        /**
         *   https://list.jd.com/listNew.php?cat=737%2C794%2C878
         *   &page=2
         *   &s=27
         *   &scrolling=y
         *   &log_id=1675496345853.2686
         *   &tpl=1_M
         *   &isList=1
         *   &show_items=100025634254,100012791656,10041346466524,100012312692,100044554263,100001962610,100023695592,100016393634,65583414051,10049002263215,100020868346,100045891771,100028368508,10044594631349,10031106000068,100037271852,990705,100004328824,2929717,10062452261872,10030700943551,100042019864,100033246688,100035718864,34613194867,10033142604818,100049303049,10066062466330,10067521663815,10036958693268
         */
        myParams.put("page", "2");
        myParams.put("cat", "cat=737,794,878");
        myParams.put("s", "27");
        myParams.put("scrolling", "y");
        myParams.put("log_id", syncParams.getString("log_id"));
        myParams.put("tpl", "1_M");
        myParams.put("isList", "1");
        myParams.put("show_items", syncParams.getJSONObject("search000014_log").getString("wids"));
        return myParams;
    }

    /**
     * 获取document
     *
     * @return Document
     */
    private Document parseHtml(String url, Map<String, String> params) {

        try (HttpResult httpResult = httpClientService.get(url, params)) {
            return Jsoup.parse(httpResult.getEntityStr());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 递归解析子分类
     *
     * @param item item
     * @return List<JSONObject>
     */
    private List<JSONObject> getSubCategory(JSONObject item) {
        JSONArray subCategory = item.getJSONArray("s");
        List<JSONObject> result = new ArrayList<>(subCategory.size());
        for (Object temp : subCategory) {
            JSONObject tempSub = (JSONObject) temp;
            JSONObject sub = buildJsonObj(tempSub.getString("n"));
            sub.put("subCategory", getSubCategory(tempSub));
            result.add(sub);
        }
        return result;
    }

    /**
     * 解析jd页面json
     *
     * @param itemAttr itemAttr
     * @return List<JSONObject>
     */
    private List<JSONObject> getJdCategoryItem(JSONArray itemAttr) {
        List<JSONObject> result = new ArrayList<>();
        for (Object str : itemAttr) {
            result.add(buildJsonObj(str.toString()));
        }
        return result;
    }

    /**
     * 构建基础对象
     *
     * @param str str
     * @return JSONObject
     */
    private JSONObject buildJsonObj(String str) {
        String[] split = str.split("\\|");
        JSONObject temp = new JSONObject();
        temp.put("name", split[1]);
        temp.put("img", split[2]);
        temp.put("url", split[0]);
        return temp;
    }


}