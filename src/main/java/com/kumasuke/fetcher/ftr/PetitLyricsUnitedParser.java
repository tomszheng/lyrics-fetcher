package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;
import com.kumasuke.fetcher.util.URLReader;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kumasuke.fetcher.util.Tools.*;
import static java.util.Objects.isNull;

/**
 * プチリリ (PetitLyrics.com) 的统合分析器。
 */
class PetitLyricsUnitedParser extends UnitedParser {
    // 网站的主机名
    private static final String HOSTNAME = "http://petitlyrics.com";
    // 匹配歌词页地址的正则表达式
    private static final Pattern fullUrlPattern;
    // 匹配歌曲代码的正则表达式
    private static final Pattern songCodePattern;
    // 提取歌曲标题的正则表达式
    private static final Pattern titlePattern;
    // 提取歌曲基本信息的正则表达式
    private static final Pattern allArtistsPattern;
    // 提取歌词文本的正则表达式
    private static final Pattern lyricsPattern;

    static {
        fullUrlPattern = Pattern.compile(".*?/lyrics/(\\d+)/?");
        songCodePattern = NUMBER_SONG_CODE_PATTERN;
        titlePattern = Pattern.compile("<div class=\"title-bar\">(.+?)</div");
        allArtistsPattern = Pattern.compile("<div class=\"pure-u-1\">.*?<div align=\"left\".*?<p>(.*?)</p>",
                Pattern.DOTALL);
        lyricsPattern = Pattern.compile("<canvas id=\"lyrics\".*?>([^<]+)\\n</canvas>");
    }

    private String doc;
    private String songCode;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * 构造一个 {@code PetitLyricsUnitedParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    PetitLyricsUnitedParser(String page, String userAgent) throws IOException {
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
        this.doc = URLReader.connect(songPageUrl())
                .timeout(5000)
                .userAgent(userAgent)
                .getText();
    }

    @Override
    Header header() {
        if (isNull(header)) {
            header = new EnumHeader();

            Matcher titleMatcher = titlePattern.matcher(doc);
            if (titleMatcher.find()) {
                String title = parseHtml(titleMatcher.group(1)).trim();
                header.setTitle(title);
            }

            Matcher allArtistsMatcher = allArtistsPattern.matcher(doc);
            if (allArtistsMatcher.find()) {
                String allArtistsText = parseHtml(allArtistsMatcher.group(1));
                String[] allArtistsArray = allArtistsText.split(JSOUP_NBSP);

                // 处理艺术家信息
                Map<String, Set<String>> allArtists = Stream.of(allArtistsArray)
                        .map(String::trim)
                        .map(s -> {
                            if (s.contains("\u4f5c\u66f2")) {
                                String[] tmp = s.split("/\\u7de8\\u66f2:");

                                if (tmp.length > 1) {
                                    s = tmp[0];
                                    header.setArranger(toStringSet(tmp[1].split("[&\\uff06]")));
                                }
                            }
                            String[] p = s.split("\\uff1a");

                            return p(p[0], toStringSet(p[1].split("[&/,]")));
                        }).collect(Collectors.toMap(P::getKey, P::getValue));

                Set<String> artists = allArtists.get("\u30a2\u30fc\u30c6\u30a3\u30b9\u30c8");
                Set<String> lyricists = allArtists.get("\u4f5c\u66f2");
                Set<String> composers = allArtists.get("\u4f5c\u8a5e");

                if (nonNullAndNonEmpty(artists))
                    header.setArtist(artists);
                if (nonNullAndNonEmpty(lyricists))
                    header.setLyricist(lyricists);
                if (nonNullAndNonEmpty(composers))
                    header.setComposer(composers);
            }
        }

        return header;
    }

    @Override
    Lyrics lyrics() {
        if (isNull(lyrics)) {
            Matcher lyricsTextMatcher = lyricsPattern.matcher(doc);

            if (lyricsTextMatcher.find()) {
                String[] lyricsText = lyricsTextMatcher.group(1).split("\\n");

                lyrics = toLyrics(Parser::parseHtml, lyricsText);
            } else
                throw new AssertionError("The regex matching lyrics ran across a problem.");
        }

        return lyrics;
    }

    @Override
    String songPageUrl() {
        return HOSTNAME + "/lyrics/" + songCode;
    }
}
