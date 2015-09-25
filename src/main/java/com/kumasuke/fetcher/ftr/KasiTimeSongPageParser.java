package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * 歌詞タイム (Kasi-Time.com) 的歌词页分析器。<br>
 * 使用 {@code Jsoup} 包获取页面信息。
 */
class KasiTimeSongPageParser extends SongPageParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://www.kasi-time.com";
    // 匹配歌词页地址的正则表达式
    private static final Pattern fullUrlPattern;
    // 匹配歌曲代码的正则表达式
    private static final Pattern songCodePattern;

    static {
        fullUrlPattern = Pattern.compile(".*?/item-(\\d+)\\.html");
        songCodePattern = NUMBER_SONG_CODE_PATTERN;
    }

    private Document doc;
    private String songCode;

    private EnumHeader header;

    /**
     * 构造一个 {@code KasiTimeSongPageParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    KasiTimeSongPageParser(String page, String userAgent) throws IOException {
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

            Element title = doc.select
                    ("div.person_list_and_other_contents > h1").first();
            header.setTitle(title.text().trim());

            Elements artistsElement = doc.select("div.person_list th + td");
            // 处理多位艺术家的情况
            List<Set<String>> allArtists = artistsElement.stream()
                    .map(e -> e.select("a")
                            .stream()
                            .map(Element::text)
                            .map(String::trim)
                            .collect(Collectors.toSet()))
                    .collect(Collectors.toList());

            header.setArtist(allArtists.get(0))
                    .setLyricist(allArtists.get(1))
                    .setComposer(allArtists.get(2));

            // 不一定存在编曲信息
            if (allArtists.size() == 4)
                header.setArranger(allArtists.get(3));
        }

        return header;
    }

    /**
     * 获取歌词所在页面地址。
     *
     * @return 歌词地址
     */
    String lrcUrl() {
        return HOSTNAME + "/item_js.php?no=" + songCode;
    }

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    @Override
    String songPageUrl() {
        return HOSTNAME + "/item-" + songCode + ".html";
    }
}
