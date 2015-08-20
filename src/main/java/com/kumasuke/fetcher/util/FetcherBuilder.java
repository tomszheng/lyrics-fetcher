package com.kumasuke.fetcher.util;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.ftr.*;

import java.io.IOException;
import java.util.Objects;

/**
 * {@code Fetcher} 构造器，用于统一地构造 {@code Fetcher} 对象
 */
public class FetcherBuilder {
    private String site;
    private String page;
    private String userAgent;

    /**
     * 构造一个 {@code FetcherBuilder} 对象，并设置默认 {@code UserAgent} 字符串。
     */
    private FetcherBuilder() {

    }

    /**
     * 获取一个新的 {@code FetcherBuilder} 对象<br>
     * 该对象拥有默认的 {@code UserAgent} 字符串。
     *
     * @return {@code FetcherBuilder} 对象
     */
    public static FetcherBuilder builder() {
        return new FetcherBuilder();
    }

    /**
     * 设置所要解析的歌词网站，如果设置了不支持的网站，则会在构造时抛出异常。
     *
     * @param site 歌词网站的域名，不区分大小写<br>
     *             <br>
     *             目前支持的域名如下：<br>
     *             uta-net.com<br>
     *             j-lyric.net<br>
     *             utamap.com<br>
     *             kasi-time.com<br>
     *             kashinavi.com<br>
     *             kget.jp<br>
     *             utaten.com<br>
     *             animap.jp<br>
     *             evesta.jp<br>
     *             jtw.zaq.ne.jp/animesong
     * @return {@code FetcherBuilder} 对象，便于链式编程
     */
    public FetcherBuilder site(String site) {
        this.site = Objects.requireNonNull(site).toLowerCase();

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
            Objects.requireNonNull(site, "The parameter 'site' haven't been set yet.");
            Objects.requireNonNull(page, "The parameter 'page' haven't been set yet.");
        } catch (NullPointerException e) {
            throw new IllegalStateException(e);
        }

        if (userAgent == null || userAgent.isEmpty())
            userAgent = UserAgent.getUserAgent();

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
            default:
                throw new IllegalArgumentException
                        ("Unable to resolve the parameter 'site': " + site);
        }

        reset();

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
}
