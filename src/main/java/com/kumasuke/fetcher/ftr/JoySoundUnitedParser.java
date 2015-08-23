package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.URLReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JoySound (JoySound.com) 的统合分析器。<br>
 * 使用 {@code JSON.simple} 包获取页面信息。
 */
class JoySoundUnitedParser extends UnitedParser {
    // 网站的主机名
    private static final String HOSTNAME = "https://www.joysound.com";
    // 获取歌曲基本信息和歌词的 Json 地址
    private static final String ALL_INFO_JSON_URL = "https://mspxy.joysound.com/Common/Lyric";
    // 匹配歌词页地址的正则表达式
    private static final Pattern FULL_URL_PATTERN;
    // 匹配歌曲代码的正则表达式
    private static final Pattern SONG_CODE_PATTERN;

    static {
        FULL_URL_PATTERN = Pattern.compile(".*?/web/search/song/(\\d+)/?");
        SONG_CODE_PATTERN = Pattern.compile("\\d+");
    }

    private JSONObject json;
    private String songCode;

    private EnumHeader header;
    private ListLyrics lyrics;

    /**
     * 构造一个 {@code JoySoundUnitedParser} 对象，且指定 {@code UserAgent}。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException    页面连接、处理失败
     * @throws ParseException JSON 解析失败
     */
    JoySoundUnitedParser(String page, String userAgent) throws IOException, ParseException {
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

    private void initialize(String userAgent) throws IOException, ParseException {
        try (Reader reader = URLReader.connect(ALL_INFO_JSON_URL)
                .timeout(5000)
                .referer(songPageUrl())
                .userAgent(userAgent)
                .requestProperty(toMap(p("X-JSP-APP-NAME", "0000800")))
                .usePost(true)
                .requestParameter(lrcJsonParameters())
                .getReader()) {
            this.json = (JSONObject) new JSONParser().parse(reader);
        }
    }

    private Map<String, String> lrcJsonParameters() {
        return toMap(p("kind", "naviGroupId"), p("selSongNo", songCode), p("interactionFlg", "0"), p("apiVer", "1.0"));
    }

    /**
     * 获取歌曲基本信息。
     *
     * @return 装有歌曲信息的 {@code Header} 容器
     */
    @Override
    EnumHeader header() {
        if (header == null) {
            header = new EnumHeader();

            header.setTitle(((String) json.get("songName")).trim())
                    .setArtist(toSet(((String) json.get("artistName")).split("\\uff0c")))
                    .setLyricist(toSet(((String) json.get("lyricist")).split("\\uff0c")))
                    .setComposer(toSet(((String) json.get("composer")).split("\\uff0c")));
        }

        return header;
    }

    /**
     * 获取歌词文本。
     *
     * @return 装有歌词文本的 {@code Lyrics} 容器
     */
    @Override
    ListLyrics lyrics() {
        if (lyrics == null) {
            lyrics = new ListLyrics();

            JSONArray lrcArray = (JSONArray) json.get("lyricList");
            JSONObject lrcObj = (JSONObject) lrcArray.get(0);
            String lrcAll = (String) lrcObj.get("lyric");
            String[] lyricsText = lrcAll.split("\\n");
            addTo(lyrics, lyricsText);
        }

        return lyrics;
    }

    @Override
    String songPageUrl() {
        return HOSTNAME + "/web/search/song/" + songCode + "/";
    }
}
