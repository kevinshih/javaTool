package ptt.crawler;

import org.jsoup.select.Elements;
import ptt.crawler.model.*;
import ptt.crawler.config.Config;

import okhttp3.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OkHttpClientReader {
    private OkHttpClient okHttpClient;
    private final Map<String, List<Cookie>> cookieStore; // 保�?? Cookie
    private final CookieJar cookieJar;

    public OkHttpClientReader() throws IOException {
        /* ??��?��?? */
        cookieStore = new HashMap<>();
        cookieJar = new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                List<Cookie> cookies = cookieStore.getOrDefault(
                    httpUrl.host(), 
                    new ArrayList<>()
                );
                cookies.addAll(list);
                cookieStore.put(httpUrl.host(), cookies);
            }
            
            /* 每次?��?�帶上儲存�?? Cookie */
            @Override
            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                return cookieStore.getOrDefault(
                    httpUrl.host(), 
                    new ArrayList<>()
                );
            }
        };
        okHttpClient = new OkHttpClient.Builder().cookieJar(cookieJar).build();

        /* ?��得網站�?��?��?? Cookie */
        Request request = new Request.Builder().get().url(Config.PTT_URL).build();
        okHttpClient.newCall(request).execute();
    }

    public List<Article> getList(String boardName) throws IOException, ParseException {
        Board board = Config.BOARD_LIST.get(boardName);

        /* 如�?�找不到??��?��?��?�板 */
        if (board == null) {
            return null;
        }

        /* 如�?��?�板??要�?�年檢查 */
        if (board.getAdultCheck() == true) {
            runAdultCheck(board.getUrl());
        }

        /* ??��?�目標�?�面 */
        Request request = new Request.Builder()
            .url(Config.PTT_URL + board.getUrl())
            .get()
            .build();

        Response response = okHttpClient.newCall(request).execute();
        String body = response.body().string();

        /* 轉�?? HTML ?�� Article */
        List<Map<String, String>> articles = parseArticle(body);
        List<Article> result = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");

        for (Map<String, String> article: articles) {
            String url = article.get("url");
            String title = article.get("title");
            String author = article.get("author");
            Date date = simpleDateFormat.parse(article.get("date"));

            result.add(new Article(board, url, title, author, date));
        }

        return result;
    }

    /* ?��?�年齡確�? */
    private void runAdultCheck(String url) throws IOException {
        FormBody formBody = new FormBody.Builder()
            .add("from", url)
            .add("yes", "yes")
            .build();

        Request request = new Request.Builder()
            .url(Config.PTT_URL + "/ask/over18")
            .post(formBody)
            .build();

        okHttpClient.newCall(request).execute();
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