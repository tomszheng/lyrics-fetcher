package com.kumasuke.fetcher.util;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.ftr.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kumasuke.fetcher.util.Tools.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

/**
 * {@code Fetcher} 构造器，用于统一地构造 {@code Fetcher} 对象
 */
public class FetcherBuilder {
    // 验证网址的正则表达式
    private static final Map<String, Pattern> URL_TO_SITE_PATTERNS = new HashMap<>();

    static {
        URL_TO_SITE_PATTERNS.put("uta-net.com", Pattern.compile(".*?uta-net\\.com/song/\\d+/?"));
        URL_TO_SITE_PATTERNS.put("j-lyric.net", Pattern.compile(".*?j-lyric\\.net/artist/a\\w+/l\\w+\\.html"));
        URL_TO_SITE_PATTERNS.put("utamap.com",
                Pattern.compile(".*?utamap\\.com/show(?:kasi|top)\\.php\\?surl=[-\\w]+"));
        URL_TO_SITE_PATTERNS.put("kasi-time.com", Pattern.compile(".*?kasi-time\\.com/item-\\d+\\.html"));
        URL_TO_SITE_PATTERNS.put("kashinavi.com", Pattern.compile(".*?kashinavi\\.com/song_view\\.html\\?\\d+"));
        URL_TO_SITE_PATTERNS.put("kget.jp", Pattern.compile(".*?kget\\.jp/lyric/\\d+/?.*"));
        URL_TO_SITE_PATTERNS.put("utaten.com", Pattern.compile(".*?utaten\\.com/lyric/[^/]+/[^/]+/?"));
        URL_TO_SITE_PATTERNS.put("animap.jp", Pattern.compile(".*?animap\\.jp/kasi/showkasi\\.php\\?surl=[-\\w]+"));
        URL_TO_SITE_PATTERNS.put("evesta.jp",
                Pattern.compile(".*?evesta\\.jp/lyric/artists/a\\d+/lyrics/l\\d+\\.html"));
        URL_TO_SITE_PATTERNS.put("joysound.com", Pattern.compile(".*?joysound\\.com/web/search/song/\\d+/?"));
        URL_TO_SITE_PATTERNS.put("jtw.zaq.ne.jp/animesong",
                Pattern.compile(".*?jtw\\.zaq\\.ne\\.jp/animesong/\\w{1,2}/\\w+/\\w+\\.html"));
        URL_TO_SITE_PATTERNS.put("petitlyrics.com", Pattern.compile(".*?petitlyrics\\.com/lyrics/\\d+"));
    }

    private String site;
    private String page;
    private String userAgent;

    /**
     * 构造一个 {@code FetcherBuilder} 对象，并设置默认 {@code UserAgent} 字符串。
     */
    private FetcherBuilder() {

    }

    /**
     * 获取一个新的 {@code FetcherBuilder} 对象。
     *
     * @return {@code FetcherBuilder} 对象
     */
    public static FetcherBuilder newBuilder() {
        return new FetcherBuilder();
    }

    private static String matchSiteFromPage(String page) {
        for (Map.Entry<String, Pattern> e : URL_TO_SITE_PATTERNS.entrySet()) {
            String s = e.getKey();
            Pattern p = e.getValue();
            Matcher m = p.matcher(page);

            if (m.matches()) return s;
        }

        throw new IllegalArgumentException("Cannot match site according to given parameter 'page'!");
    }

    /**
     * 设置所要解析的歌词网站，如果设置了不支持的网站，则会在构造时抛出异常。
     *
     * @param site 歌词网站的域名，不区分大小写<br>
     *             特别地，如果传入站点参数（site）为 *，将会根据页面参数（page）自动匹配站点。<br>
     *             自动匹配只能匹配完整的歌词地址，而不能匹配歌曲代码。<br>
     *             如果自动匹配失败，构造时将会抛出 {@code IllegalArgumentException} 异常。
     *             <p>目前支持的域名如下：<br>
     *             uta-net.com<br>
     *             j-lyric.net<br>
     *             utamap.com<br>
     *             kasi-time.com<br>
     *             kashinavi.com<br>
     *             kget.jp<br>
     *             utaten.com<br>
     *             animap.jp<br>
     *             evesta.jp<br>
     *             joysound.com<br>
     *             jtw.zaq.ne.jp/animesong<br>
     *             petitlyrics.com</p>
     * @return {@code FetcherBuilder} 对象，便于链式编程
     */
    public FetcherBuilder site(String site) {
        this.site = requireNonNull(site).toLowerCase();

        return this;
    }

