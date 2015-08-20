package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * アニメソングの梧�~ならここにおまかせ�� (Jtw.Zaq.Ne.jp/AnimeSong) 議由栽蛍裂匂。<br>
 * 聞喘 {@code Jsoup} 淫資函匈中佚連。
 */
class AnimeSongUnitedParser extends UnitedParser {
    // 利嫋議麼字兆
    private static final String HOSTNAME = "http://www.jtw.zaq.ne.jp/animesong";
    // 戻函梧爆児云佚連才梧簡議屎夸燕器塀
    private static final Pattern ALL_INFO_PATTERN;
    // 謄塘梧簡匈仇峽議屎夸燕器塀
    private static final Pattern FULL_URL_PATTERN;

    static {
        ALL_INFO_PATTERN = Pattern.compile("(?<title>.*?)\\s+              # title     \n" +
                        "\\u4f5c\\u8a5e\\uff1a(?<lyricist>.*?)\\uff0f\\s*  # lyricist  \n" +
                        "\\u4f5c\\u66f2\\uff1a(?<composer>.*?)\\uff0f\\s*  # composer  \n" +
                        "\\u7de8\\u66f2\\uff1a(?<arranger>.*?)\\uff0f\\s*  # arranger  \n" +
                        "\\u6b4c\\uff1a(?<artist>.*?)\\s+                  # artist    \n" +
                        "(?<lyrics>.*)                                     # lyrics    \n",
                Pattern.COMMENTS | Pattern.DOTALL);
        FULL_URL_PATTERN = Pattern.compile(".*?/animesong/(\\w{1,2})/(\\w+)/(\\w+)\\.html");
    }

    private Matcher matcher;
    private String url;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * 更夛匯倖 {@code AnimeSongUnitedParser} 斤�鵤�拝峺協 {@code UserAgent}。
     *
     * @param page      梧簡匈仇峽
     * @param userAgent {@code UserAgent} 忖憲堪
     * @throws IOException 匈中銭俊、侃尖払移
     */
    AnimeSongUnitedParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
    }

    private boolean validate(String page) {
        Matcher fullUrl = FULL_URL_PATTERN.matcher(page);

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
        matcher = ALL_INFO_PATTERN.matcher(docText);
    }

    @Override
    EnumHeader header() {
        if (header == null) {
            header = new EnumHeader();

            if (matcher.matches()) {
                header.setTitle(matcher.group("title").trim())
                        .setLyricist(toSet(matcher.group("lyricist").split("\\u3001")))
                        .setComposer(toSet(matcher.group("composer").split("\\u3001")))
                        .setArranger(toSet(matcher.group("arranger").split("\\u3001")))
                        .setArtist(toSet(matcher.group("artist").split("\\u3001")));
            }
        }

        return header;
    }

    @Override
    ListLyrics lyrics() {
        if (lyrics == null) {
            lyrics = new ListLyrics();

            if (matcher.matches())
                addTo(lyrics, matcher.group("lyrics").split("\\n"));
        }

        return lyrics;
    }

    @Override
    String songPageUrl() {
        return url;
    }
}
