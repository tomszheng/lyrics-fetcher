package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.io.IOException;

/**
 * イベスタ (Evesta.jp) 的歌词获取器。
 */
public class EvestaFetcher extends AbstractUnitedFetcher<EvestaUnitedParser> {
    /**
     * 构造一个 {@code EvestaFetcher} 对象，用于获取对应网站歌词相关信息。<br>
     * 也可使用 {@link FetcherBuilder FetcherBuilder} 来进行构造。
     *
     * @param page      歌词页地址
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    public EvestaFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        parser = new EvestaUnitedParser(page, userAgent);
    }
}
