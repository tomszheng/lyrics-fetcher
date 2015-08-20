package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * 歌ネット (Uta-Net.com) 的歌词获取器。
 */
public class UtaNetFetcher extends AbstractSplitFetcher<UtaNetSongPageParser, UtaNetLyricsParser> {
    /**
     * 构造一个 {@code UtaNetFetcher} 对象，用于获取对应网站歌词相关信息。<br>
     * 也可使用 {@link FetcherBuilder FetcherBuilder} 来进行构造。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、解析、处理失败
     */
    public UtaNetFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        songPageParser = new UtaNetSongPageParser(page, userAgent);
        try {
            lyricsParser = new UtaNetLyricsParser(songPageParser);
        } catch (DocumentException e) {
            // 包装 DocumentException 为 IOException
            throw new IOException(e);
        }
    }
}
