package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.URLReader;

import java.io.IOException;

/**
 * 歌詞ナビ (KashiNavi.com) 的歌词分析器。
 */
class KashiNaviLyricsParser extends LyricsParser {
    private String doc;

    private ListLyrics lyrics;

    /**
     * 构造一个 {@code KashiNaviLyricsParser} 对象，且指定 {@code UserAgent}。
     *
     * @param songPage  {@code KashiNaviSongPageParser} 对象<br>
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    KashiNaviLyricsParser(KashiNaviSongPageParser songPage, String userAgent) throws IOException {
        this.doc = URLReader.connect(KashiNaviSongPageParser.lrcCgiUrl())
                .timeout(5000)
                .referer(songPage.lrcUrl())
                .userAgent(userAgent)
                .requestProperty(toMap(X_REQUESTED_WITH_PROPERTY))
                .usePost(true)
                .requestParameter(songPage.lrcCgiParameters())
                .getText();
    }

    /**
     * 获取歌词文本。
     *
     * @return 装有歌词文本的 {@code Lyrics} 容器
     */
    @Override
    ListLyrics lyrics() {
        if (lyrics == null) {
            lyrics = new ListLyrics();

            int begin = doc.indexOf("\n") + 1;
            String[] lyricsText = doc.substring(begin).split("\\n");
            addTo(lyrics, lyricsText);
        }

        return lyrics;
    }
}