    /**
     * 设置所要解析的歌词页地址或歌曲代码，需和 {@code site} 值对应。
     *
     * @param page 歌词页地址或歌曲代码
     * @return {@code FetcherBuilder} 对象，便于链式编程
     */
    public FetcherBuilder page(String page) {
        this.page = page;

        return this;
    }

    /**
     * 设置 {@code UserAgent} 字符串，如果未进行设置将使用默认值。
     *
     * @param userAgent {@code UserAgent} 字符串
     * @return {@code FetcherBuilder} 对象，便于链式编程
     */
    public FetcherBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;

        return this;
    }

    /**
     * 根据设置的参数构造相应的 {@code Fetcher} 对象。<br>
     * 构造完成后，将会重置该 {@code FetcherBuilder}。<br>
     * 如果参数未设置或设置不全将会抛出 {@code IllegalStateException} 异常。
     *
     * @return {@code Fetcher} 对象
     * @throws IOException 构造失败
     */
    public Fetcher build() throws IOException {
        try {
            requireNonNull(site, "The parameter 'site' haven't been set yet.");
            requireNonNull(page, "The parameter 'page' haven't been set yet.");
        } catch (NullPointerException e) {
            throw new IllegalStateException(e);
        }

        if (isNullOrEmpty(userAgent))
            userAgent = UserAgent.getUserAgent();

        if (site.equals("*"))
            site = matchSiteFromPage(page);

        Fetcher fetcher;

        switch (site) {
            case "uta-net.com":
                fetcher = new UtaNetFetcher(page, userAgent);
                break;
            case "j-lyric.net":
                fetcher = new JLyricFetcher(page, userAgent);
                break;
            case "utamap.com":
                fetcher = new UtaMapFetcher(page, userAgent);
                break;
            case "kasi-time.com":
                fetcher = new KasiTimeFetcher(page, userAgent);
                break;
            case "kashinavi.com":
                fetcher = new KashiNaviFetcher(page, userAgent);
                break;
            case "kget.jp":
                fetcher = new KGetFetcher(page, userAgent);
                break;
            case "utaten.com":
                fetcher = new UtaTenFetcher(page, userAgent);
                break;
            case "animap.jp":
                fetcher = new AniMapFetcher(page, userAgent);
                break;
            case "evesta.jp":
                fetcher = new EvestaFetcher(page, userAgent);
                break;
            case "jtw.zaq.ne.jp/animesong":
                fetcher = new AnimeSongFetcher(page, userAgent);
                break;
            case "joysound.com":
                fetcher = new JoySoundFetcher(page, userAgent);
                break;
            case "petitlyrics.com":
                fetcher = new PetitLyricsFetcher(page, userAgent);
                break;
            default:
                throw new IllegalArgumentException
                        ("Unable to resolve the parameter 'site': " + site);
        }

        return fetcher;
    }

    /**
     * 重置该 {@code FetcherBuilder}。
     *
     * @return {@code FetcherBuilder} 对象，便于链式编程
     */
    public FetcherBuilder reset() {
        site = page = userAgent = null;

        return this;
    }

    /**
     * 调用该方法后，构造时将根据页面参数（page）匹配站点。<br>
     * 如果自动匹配失败，构造时将会抛出 {@code IllegalArgumentException} 异常。<br>
     * 效果等同于调用 {@code FetcherBuilder.site("*")}，因此若在调用该方法之前或之后调用
     * {@link FetcherBuilder#site FetcherBuilder.site(String)}，将会覆盖该操作。
     *
     * @return {@code FetcherBuilder} 对象，便于链式编程
     */
    public FetcherBuilder autoMatch() {
        site = "*";

        return this;
    }
}
