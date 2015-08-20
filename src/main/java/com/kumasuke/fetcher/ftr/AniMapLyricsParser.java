package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.URLReader;

import java.io.IOException;

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
                .requestProperty(toMap(X_REQUESTED_WITH_PROPERTY))
                .get();
    }

    /**
     * 获取歌词文本。<br>
     *
     * @return 装有歌词文本的 {@code Lyrics} 容器
     * @implSpec 初次调用时，会初始化需要返回的对象，这将耗费一定的时间。
     */
    @Override
    ListLyrics lyrics() {
        if (lyrics == null) {
            lyrics = new ListLyrics();

            int begin = doc.indexOf("test2=") + 6;
            addTo(lyrics, doc.substring(begin).split("\\n"));
        }

        return lyrics;
    }
}
