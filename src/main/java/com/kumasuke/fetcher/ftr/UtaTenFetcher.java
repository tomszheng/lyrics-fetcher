package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.io.IOException;

/**
 * UtaTen (UtaTen.com) 的歌词获取器。<br>
 * <p>
 * 该获取器获取的歌词文本含有假名注音(読み仮名)。</p>
 */
public class UtaTenFetcher extends AbstractUnitedFetcher<UtaTenUnitedParser> {
    /**
     * 构造一个 {@code UtaTenFetcher} 对象，用于获取对应网站歌词相关信息。<br>
     * 也可使用 {@link FetcherBuilder FetcherBuilder} 来进行构造。
     *
     * @param page      歌词页地址
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    public UtaTenFetcher(String page, String userAgent)
            throws IOException {
        super(page, userAgent);

        parser = new UtaTenUnitedParser(page, userAgent);
    }
}
