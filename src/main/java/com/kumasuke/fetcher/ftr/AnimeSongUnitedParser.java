package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ���˥᥽�󥰤θ��~�ʤ餳���ˤ��ޤ����� (Jtw.Zaq.Ne.jp/AnimeSong) ��ͳ�Ϸ�������<br>
 * ʹ�� {@code Jsoup} ����ȡҳ����Ϣ��
 */
class AnimeSongUnitedParser extends UnitedParser {
    // ��վ��������
    private static final String HOSTNAME = "http://www.jtw.zaq.ne.jp/animesong";
    // ��ȡ����������Ϣ�͸�ʵ�������ʽ
    private static final Pattern ALL_INFO_PATTERN;
    // ƥ����ҳ��ַ��������ʽ
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
     * ����һ�� {@code AnimeSongUnitedParser} ������ָ�� {@code UserAgent}��
     *
     * @param page      ���ҳ��ַ
     * @param userAgent {@code UserAgent} �ַ���
     * @throws IOException ҳ�����ӡ�����ʧ��
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
