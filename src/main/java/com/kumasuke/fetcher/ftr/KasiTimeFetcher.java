package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.io.IOException;

/**
 * 歌詞タイム (Kasi-Time.com) 的歌词获取器。<br>
 * <p>
 * 该获取器获取的歌曲基本信息中包含编曲。</p>。
 */
public class KasiTimeFetcher extends AbstractSplitFetcher<KasiTimeSongPageParser, KasiTimeLyricsParser> {
    /**
     * 构造一个 {@code KasiTimeFetcher} 对象，用于获取对应网站歌词相关信息。<br>
     * 也可使用 {@link FetcherBuilder FetcherBuilder} 来进行构造。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    public KasiTimeFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        songPageParser = new KasiTimeSongPageParser(page, userAgent);
        lyricsParser = new KasiTimeLyricsParser(songPageParser, userAgent);
    }
}
