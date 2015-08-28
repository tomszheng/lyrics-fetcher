package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Lyrics;
import com.kumasuke.fetcher.util.URLReader;

import java.io.IOException;

import static java.util.Objects.isNull;

/**
 * あにまっぷ (AniMap.jp) 的歌词分析器。
 */
class AniMapLyricsParser extends LyricsParser {
    private String doc;

    private ListLyrics lyrics;

    /**
     * 构造一个 {@code AniMapLyricsParser} 对象，且指定 {@code UserAgent}。
     *
     * @param songPage  {@code AniMapSongPageParser} 对象<br>
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    AniMapLyricsParser(AniMapSongPageParser songPage, String userAgent) throws IOException {
        this.doc = URLReader.connect(songPage.lrcUrl())
                .charset("Shift-JIS")
                .timeout(5000)
                .referer(songPage.flashUrl())
                .userAgent(userAgent)
                .xRequestedWith(FLASH_VERSION)
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
            int begin = doc.indexOf("test2=") + 6;
            String[] lyricsText = doc.substring(begin).split("\\n");

            lyrics = toLyrics(lyricsText);
        }

        return lyrics;
    }
}
