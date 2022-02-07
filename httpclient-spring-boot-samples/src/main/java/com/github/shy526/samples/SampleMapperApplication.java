package com.github.shy526.samples;


import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

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
        System.out.println("myHttp = " + myHttp);
        System.out.println("httpClientService = " + httpClientService);
        String cid = "204315";
        String nowImgUrl = "https://xmanhua.com/m%s/chapterimage.ashx?cid=%s&page=1&key=";
        Map<String, String> header = new HashMap<>(1);
        header.put("referer", "https://xmanhua.com/");
        HttpResult httpResult = httpClientService.get(String.format(nowImgUrl, cid, cid), null, header);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        ScriptObjectMirror eval = (ScriptObjectMirror) engine.eval(httpResult.getEntityStr());
        if (eval != null) {
            eval.forEach((k, v) -> System.out.println(v));
        }

        //获取漫画信息start
        final HttpResult mPage = httpClientService.get("https://xmanhua.com/5800xm/");
        Document doc = Jsoup.parse(mPage.getEntityStr());
        Elements detailInfo = doc.select(".detail-info");
        final Elements tip = detailInfo.select(".detail-info-tip>span");
        for (Element item : tip) {
            String text = item.text();
            String[] split = text.split("：");
            if ("作者".equals(split[0])) {
                System.out.println(item.select("a").text());
            } else if ("狀態".equals(split[0])) {

                System.out.println(item.select("span>span").text());
            } else if ("題材".equals(split[0])) {
                System.out.println(item.select(".item").text());
            }
        }
        String content = doc.select(".detail-info-content").text();
        String cover = detailInfo.select(".detail-info-cover").attr("str");
        //获取漫画信息end

        //爬取章节信息start
        final Elements select = doc.select("#chapterlistload>a");
        for (int i = select.size() - 1; i >= 0; i--) {
            final Element element = select.get(i);
            String mcid = element.attr("href").replaceAll("/", "");
            String p = element.select("span").text().replaceAll("P", "");
            String title = element.attr("title");
            System.out.println(mcid + ":" + element.ownText() + "(" + title + ")->" + p);
        }
        //end
        String page = "https://xmanhua.com/manga-list-0-0-p%s/";
        for (int i = 0; ; i++) {
            HttpResult httpResult1 = httpClientService.get(String.format(page, i));
            final String entityStr = httpResult1.getEntityStr();
            if (StringUtils.isEmpty(entityStr)) {
                break;
            }
            Elements xElements = Jsoup.parse(entityStr).select(".mh-item");
            xElements.forEach(item -> {
                String mCover = item.select("mh-cover").attr("src");
                Elements itemDetali = item.select(".title>a");
                String mId = itemDetali.attr("href").replaceAll("/", "");
                String xTitle = itemDetali.text();
                System.out.println(mId + ":" + xTitle);
            });

        }


    }


}