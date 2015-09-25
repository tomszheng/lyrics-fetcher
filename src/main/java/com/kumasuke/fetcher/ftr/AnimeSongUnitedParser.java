package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

/**
 * アニメソングの歌詞ならここにおまかせ？ (Jtw.Zaq.Ne.jp/AnimeSong) 的统合分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class AnimeSongUnitedParser extends UnitedParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.jtw.zaq.ne.jp/animesong";
    // 提取歌曲基本信息和歌词的正则表达式
    private static final Pattern allInfoPattern;
    // 匹配歌词页地址的正则表达式
    private static final Pattern fullUrlPattern;

    static {
        allInfoPattern = Pattern.compile("(?<title>.*?)\\n\\s+             # title     \n" +
                        "\\u4f5c\\u8a5e\\uff1a(?<lyricist>.*?)\\uff0f\\s*  # lyricist  \n" +
                        "\\u4f5c\\u66f2\\uff1a(?<composer>.*?)\\uff0f\\s*  # composer  \n" +
                        "\\u7de8\\u66f2\\uff1a(?<arranger>.*?)\\uff0f\\s*  # arranger  \n" +
                        "\\u6b4c\\uff1a(?<artist>.*?)\\n\\s+               # artist    \n" +
                        "(?<lyrics>.*)                                     # lyrics    \n",
                Pattern.COMMENTS | Pattern.DOTALL);
        fullUrlPattern = Pattern.compile(".*?/animesong/(\\w{1,2})/(\\w+)/(\\w+)\\.html");
    }

    private Matcher matcher;
    private String url;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * 构造一个 {@code AnimeSongUnitedParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    AnimeSongUnitedParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
    }

    private boolean validate(String page) {
        Matcher fullUrl = fullUrlPattern.matcher(page);

        if (fullUrl.matches())
            this.url = HOSTNAME + "/" + fullUrl.group(1) + "/" + fullUrl.group(2) + "/" + fullUrl.group(3) + ".html";
        else
            return false;

        return true;
    }

    private void initialize(String userAgent) throws IOException {
        Document doc = Jsoup.connect(songPageUrl())
                .timeout(5000)
                .userAgent(userAgent)
                .get();
        String docText = doc.select("td.b pre").first().text();
        matcher = allInfoPattern.matcher(docText);
    }

    /**
     * 获取歌曲基本信息。
     *
     * @return 装有歌曲信息的 {@code Header} 容器
     */
    @Override
    Header header() {
        if (isNull(header)) {
            header = new EnumHeader();

            if (matcher.matches()) {
                String title = matcher.group("title").trim();
                String[] lyricists = matcher.group("lyricist").split("\\u3001");
                String[] composers = matcher.group("composer").split("\\u3001");
                String[] arrangers = matcher.group("arranger").split("\\u3001");
                String[] artists = matcher.group("artist").split("\\u3001");

                header.setTitle(title)
                        .setLyricist(toStringSet(lyricists))
                        .setComposer(toStringSet(composers))
                        .setArranger(toStringSet(arrangers))
                        .setArtist(toStringSet(artists));
            }
        }

        return header;
    }

    /**
     * 获取歌词文本。
     *
     * @return 装有歌词文本的 {@code Lyrics} 容器
     */
    @Override
    Lyrics lyrics() {
        if (isNull(lyrics)) {
            if (matcher.matches()) {
                String[] lyricsText = matcher.group("lyrics").split("\\n");

                lyrics = toLyrics(lyricsText);
            } else
                throw new AssertionError("The regex matching lyrics ran across some problem.");
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
