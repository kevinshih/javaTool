package ptt.crawler.config;

import ptt.crawler.model.Board;

import java.util.*;

public final class Config {
    public static final String PTT_URL = "https://www.ptt.cc";
    public static final Map<String, Board> BOARD_LIST = new HashMap<>();
        /*
            PTT 看板網址等於看板英文名稱，故直接使用英文名稱當 Map 的 Key
            Ex.
                八卦板 Gossiping，網址為 https://www.ptt.cc/bbs/Gossiping/index.html
                股板 Stock，網址為 https://www.ptt.cc/bbs/Stock/index.html
        */
    static {
    	BOARD_LIST.put("Gossiping", new Board(
            "/bbs/Gossiping/index.html",
            "八卦板",
            "Gossiping",
            true));
    	BOARD_LIST.put("Stock", new Board(
                "/bbs/Stock/index.html",
                "股票板",
                "Stock",
                false));
    }
}