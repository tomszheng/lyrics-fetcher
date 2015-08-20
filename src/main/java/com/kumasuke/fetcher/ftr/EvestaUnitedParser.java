package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * イベスタ (Evesta.jp) 的统合分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class EvestaUnitedParser extends UnitedParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.evesta.jp/";
    // 提取歌曲标题的正则表达式
    private static final Pattern TITLE_PATTERN;
    // 提取歌曲基本信息的正则表达式
    private static final Pattern INFO_PATTERN;
    // 匹配歌词页地址的正则表达式
    private static final Pattern FULL_URL_PATTERN;

    static {
        TITLE_PATTERN = Pattern.compile("(.*?)\\u6b4c\\u8a5e\\s\\u002f.*");
        INFO_PATTERN = Pattern.compile("\\u6b4c\\uff1a(.*?)   # artist    \n" +
                        "\\u4f5c\\u8a5e\\uff1a(.*?)           # lyricist  \n" +
                        "\\u4f5c\\u66f2\\uff1a(.*)            # composer  \n",
                Pattern.COMMENTS);
        FULL_URL_PATTERN = Pattern.compile(".*?/lyric/artists/(a\\d+)/lyrics/(l\\d+)\\.html");
    }

    private Document doc;
    private String url;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * 构造一个 {@code EvestaUnitedParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    EvestaUnitedParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
    }

    private boolean validate(String page) {
        Matcher fullUrl = FULL_URL_PATTERN.matcher(page);

        if (fullUrl.matches())
            this.url = HOSTNAME + "/lyric/artists/" + fullUrl.group(1) + "/lyrics/" + fullUrl.group(2) + ".html";
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
     * 获取歌曲基本信息。<br>
     *
     * @return 装有歌曲信息的 {@code Header} 容器
     * @implSpec 初次调用时，会初始化需要返回的对象，这将耗费一定的时间。
     */
    @Override
    EnumHeader header() {
        if (header == null) {
            header = new EnumHeader();

            Element title = doc.select("#titleBand h1").first();
            Matcher titleMatcher = TITLE_PATTERN.matcher(title.text());
            if (titleMatcher.matches())
                header.setTitle(titleMatcher.group(1).trim());

            Element artists = doc.select("#descriptionBand div.artists").first();
            Matcher matcher = INFO_PATTERN.matcher(artists.text());
            if (matcher.matches())
                header.setArtist(toSet(true, matcher.group(1).split("/")))
                        .setLyricist(toSet(true, matcher.group(2).split("/")))
                        .setComposer(toSet(true, matcher.group(3).split("/")));
        }

        return header;
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

            Element lrcBody = doc.select("#lyricview div.body p").first();
            addTo(lyrics, lrcBody.html().split("<br(?: /)?>"));
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
        return url;
    }
}
