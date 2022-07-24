package ptt.crawler;

import org.jsoup.select.Elements;
import ptt.crawler.model.*;
import ptt.crawler.config.Config;


import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;



public class HttpClientReader {
    //private HttpClient httpClient;
    private CloseableHttpClient httpClient;
    //private final Map<String, List<Cookie>> cookieStore; // 保�?? Cookie
    //private final CookieJar cookieJar;
    

    public HttpClientReader() throws ClientProtocolException, IOException {
        /* ??��?��?? */
//        cookieStore = new HashMap<>();
//        cookieJar = new CookieJar() {
//            @Override
//            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
//                List<Cookie> cookies = cookieStore.getOrDefault(
//                    httpUrl.host(), 
//                    new ArrayList<>()
//                );
//                cookies.addAll(list);
//                cookieStore.put(httpUrl.host(), cookies);
//            }
//            
//            /* 每次?��?�帶上儲存�?? Cookie */
//            @Override
//            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
//                return cookieStore.getOrDefault(
//                    httpUrl.host(), 
//                    new ArrayList<>()
//                );
//            }
//        };
//        httpClient = new HttpClient.Builder().cookieJar(cookieJar).build();

        /* ?��得網站�?��?��?? Cookie */
    	System.out.println("000");
        httpClient = HttpClients.createDefault();
        HttpGet  httpGet  = new HttpGet (Config.PTT_URL);
        httpClient.execute(httpGet);
        System.out.println("111");
        //Request request = new Request.Builder().get().url(Config.PTT_URL).build();
        //okHttpClient.newCall(request).execute();
    }

    public List<Article> getList(String boardName) throws IOException, ParseException {
        Board board = Config.BOARD_LIST.get(boardName);
        System.out.println("123");
        /* 如�?�找不到??��?��?��?�板 */
        if (board == null) {
            return null;
        }
        System.out.println("234");
        /* 如�?��?�板??要�?�年檢查 */
    
        if (board.getAdultCheck() == true) {
            runAdultCheck(board.getUrl());
        }
        System.out.println("345");
        /* ??��?�目標�?�面 */

        
        BasicCookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("over18", "1");
        cookie.setDomain("www.ptt.cc");
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "true");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        
        HttpGet httpGet = new HttpGet(Config.PTT_URL + board.getUrl());
        System.out.println("444");
        System.out.println(Config.PTT_URL + board.getUrl());
        //HttpContext httpContext = new BasicHttpContext();
        List<Article> result = new ArrayList<>();
        try {
        CloseableHttpResponse response = httpClient.execute(httpGet,context);
        EntityUtils.consumeQuietly(response.getEntity());
        System.out.println("455");
        HttpEntity entity=response.getEntity();
        String body=EntityUtils.toString(entity);
        System.out.println("456");
        response.close();
		httpClient.close();
        
		
        /* 轉�?? HTML ?�� Article */
        List<Map<String, String>> articles = parseArticle(body);
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
        System.out.println("567");
        for (Map<String, String> article: articles) {
            String url = article.get("url");
            String title = article.get("title");
            String author = article.get("author");
            Date date = simpleDateFormat.parse(article.get("date"));

            result.add(new Article(board, url, title, author, date));
        }
        System.out.println("678");
        
        
        } catch (Exception e) {
            System.out.print("http GET 请求异常" + e);
        }
        return result;
    }

    /* ?��?�年齡確�? */
    private void runAdultCheck(String url) throws IOException {
//        FormBody formBody = new FormBody.Builder()
//            .add("from", url)
//            .add("yes", "yes")
//            .build();
//
//        Request request = new Request.Builder()
//            .url(Config.PTT_URL + "/ask/over18")
//            .post(formBody)
//            .build();
//
//        okHttpClient.newCall(request).execute();
        
        HttpPost httpPost = new HttpPost(Config.PTT_URL + "/ask/over18");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("from", url));
        params.add(new BasicNameValuePair("yes", "yes"));
        StringEntity entity=new UrlEncodedFormEntity(params,"utf-8");
        httpPost.setEntity(entity);
        //httpPost.setEntity(new UrlEncodedFormEntity(params));

        httpClient.execute(httpPost);
        
    }

    /* �???��?�板??��?��?�表 */
    private List<Map<String, String>> parseArticle(String body) {
        List<Map<String, String>> result = new ArrayList<>();
        Document doc = Jsoup.parse(body);
        Elements articleList = doc.select(".r-ent");

        for (Element element: articleList) {
            String url = element.select(".title a").attr("href");
            String title = element.select(".title a").text();
            String author = element.select(".meta .author").text();
            String date = element.select(".meta .date").text();
            Map m = new HashMap<>();
            m.put("url", url);
            m.put("title", title);
            m.put("author", author);
            m.put("date", date);
            result.add(m);
        }

        return result;
    }
}