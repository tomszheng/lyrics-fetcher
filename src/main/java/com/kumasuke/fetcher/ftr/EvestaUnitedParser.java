package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ���٥��� (Evesta.jp) ��ͳ�Ϸ�������<br>
 * ʹ�� {@code Jsoup} ����ȡҳ����Ϣ��
 */
class EvestaUnitedParser extends UnitedParser {
    // ��վ��������
    private static final String HOSTNAME = "http://www.evesta.jp/";
    // ��ȡ���������������ʽ
    private static final Pattern TITLE_PATTERN;
    // ��ȡ����������Ϣ��������ʽ
    private static final Pattern INFO_PATTERN;
    // ƥ����ҳ��ַ��������ʽ
    private static final Pattern FULL_URL_PATTERN;

    static {
        TITLE_PATTERN = Pattern.compile("(.*?)\\u6b4c\\u8a5e\\s\\u002f.*");
        INFO_PATTERN = Pattern.compile("\\u6b4c\\uff1a(.*?)   # artist    \n" +
                        "\\u4f5c\\u8a5e\\uff1a(.*?)           # lyricist  \n" +
                        "\\u4f5c\\u66f2\\uff1a(.*)            # composer  \n",
                Pattern.COMMENTS);
        FULL_URL_PATTERN = Pattern.compile(".*?/lyric/artists/(a\\d+)/lyrics/(l\\d+)\\.html");
    }

    private Document doc;
    private String url;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * ����һ�� {@code EvestaUnitedParser} ������ָ�� {@code UserAgent}��
     *
     * @param page      ���ҳ��ַ
     * @param userAgent {@code UserAgent} �ַ���
     * @throws IOException ҳ�����ӡ�����ʧ��
     */
    EvestaUnitedParser(String page, String userAgent) throws IOException {
        if (!validate(page))
            throw new IllegalArgumentException("Unable to resolve the parameter page: " + page);

        initialize(userAgent);
    }

    private boolean validate(String page) {
        Matcher fullUrl = FULL_URL_PATTERN.matcher(page);

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
     * ��ȡ����������Ϣ��<br>
     *
     * @return װ�и�����Ϣ�� {@code Header} ����
     * @implSpec ���ε���ʱ�����ʼ����Ҫ���صĶ����⽫�ķ�һ����ʱ�䡣
     */
    @Override
    EnumHeader header() {
        if (header == null) {
            header = new EnumHeader();

            Element title = doc.select("#titleBand h1").first();
            Matcher titleMatcher = TITLE_PATTERN.matcher(title.text());
            if (titleMatcher.matches())
                header.setTitle(titleMatcher.group(1).trim());

            Element artists = doc.select("#descriptionBand div.artists").first();
            Matcher matcher = INFO_PATTERN.matcher(artists.text());
            if (matcher.matches())
                header.setArtist(toSet(true, matcher.group(1).split("/")))
                        .setLyricist(toSet(true, matcher.group(2).split("/")))
                        .setComposer(toSet(true, matcher.group(3).split("/")));
        }

        return header;
    }

    /**
     * ��ȡ����ı���<br>
     *
     * @return װ�и���ı��� {@code Lyrics} ����
     * @implSpec ���ε���ʱ�����ʼ����Ҫ���صĶ����⽫�ķ�һ����ʱ�䡣
     */
    @Override
    ListLyrics lyrics() {
        if (lyrics == null) {
            lyrics = new ListLyrics();

            Element lrcBody = doc.select("#lyricview div.body p").first();
            addTo(lyrics, lrcBody.html().split("<br(?: /)?>"));
        }

        return lyrics;
    }

    /**
     * ��ȡ���ҳ��ַ��
     *
     * @return ���ҳ��ַ
     */
    @Override
    String songPageUrl() {
        return url;
    }
}
