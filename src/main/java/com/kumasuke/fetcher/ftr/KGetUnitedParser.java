package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌詞ＧＥＴ (KGet.jp) 的统合分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class KGetUnitedParser extends UnitedParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.kget.jp";
    // 匹配歌词页地址的正则表达式
    private static final Pattern FULL_URL_PATTERN;
    // 匹配歌曲代码的正则表达式
    private static final Pattern SONG_CODE_PATTERN;

    static {
        FULL_URL_PATTERN = Pattern.compile(".*?/lyric/(\\d+)/?.*");
        SONG_CODE_PATTERN = Pattern.compile("\\d+");
    }

    private Document doc;
    private String songCode;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * 构造一个 {@code KGetSongPageParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    KGetUnitedParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
    }

    private boolean validate(String page) {
        Matcher fullUrl = FULL_URL_PATTERN.matcher(page);
        Matcher songCode = SONG_CODE_PATTERN.matcher(page);

        if (fullUrl.matches())
            this.songCode = fullUrl.group(1);
        else if (songCode.matches())
            this.songCode = page;
        else
            return false;

        return true;
    }

    private void initialize(String userAgent) throws IOException {
        this.doc = Jsoup.connect(songPageUrl())
                .timeout(5000)
                .userAgent(userAgent)
                .get();
    }

    /**
     * 获取歌曲基本信息。
     *
     * @return 装有歌曲信息的 {@code Header} 容器
     */
    @Override
    EnumHeader header() {
        if (header == null) {
            header = new EnumHeader();

            Element titleElement = doc.select("h1[itemprop=name]").first();
            Elements artistsElement = doc.select("table.lyric-data td");
            Element artistElement = artistsElement.get(0);
            Element lyricistElement = artistsElement.get(1);
            Element composerElement = artistsElement.get(2);

            String title = titleElement.text().trim();
            String[] artists = artistElement.text().split(", ");
            String[] lyricists = lyricistElement.text().split(", ");
            String[] composers = composerElement.text().split(", ");

            header.setTitle(title)
                    .setArtist(toSet(artists))
                    .setLyricist(toSet(lyricists))
                    .setComposer(toSet(composers));
        }

        return header;
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

            Element lrcBody = doc.select("#lyric-trunk").first();
            addTo(Parser::parseHtml, lyrics, lrcBody.html().split("<br(?: /)?>"));
        }

        return lyrics;
    }

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    @Override
    String songPageUrl() {
        return HOSTNAME + "/lyric/" + songCode + "/";
    }
}
