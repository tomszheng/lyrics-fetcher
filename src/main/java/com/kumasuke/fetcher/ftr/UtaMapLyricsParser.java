package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Lyrics;
import com.kumasuke.fetcher.util.URLReader;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

/**
 * うたまっぷ (UtaMap.com) 的歌词分析器。
 */
class UtaMapLyricsParser extends LyricsParser {
    // 从 Js 代码中匹配歌词的正则表达式
    private static final Pattern JS_LYRICS_PATTERN;

    static {
        JS_LYRICS_PATTERN = Pattern.compile("\\.fillText\\('(.*?)',");
    }

    private String js;

    private ListLyrics lyrics;

    /**
     * 构造一个 {@code UtaMapLyricsParser} 对象，且指定 {@code UserAgent}。
     *
     * @param songPage  {@code UtaMapSongPageParser} 对象<br>
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    UtaMapLyricsParser(UtaMapSongPageParser songPage, String userAgent) throws IOException {
        this.js = URLReader.connect(songPage.lrcUrl())
                .charset("Shift-JIS")
                .timeout(5000)
                .referer(songPage.songPageUrl())
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
            Matcher lyricsTextMatcher = JS_LYRICS_PATTERN.matcher(js);

            lyrics = toLyrics(lyricsTextMatcher);
        }

        return lyrics;
    }
}

