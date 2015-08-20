package com.kumasuke.sample;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.util.FetcherBuilder;
import com.kumasuke.fetcher.util.UserAgent;

import java.util.Scanner;

public class Sample {
    public static void main(String[] args) throws Exception {
        System.out.println("请选择需要测试的网站：");
        System.out.println("[0] Uta-Net.com");
        System.out.println("[1] J-Lyric.net");
        System.out.println("[2] UtaMap.com");
        System.out.println("[3] Kasi-Time.com");
        System.out.println("[4] KashiNavi.com");
        System.out.println("[5] KGet.jp");
        System.out.println("[6] UtaTen.com");
        System.out.println("[7] AniMap.jp");
        System.out.println("[8] Evesta.jp");
        System.out.println("[9] Jtw.Zaq.Ne.jp/AnimeSong");
        System.out.println("请输入选项：");

        int choice = new Scanner(System.in).nextInt();
        FetcherBuilder builder = FetcherBuilder.builder();

        switch (choice) {
            case 0:
                printFetcher(builder
                        .site("Uta-Net.com")
                        .page("183656")
                        .userAgent(UserAgent.CHROME)
                        .build());
                break;
            case 1:
                printFetcher(builder
                        .site("J-Lyric.net")
                        .page("http://j-lyric.net/artist/a057818/l031ba7.html")
                        .userAgent(UserAgent.IE)
                        .build());
                break;
            case 2:
                printFetcher(builder
                        .site("UtaMap.com")
                        .page("k-150415-182")
                        .userAgent(UserAgent.FIREFOX)
                        .build());
                break;
            case 3:
                printFetcher(builder
                        .site("Kasi-Time.com")
                        .page("http://www.kasi-time.com/item-73631.html")
                        .userAgent(UserAgent.getUserAgent())
                        .build());
                break;
            case 4:
                printFetcher(builder
                        .site("KashiNavi.com")
                        .page("83934")
                        .build());
                break;
            case 5:
                printFetcher(builder
                        .site("KGet.jp")
                        .page("http://www.kget.jp/lyric/171135/" +
                                "Good+Time+%28with+Owl+City%29_Carly+Rae+" +
                                "Jepsen%2C+Owl+City")
                        .build());
                break;
            case 6:
                printFetcher(builder
                        .site("UtaTen.com")
                        .page("utaten.com/lyric/Neru,鏡音リン,鏡音レン/ハウトゥー世界征服/")
                        .userAgent(UserAgent.EDGE)
                        .build());
                break;
            case 7:
                printFetcher(builder
                        .site("AniMap.jp")
                        .page("k-140806-069")
                        .build());
                break;
            case 8:
                printFetcher(builder
                        .site("Evesta.jp")
                        .page("www.evesta.jp/lyric/artists/a359772/lyrics/l223589.html")
                        .build());
                break;
            case 9:
                printFetcher(builder
                        .site("Jtw.Zaq.Ne.jp/AnimeSong")
                        .page("http://www.jtw.zaq.ne.jp/animesong/me/konan/munega.html")
                        .build());
        }
    }

    private static void printFetcher(Fetcher fetcher) {
        System.out.println("Source = " + fetcher.getSource());
        fetcher.getHeader()
                .forEach(System.out::println);
        System.out.println("------------------------------------");

        fetcher.getLyrics()
                .forEach(System.out::println);
    }
}
