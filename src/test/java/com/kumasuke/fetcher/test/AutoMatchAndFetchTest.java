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

        // 重试多次，尽可能排除网络异常造成的测试失败
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

    private static void testStart(String page) {
        Fetcher fetcher = tryToAutoGetFetcher(page);
        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());
    }

    @Test
    public void aniMap() {
        testStart("http://www.animap.jp/kasi/showkasi.php?surl=k-150819-216");
    }

    @Test
    public void evesta() {
        testStart("http://www.evesta.jp/lyric/artists/a17674/lyrics/l213233.html");
    }

    @Test
    public void jLyric() {
        testStart("http://j-lyric.net/artist/a04cb21/l00c0b2.html");
    }

    @Test
    public void joySound() {
        testStart("https://www.joysound.com/web/search/song/27215/");
    }

    @Test
    public void animeSong() {
        testStart("http://www.jtw.zaq.ne.jp/animesong/ma/majotaku/ruuju.html");
    }

    @Test
    public void kashiNavi() {
        testStart("http://kashinavi.com/song_view.html?86429");
    }

    @Test
    public void kasiTime() {
        testStart("http://www.kasi-time.com/item-4423.html");
    }

    @Test
    public void kGet() {
        testStart("http://www.kget.jp/lyric/154428/%E7%B5%82%E3%82%8F%E3%82%8A%E3%81%AE%E4%B8%96%E7%95%8C%E3%81%8B" +
                "%E3%82%89_%E9%BA%BB%E6%9E%9D%E5%87%86%C3%97%E3%82%84%E3%81%AA%E3%81%8E%E3%81%AA%E3%81%8E");
    }

    @Test
    public void petitLyrics() {
        testStart("http://petitlyrics.com/lyrics/1153098");
    }

    @Test
    public void utaMap() {
        testStart("http://www.utamap.com/showkasi.php?surl=k-150819-173");
    }

    @Test
    public void utaNet() {
        testStart("http://www.uta-net.com/song/188939/");
    }

    @Test
    public void utaTen() {
        testStart("http://utaten.com/lyric/HoneyWorks+feat.sana%2CCHICO/%E3%83%97%E3%83%A9%E3%82%A4%E3%83%89%E9%9D" +
                "%A9%E5%91%BD/");
    }
}
