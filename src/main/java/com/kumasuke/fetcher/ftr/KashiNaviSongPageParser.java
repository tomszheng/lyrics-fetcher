package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kumasuke.fetcher.util.Tools.p;
import static com.kumasuke.fetcher.util.Tools.toMap;
import static java.util.Objects.isNull;

/**
 * 歌詞ナビ (KashiNavi.com) 的歌词页分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class KashiNaviSongPageParser extends SongPageParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://kashinavi.com";
    // 提取歌曲基本信息中作词和作曲的正则表达式
    private static final Pattern lcInfoPattern;
    // 匹配歌词页地址的正则表达式
    private static final Pattern fullUrlPattern;
    // 匹配歌曲代码的正则表达式
    private static final Pattern songCodePattern;

    static {
        lcInfoPattern = Pattern.compile("\\u4f5c\\u8a5e\\u3000\\uff1a\\u3000(.*?)   # lyricist  \n" +
                        "\\u4f5c\\u66f2\\u3000\\uff1a\\u3000(.*?)                   # composer  \n",
                Pattern.COMMENTS);
        fullUrlPattern = Pattern.compile(".*?/song_view\\.html\\?(\\d+)");
        songCodePattern = NUMBER_SONG_CODE_PATTERN;
    }

    private Document doc;
    private String songCode;

    private EnumHeader header;

    /**
     * 构造一个 {@code KashiNaviSongPageParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    KashiNaviSongPageParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
    }

    /**
     * 获取歌词 CGI 地址。
     *
     * @return 歌词 CGI 地址
     */
    static String lrcCgiUrl() {
        return HOSTNAME + "/cgi-bin/kashi.cgi";
    }

    private boolean validate(String page) {
        Matcher fullUrl = fullUrlPattern.matcher(page);
        Matcher songCode = songCodePattern.matcher(page);

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
    Header header() {
        if (isNull(header)) {
            header = new EnumHeader();

            Elements titleAndArtist = doc.select("table[cellpadding=2] table[cellspacing=5]")
                    .first().select("td");
            String title = titleAndArtist.get(0).text().trim();
            String[] artists = titleAndArtist.get(2).text().split("\\u30fb");
            header.setTitle(title)
                    .setArtist(toStringSet(artists));

            String lyricistAndComposer = doc.select("table[cellpadding=2] table[cellspacing=0]")
                    .first().select("td")
                    .first().text();
            Matcher matcher = lcInfoPattern.matcher(lyricistAndComposer);

            if (matcher.matches()) {
                String[] lyricists = matcher.group(1).split("\\u30fb");
                String[] composers = matcher.group(2).split("\\u30fb");

                header.setLyricist(toStringSet(lyricists))
                        .setComposer(toStringSet(composers));
            }
        }

        return header;
    }

    /**
     * 获取歌词 CGI 所需参数。
     *
     * @return CGI 参数 {@code Map} 对象集
     */
    Map<String, String> lrcCgiParameters() {
        return toMap(p("kdifoe88", "smx;paa"), p("file_no", songCode));
    }

    /**
     * 获取歌词所在页面地址。
     *
     * @return 歌词地址
     */
    String lrcUrl() {
        return HOSTNAME + "/song_view.swf?file_no=" + songCode;
    }

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    @Override
    String songPageUrl() {
        return HOSTNAME + "/song_view.html?" + songCode;
    }
}
