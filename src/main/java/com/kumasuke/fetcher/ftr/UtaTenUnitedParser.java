package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UtaTen (UtaTen.com) 的统合分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class UtaTenUnitedParser extends UnitedParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://utaten.com";
    // 提取歌曲标题的正则表达式
    private static final Pattern TITLE_PATTERN;
    // 匹配歌词页地址的正则表达式
    private static final Pattern FULL_URL_PATTERN;
    // 提取假名注音的正则表达式
    private static final Pattern RUBY_PATTERN;

    static {
        TITLE_PATTERN = Pattern.compile("(.*?)(?=<span).*");
        FULL_URL_PATTERN = Pattern.compile(".*?/lyric/([^/]+)/([^/]+)/?");
        RUBY_PATTERN = Pattern.compile("<span class=\"ruby\"><span class=\"rb\">" +
                "(.*?)</span><span class=\"rt\">(.*?)</span></span>");
    }

    private Document doc;
    private String url;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * 构造一个 {@code JLyricSongPageParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    UtaTenUnitedParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
    }

    private boolean validate(String page) {
        Matcher fullUrl = FULL_URL_PATTERN.matcher(page);

        if (fullUrl.matches())
            this.url = HOSTNAME + "/lyric/" + fullUrl.group(1) + "/" + fullUrl.group(2) + "/";
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

            Element title = doc.select("div.contentBox__title--lyricTitle > h1").first();
            Matcher titleMatcher = TITLE_PATTERN.matcher(title.html());
            if (titleMatcher.matches())
                header.setTitle(titleMatcher.group(1).trim());

            Element artist = doc.select("span.contentBox__titleSub").first();
            Elements lyricistAndComposer = doc.select("dd.lyricWork__body");

            header.setArtist(toSet(artist.text().trim().split(",")))
                    .setLyricist(toSet(lyricistAndComposer.get(0).text().split("/")))
                    .setComposer(toSet(lyricistAndComposer.get(1).text().split("/")));
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

            Element lrcBody = doc.select("div.lyricBody div.medium").first();
            String srcLrc = lrcBody.html().replaceAll("\\n", "");
            String lrcWithRuby = RUBY_PATTERN.matcher(srcLrc)
                    .replaceAll("$1($2)");
            addTo(lyrics, lrcWithRuby.split("<br(?: /)?>"));
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
