package com.kumasuke.sample;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.util.FetcherBuilder;
import com.kumasuke.fetcher.util.UserAgent;

/**
 * 歌ネット (Uta-Net.com) 歌词获取示例
 */
public class UtaNetSample {
    public static void main(String[] args) throws Exception {
        // 构造一个 FetcherBuilder 对象，用于创建 Fetcher 对象
        FetcherBuilder builder = FetcherBuilder.builder();
        // FetcherBuilder builder = Fetcher.builder();
        Fetcher fetcher = builder
                .site("Uta-Net.com")      // 指定站点
                .page("171502")           // 指定歌曲地址或代码
                .userAgent(UserAgent.IE)  // 指定 UserAgent
                .build();                 // 构造

        // 使用 Java 8 新的 Stream API 输出结果
        fetcher.getHeader()
                .forEach((n, v) -> System.out.println(String.format("%s = %s", n, v)));
        System.out.println();
        fetcher.getLyrics()
                .forEach(System.out::println);
    }
}
