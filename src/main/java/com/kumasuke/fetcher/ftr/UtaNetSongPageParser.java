package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

/**
 * 歌ネット (Uta-Net.com) 的歌词页分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class UtaNetSongPageParser extends SongPageParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.uta-net.com";
    // 提取歌曲基本信息的正则表达式
    private static final Pattern infoPattern;
    // 匹配歌词页地址的正则表达式
    private static final Pattern fullUrlPattern;
    // 匹配歌曲代码的正则表达式
    private static final Pattern songCodePattern;

    static {
        infoPattern = Pattern.compile("\\u6b4c\\u624b\\uff1a\\s(.*?)  # artist    \n" +
                        "\\u4f5c\\u8a5e\\uff1a\\s(.*?)                # lyrics    \n" +
                        "\\u4f5c\\u66f2\\uff1a\\s(.*)                 # composer  \n",
                Pattern.COMMENTS);
        fullUrlPattern = Pattern.compile(".*?/song/(\\d+)/?");
        songCodePattern = NUMBER_SONG_CODE_PATTERN;
    }

    private Document doc;
    private String songCode;

    private EnumHeader header;

    /**
     * 构造一个 {@code UtaNetSongPageParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    UtaNetSongPageParser(String page, String userAgent) throws IOException {
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

            Element titleElement = doc.select("#sound_uri + h2").first();
            String title = titleElement.text().trim();
            header.setTitle(title);

            Element artistsElement = doc.select("div.kashi_artist").first();
            String allArtists = artistsElement.text();
            Matcher matcher = infoPattern.matcher(allArtists);
            if (matcher.matches()) {
                String[] artists = matcher.group(1).split("\\u30fb");
                String[] lyricists = matcher.group(2).split("\\u30fb");
                String[] composers = matcher.group(3).split("\\u30fb");

                header.setArtist(toStringSet(artists))
                        .setLyricist(toStringSet(lyricists))
                        .setComposer(toStringSet(composers));
            }
        }

        return header;
    }

    /**
     * 获取歌词所在页面地址。
     *
     * @return 歌词地址
     */
    String lrcUrl() {
        return HOSTNAME + "/user/phplib/svg/showkasi.php?ID=" + songCode;
    }

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    @Override
    String songPageUrl() {
        return HOSTNAME + "/song/" + songCode + "/";
    }
}
