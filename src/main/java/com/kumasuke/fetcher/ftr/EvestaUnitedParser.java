package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kumasuke.fetcher.util.Tools.toSet;
import static java.util.Objects.isNull;


/**
 * イベスタ (Evesta.jp) 的统合分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class EvestaUnitedParser extends UnitedParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.evesta.jp/";
    // 提取歌曲标题的正则表达式
    private static final Pattern titlePattern;
    // 提取歌曲基本信息的正则表达式
    private static final Pattern infoPattern;
    // 匹配歌词页地址的正则表达式
    private static final Pattern fullUrlPattern;

    static {
        titlePattern = Pattern.compile("(.*?)\\u6b4c\\u8a5e\\s\\u002f.*");
        infoPattern = Pattern.compile("\\u6b4c\\uff1a(.*?)    # artist    \n" +
                        "\\u4f5c\\u8a5e\\uff1a(.*?)           # lyricist  \n" +
                        "\\u4f5c\\u66f2\\uff1a(.*)            # composer  \n",
                Pattern.COMMENTS);
        fullUrlPattern = Pattern.compile(".*?/lyric/artists/(a\\d+)/lyrics/(l\\d+)\\.html");
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
        Matcher fullUrl = fullUrlPattern.matcher(page);

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
     * 获取歌曲基本信息。
     *
     * @return 装有歌曲信息的 {@code Header} 容器
     */
    @Override
    Header header() {
        if (isNull(header)) {
            header = new EnumHeader();

            Element titleElement = doc.select("#titleBand h1").first();
            Matcher titleMatcher = titlePattern.matcher(titleElement.text());

            if (titleMatcher.matches()) {
                String title = titleMatcher.group(1).trim();
                header.setTitle(title);
            }

            Element artistsElement = doc.select("#descriptionBand div.artists").first();
            Matcher matcher = infoPattern.matcher(artistsElement.text());

            if (matcher.matches()) {
                String[] artists = matcher.group(1).split("/");
                String[] lyricists = matcher.group(2).split("/");
                String[] composers = matcher.group(3).split("/");

                header.setArtist(toSet(Parser::superTrim, artists))
                        .setLyricist(toSet(Parser::superTrim, lyricists))
                        .setComposer(toSet(Parser::superTrim, composers));
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
            Element lrcBody = doc.select("#lyricview div.body p").first();
            String[] lyricsText = lrcBody.html().split("<br(?: /)?>");

            lyrics = toLyrics(Parser::parseHtml, lyricsText);
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
