package com.kumasuke.fetcher.test;

import com.kumasuke.fetcher.Fetcher;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * 获取和输出测试
 */
public class FetchAndPrintResultTest {
    private static final int RETRY_TIME = 5;

    private static Fetcher tryToGetFetcher(String site, String page) {
        boolean finished = false;
        int retryTime = 0;
        Fetcher fetcher = null;

        // 重试多次，尽可能排除网络异常造成的测试失败
        while (!finished && retryTime < RETRY_TIME) {
            try {
                fetcher = Fetcher.builder()
                        .site(site)
                        .page(page)
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

    private static void printFetcher(Fetcher fetcher) {
        System.out.println("Source = " + fetcher.getSource());
        fetcher.getHeader().forEach(System.out::println);
        System.out.println("------------------------------------");
        fetcher.getLyrics().forEach(System.out::println);
    }

    private static void testStart(String site, String page) {
        Fetcher fetcher = tryToGetFetcher(site, page);

        assertNotNull(fetcher);
        assertNotNull(fetcher.getHeader());
        assertNotNull(fetcher.getLyrics());
        assertNotNull(fetcher.getSource());

        printFetcher(fetcher);
    }

    @Test
    public void aniMap() {
        testStart("AniMap.jp", "k-140806-069");
    }

    @Test
    public void evesta() {
        testStart("Evesta.jp", "www.evesta.jp/lyric/artists/a359772/lyrics/l223589.html");
    }

    @Test
    public void jLyric() {
        testStart("J-Lyric.net", "http://j-lyric.net/artist/a057818/l031ba7.html");
    }

    @Test
    public void joySound() {
        testStart("JoySound.com", "405267");
    }

    @Test
    public void animeSong() {
        testStart("Jtw.Zaq.Ne.jp/AnimeSong", "http://www.jtw.zaq.ne.jp/animesong/me/konan/munega.html");
    }

    @Test
    public void kashiNavi() {
        testStart("KashiNavi.com", "83934");
    }

    @Test
    public void kasiTime() {
        testStart("Kasi-Time.com", "http://www.kasi-time.com/item-73631.html");
    }

    @Test
    public void kGet() {
        testStart("KGet.jp",
                "http://www.kget.jp/lyric/171135/Good+Time+%28with+Owl+City%29_Carly+Rae+Jepsen%2C+Owl+City");
    }

    @Test
    public void petitLyrics() {
        testStart("PetitLyrics.com", "1034551");
    }

    @Test
    public void utaMap() {
        testStart("UtaMap.com", "k-150415-182");
    }

    @Test
    public void utaNet() {
        testStart("Uta-Net.com", "183656");
    }

    @Test
    public void utaTen() {
        testStart("UtaTen.com", "utaten.com/lyric/Neru,鏡音リン,鏡音レン/ハウトゥー世界征服/");
    }
}
