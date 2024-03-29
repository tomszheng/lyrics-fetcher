package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kumasuke.fetcher.util.Tools.toSet;
import static java.util.Objects.isNull;

/**
 * あにまっぷ (AniMap.jp) 的歌词页分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class AniMapSongPageParser extends SongPageParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.animap.jp";
    // 匹配歌词页地址的正则表达式
    private static final Pattern fullUrlPattern;
    // 匹配歌曲代码的正则表达式
    private static final Pattern songCodePattern;

    static {
        fullUrlPattern = Pattern.compile(".*?jp/kasi/showkasi\\.php\\?surl=([-\\w]+)");
        songCodePattern = WORD_SONG_CODE_PATTERN;
    }

    private Document doc;
    private String songCode;

    private EnumHeader header;

    /**
     * 构造一个 {@code AniMapSongPageParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    AniMapSongPageParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
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

            Elements titleAndArtists = doc
                    .select("table[width=442]")
                    .first()
                    .select("td[bgcolor=#ffffff]");

            String[] artists = titleAndArtists.get(0).text().split("/");
            String[] lyricists = titleAndArtists.get(1).text().split("/");
            String title = htmlTrim(titleAndArtists.get(2).text());
            String[] composers = titleAndArtists.get(3).text().split("/");

            header.setArtist(toSet(Parser::htmlTrim, artists))
                    .setLyricist(toSet(Parser::htmlTrim, lyricists))
                    .setTitle(title)
                    .setComposer(toSet(Parser::htmlTrim, composers));
        }

        return header;
    }

    /**
     * 获取歌词所在页面地址。
     *
     * @return 歌词地址
     */
    String lrcUrl() {
        return HOSTNAME + "/kasi/phpflash/flashphp.php?unum=" + songCode;
    }

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    @Override
    String songPageUrl() {
        return HOSTNAME + "/kasi/showkasi.php?surl=" + songCode;
    }

    /**
     * 获取 Flash 容器地址，用于 Referer 欺骗。
     *
     * @return Flash 容器地址
     */
    String flashUrl() {
        return HOSTNAME + "/kasi/showkasi.swf?ucode=" + songCode;
    }
}
