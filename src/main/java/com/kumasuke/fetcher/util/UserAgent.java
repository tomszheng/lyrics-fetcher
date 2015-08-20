package com.kumasuke.fetcher.util;

import java.util.Random;

/**
 * 提供预设的各大浏览器 {@code UserAgent} 字符串
 */
public class UserAgent {
    /**
     * Chrome 42 浏览器 {@code UserAgent} 字符串
     */
    public static final String CHROME = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/42.0.2311.90 Safari/537.36";

    /**
     * Firefox 37 浏览器 {@code UserAgent} 字符串
     */
    public static final String FIREFOX = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:36.0) Gecko/20100101 Firefox/37.0";

    /**
     * IE 11 浏览器 {@code UserAgent} 字符串
     */
    public static final String IE = "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; MALNJS; rv:11.0) like Gecko";

    /**
     * Microsoft Edge 浏览器 {@code UserAgent} 字符串
     */
    public static final String EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like " +
            "Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240";

    private static final Random GEN = new Random();

    // 工具类，防止被创建
    private UserAgent() {
        throw new AssertionError();
    }

    /**
     * 获取 {@code UserAgent} 字符串。
     *
     * @return {@code UserAgent} 字符串
     */
    public static String getUserAgent() {
        switch (GEN.nextInt(4)) {
            case 0:
                return CHROME;
            case 1:
                return FIREFOX;
            case 2:
                return IE;
            case 3:
                return EDGE;
            default:
                throw new AssertionError("Won't happen.");
        }
    }
}
