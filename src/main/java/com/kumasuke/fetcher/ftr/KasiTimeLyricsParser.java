package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Lyrics;
import com.kumasuke.fetcher.util.URLReader;

import java.io.IOException;

import static java.util.Objects.isNull;

/**
 * 歌詞タイム (Kasi-Time.com) 的歌词分析器。
 */
class KasiTimeLyricsParser extends LyricsParser {
    private String js;

    private ListLyrics lyrics;

    /**
     * 构造一个 {@code KasiTimeLyricsParser} 对象，且指定 {@code UserAgent}。
     *
     * @param songPage  {@code KasiTimeSongPageParser} 对象<br>
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    KasiTimeLyricsParser(KasiTimeSongPageParser songPage, String userAgent) throws IOException {
        this.js = URLReader.connect(songPage.lrcUrl())
                .charset("UTF-8")
                .timeout(5000)
                .userAgent(userAgent)
                .getText();
    }

    /**
     * 获取歌词文本。
     *
     * @return 装有歌词文本的 {@code Lyrics} 容器
     */
    @Override
    Lyrics lyrics() {
        if (isNull(lyrics)) {
            int begin = js.indexOf("write('") + 7;
            int end = js.lastIndexOf("');");
            String[] lyricsText = js.substring(begin, end).split("<br(?: /)?>");

            lyrics = toLyrics(lyricsText);
        }

        return lyrics;
    }
}
