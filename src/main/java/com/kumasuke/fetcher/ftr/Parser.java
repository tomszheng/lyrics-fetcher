package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通用分析器，提供公用常量与方法。
 */
abstract class Parser {
    // '&nbsp;' 被 Jsoup 解析成的值，其 Unicode 值为 '\u00a0'
    static final String JSOUP_NBSP = Jsoup.parse("&nbsp;").text();
    // Flash 播放器版本
    static final String FLASH_VERSION = "ShockwaveFlash/18.0.0.232";
    // 用于去除两端全角空格的正则表达式
    static final Pattern SUPER_TRIM_PATTERN;

    static {
        SUPER_TRIM_PATTERN = Pattern.compile("\\u3000*(.*?)\\u3000*");
    }

    /**
     * 将一个或多个 {@code String} 对象转换为一个 {@code Set} 对象。
     *
     * @param args 需要转换的 {@code String} 对象
     * @return 装有传入的 {@code String} 对象的 {@code Set} 对象
     */
    static Set<String> toSet(String... args) {
        return Stream.of(args)
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    /**
     * 将一个或多个 {@code String} 对象转换为一个 {@code Set} 对象。<br>
     * 可使用 {@link Parser#superTrim(String) superTrim(String} 截除多于半角和全角空格。
     *
     * @param isSuperTrim 是否使用超级截除
     * @param args        需要转换的 {@code String} 对象
     * @return 装有传入的 {@code String} 对象的 {@code Set} 对象
     */
    static Set<String> toSet(boolean isSuperTrim, String... args) {
        if (!isSuperTrim)
            return toSet(args);
        else
            return Stream.of(args)
                    .map(Parser::superTrim)
                    .collect(Collectors.toSet());
    }

    /**
     * 将一行或多行歌词装入 {@code ListLyrics} 歌词容器中。
     *
     * @param dest {@code ListLyrics} 歌词容器
     * @param args 歌词文本
     */
    static void addTo(ListLyrics dest, String... args) {
        Stream.of(args)
                .map(String::trim)
                .forEach(dest::addLine);
    }

    /**
     * 将一行或多行歌词装入 {@code ListLyrics} 歌词容器中。
     *
     * @param dest    {@code ListLyrics} 歌词容器
     * @param matcher 匹配歌词文本的 {code Matcher}
     */
    static void addTo(ListLyrics dest, Matcher matcher) {
        while (matcher.find())
            dest.addLine(matcher.group(1).trim());
    }

    static String superTrim(String str) {
        Matcher matcher = SUPER_TRIM_PATTERN.matcher(str);

        return matcher.matches() ? matcher.group(1).trim() : str.trim();
    }
}
