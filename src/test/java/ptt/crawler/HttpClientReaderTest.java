package ptt.crawler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ptt.crawler.model.Article;

class HttpClientReaderTest {
	private HttpClientReader reader;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        try {
            reader = new HttpClientReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void list() {
        try {
            //List<Article> result = reader.getList("Stock");
            List<Article> result = reader.getList("Gossiping");
            Assertions.assertInstanceOf(List.class, result);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
