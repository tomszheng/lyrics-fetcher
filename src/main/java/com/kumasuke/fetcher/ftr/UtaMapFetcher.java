package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.io.IOException;

/**
 * うたまっぷ (UtaMap.com) 的歌词获取器。
 */
public class UtaMapFetcher extends AbstractSplitFetcher<UtaMapSongPageParser, UtaMapLyricsParser> {
    /**
     * 构造一个 {@code UtaMapFetcher} 对象，用于获取对应网站歌词相关信息。<br>
     * 也可使用 {@link FetcherBuilder FetcherBuilder} 来进行构造。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    public UtaMapFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        songPageParser = new UtaMapSongPageParser(page, userAgent);
        lyricsParser = new UtaMapLyricsParser(songPageParser, userAgent);
    }
}
