package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌ネット (Uta-Net.com) 的歌词页分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class UtaNetSongPageParser extends SongPageParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.uta-net.com";
    // 提取歌曲基本信息的正则表达式
    private static final Pattern INFO_PATTERN;
    // 匹配歌词页地址的正则表达式
    private static final Pattern FULL_URL_PATTERN;
    // 匹配歌曲代码的正则表达式
    private static final Pattern SONG_CODE_PATTERN;

    static {
        INFO_PATTERN = Pattern.compile("\\u6b4c\\u624b\\uff1a\\s(.*?) # artist    \n" +
                        "\\u4f5c\\u8a5e\\uff1a\\s(.*?)                # lyrics    \n" +
                        "\\u4f5c\\u66f2\\uff1a\\s(.*)                 # composer  \n",
                Pattern.COMMENTS);
        FULL_URL_PATTERN = Pattern.compile(".*?/song/(\\d+)/?");
        SONG_CODE_PATTERN = Pattern.compile("\\d+");
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
     * 获取歌曲基本信息。<br>
     *
     * @return 装有歌曲信息的 {@code Header} 容器
     * @implSpec 初次调用时，会初始化需要返回的对象，这将耗费一定的时间。
     */
    @Override
    EnumHeader header() {
        if (header == null) {
            header = new EnumHeader();

            Element title = doc.select("h2.prev_pad").first();
            header.setTitle(title.text().trim());

            Element artists = doc.select("div.kashi_artist").first();
            String allArtists = artists.text();
            Matcher matcher = INFO_PATTERN.matcher(allArtists);
            if (matcher.matches())
                header.setArtist(toSet(matcher.group(1).split("\\u30fb")))
                        .setLyricist(toSet(matcher.group(2).split("\\u30fb")))
                        .setComposer(toSet(matcher.group(3).split("\\u30fb")));
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
