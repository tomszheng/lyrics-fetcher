package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.Tools;
import org.jsoup.Jsoup;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 通用分析器，提供公用常量与方法。
 */
abstract class Parser {
    // Flash 播放器版本
    static final String FLASH_VERSION = "ShockwaveFlash/18.0.0.232";
    // '&nbsp;' 被 Jsoup 解析成的值，其 Unicode 值为 '\u00a0'
    static final String JSOUP_NBSP = "\u00a0";

    // 用于去除两端半角 / 全角空格的正则表达式
    static final Pattern SUPER_TRIM_PATTERN;
    // 用于去除两端 Html 和普通空格的正则表达式
    static final Pattern HTML_TRIM_PATTERN;

    // 匹配纯数字歌曲代码的正则表达式
    static final Pattern NUMBER_SONG_CODE_PATTERN;
    // 匹配字母数字混合歌曲代码的正则表达式
    static final Pattern WORD_SONG_CODE_PATTERN;

    static {
        SUPER_TRIM_PATTERN = Pattern.compile("[\\u3000\\s]*(.*?)[\\u3000\\s]*");
        HTML_TRIM_PATTERN = Pattern.compile("[\\u00a0\\s]*(.*?)[\\u00a0\\s]*");
        NUMBER_SONG_CODE_PATTERN = Pattern.compile("\\d+");
        WORD_SONG_CODE_PATTERN = Pattern.compile("[-\\w]+");
    }

    /**
     * 将一个或多个对象转换为一个 {@code Set} 对象, 同时去除字符串两端空格。
     *
     * @param args 需要转换的 {@code String} 对象
     * @return 装有传入的 {@code String} 对象的 {@code Set} 对象
     */
    static Set<String> toSet(String... args) {
        return Tools.toSet(String::trim, args);
    }

    /**
     * 将一个或多个 {@code String} 对象转换为一个 {@code Set} 对象。<br>
     * 使用给定的截取器截取字符串。
     *
     * @param trimmer 字符串截取器
     * @param args    需要转换的 {@code String} 对象
     * @return 装有传入的 {@code String} 对象的 {@code Set} 对象
     */
    static Set<String> toSet(Function<String, String> trimmer, String... args) {
        return Tools.toSet(trimmer, args);
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
     * 使用给定方法修饰一行或多行歌词并将其装入 {@code ListLyrics} 歌词容器中。
     *
     * @param dest   {@code ListLyrics} 歌词容器
     * @param mapper 修饰方法
     * @param args   歌词文本
     */
    static void addTo(ListLyrics dest, Function<String, String> mapper, String... args) {
        Stream.of(args)
                .map(String::trim)
                .map(mapper)
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

    /**
     * 去除字符串两端多于的所有半角 / 全角空格。
     *
     * @param str 需要截取的字符串
     * @return 截取完成的字符串
     */
    static String superTrim(String str) {
        Matcher matcher = SUPER_TRIM_PATTERN.matcher(str);

        if (matcher.matches())
            return matcher.group(1);
        else
            throw new AssertionError("The regex always matches.");
    }

    /**
     * 去除字符串多于的所有 Html 和普通空格。
     *
     * @param str 需要截取的字符串
     * @return 截取完成的字符串
     */
    static String htmlTrim(String str) {
        Matcher matcher = HTML_TRIM_PATTERN.matcher(str);

        if (matcher.matches())
            return matcher.group(1);
        else
            throw new AssertionError("The regex always matches.");
    }

    /**
     * 将 Html 标签转换为普通文本。
     *
     * @param html 需要转换 Html 文本
     * @return 转换完成的普通文本
     */
    static String parseHtml(String html) {
        return Jsoup.parse(html).text();
    }
}
