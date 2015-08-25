package com.kumasuke.fetcher.test;

import com.kumasuke.fetcher.Fetcher;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * 自动匹配和获取测试
 */
public class AutoMatchAndFetchTest {
    private static final int RETRY_TIME = 5;

    private static Fetcher tryToAutoGetFetcher(String page) {
        boolean finished = false;
        int retryTime = 0;
        Fetcher fetcher = null;

        while (!finished && retryTime < RETRY_TIME) {
            try {
                fetcher = Fetcher.builder()
                        .page(page)
                        .autoMatch()
                        .build();
                finished = true;
            } catch (IOException e) {
                if (e.getMessage().contains("timed out")) {
                    if (retryTime++ >= RETRY_TIME)
                        System.err.println("网络超时错误！请稍后重试！");
                } else {
                    finished = true;
                    System.err.println("未知错误！请稍后重试！");
                    e.printStackTrace();
                }
            }
        }

        return fetcher;
    }

    @Test
    public void aniMap() {
        Fetcher fetcher = tryToAutoGetFetcher("http://www.animap.jp/kasi/showkasi.php?surl=k-150819-216");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void evesta() {
        Fetcher fetcher = tryToAutoGetFetcher("http://www.evesta.jp/lyric/artists/a17674/lyrics/l213233.html");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void jLyric() {
        Fetcher fetcher = tryToAutoGetFetcher("http://j-lyric.net/artist/a04cb21/l00c0b2.html");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void joySound() {
        Fetcher fetcher = tryToAutoGetFetcher("https://www.joysound.com/web/search/song/27215/");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void animeSong() {
        Fetcher fetcher = tryToAutoGetFetcher("http://www.jtw.zaq.ne.jp/animesong/ma/majotaku/ruuju.html");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void kashiNavi() {
        Fetcher fetcher = tryToAutoGetFetcher("http://kashinavi.com/song_view.html?86429");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());

        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void kasiTime() {
        Fetcher fetcher = tryToAutoGetFetcher("http://www.kasi-time.com/item-4423.html");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void kGet() {
        Fetcher fetcher = tryToAutoGetFetcher("http://www.kget.jp/lyric/154428/%E7%B5%82%E3%82%8F%E3%82%8A%E3%81%AE" +
                "%E4%B8%96%E7%95%8C%E3%81%8B%E3%82%89_%E9%BA%BB%E6%9E%9D%E5%87%86%C3%97%E3%82%84%E3%81%AA%E3%81%8E" +
                "%E3%81%AA%E3%81%8E");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void utaMap() {
        Fetcher fetcher = tryToAutoGetFetcher("http://www.utamap.com/showkasi.php?surl=k-150819-173");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void utaNet() {
        Fetcher fetcher = tryToAutoGetFetcher("http://www.uta-net.com/song/188939/");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void utaTen() {
        Fetcher fetcher = tryToAutoGetFetcher("http://utaten.com/lyric/HoneyWorks+feat.sana%2CCHICO/%E3%83%97%E3%83" +
                "%A9%E3%82%A4%E3%83%89%E9%9D%A9%E5%91%BD/");
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }
}
