package com.kumasuke.sample;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.util.FetcherBuilder;
import com.kumasuke.fetcher.util.Formatter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * 站点地址自动匹配歌词获取示例
 */
public class AutoMatchSample {
    public static void main(String[] args) throws Exception {
        // 利用反射获取匹配完成后的站点参数（site）字符串
        Class<FetcherBuilder> fbClass = FetcherBuilder.class;
        Field siteField = fbClass.getDeclaredField("site");
        siteField.setAccessible(true);

        // 用于测试的歌曲地址
        String[] pages = new String[]{
                "http://www.animap.jp/kasi/showkasi.php?surl=k-150819-094",
                "http://www.jtw.zaq.ne.jp/animesong/to/tokyoghoul/unravel.html",
                "http://kashinavi.com/song_view.html?89015",
                "http://www.kget.jp/lyric/171133/Call+Me+Maybe_Carly+Rae+Jepsen",
                "http://utaten.com/lyric/%E7%AD%8B%E8%82%89%E5%B0%91%E5%A5%B3%E5%B8%AF/" +
                        "%E6%B7%B7%E3%81%9C%E3%82%8B%E3%81%AA%E5%8D%B1%E9%99%BA/"
        };

        // 批量获取
        FetcherBuilder fetcherBuilder = Fetcher.builder();
        Stream.of(pages)
                .forEach(p -> {
                    try {
                        Fetcher fetcher = fetcherBuilder
                                .page(p)                    // 指定歌曲地址
                                .autoMatch()                // 开启自动匹配
                                .build();
                        String site = (String) siteField.get(fetcherBuilder);
                        String formattedString = Formatter.headerToFormattedString(fetcher.getHeader(), "%ar% - %ti%");

                        System.out.printf("[%s] %s%n", site, formattedString);
                    } catch (IOException e) {               // 连接、解析错误
                        System.out.printf("无法获取页面：%s%n", p);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } finally {
                        fetcherBuilder.reset();             // 重置构造器
                    }
                });
    }
}
