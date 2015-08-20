package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * うたまっぷ (UtaMap.com) 的歌词页分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class UtaMapSongPageParser extends SongPageParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.utamap.com";
    // 匹配歌词页地址的正则表达式
    private static final Pattern FULL_URL_PATTERN;
    // 匹配歌曲代码的正则表达式
    private static final Pattern SONG_CODE_PATTERN;

    static {
        FULL_URL_PATTERN = Pattern.compile(".*?com/show(?:kasi|top)\\.php\\?surl=([-\\w]+)");
        SONG_CODE_PATTERN = Pattern.compile("[-\\w]+");
    }

    private Document doc;
    private String songCode;

    private EnumHeader header;

    /**
     * 构造一个 {@code UtaMapSongPageParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    public UtaMapSongPageParser(String page, String userAgent) throws IOException {
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

            Element ti = doc.select("td.kasi1").first();
            Elements artists = doc.select("td.pad5x10x0x10");
            String lr = artists.get(1).text();
            String co = artists.get(3).text();
            String ar = artists.get(5).text();

            header.setTitle(ti.text().trim())
                    .setLyricist(toSet(lr.replace(JSOUP_NBSP, "").split("/")))
                    .setComposer(toSet(co.replace(JSOUP_NBSP, "").split("/")))
                    .setArtist(toSet(ar.replace(JSOUP_NBSP, "").split("/")));
        }

        return header;
    }

    /**
     * 获取歌词所在页面地址。
     *
     * @return 歌词地址
     */
    String lrcUrl() {
        return HOSTNAME + "/js_smt.php?unum=" + songCode;
    }

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    @Override
    String songPageUrl() {
        return HOSTNAME + "/showkasi.php?surl=" + songCode;
    }
}