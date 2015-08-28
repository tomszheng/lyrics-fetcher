package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kumasuke.fetcher.util.Tools.toList;
import static com.kumasuke.fetcher.util.Tools.toSet;

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
     * 将一个或多个 {@code String} 对象转换为一个 {@code Set} 对象，同时去除字符串两端空格。<br>
     * 效果等同于调用 {@code Tools.toSet(String::trim, args)}，使用次数最多，作为简化。
     *
     * @param args 需要转换的 {@code String} 对象
     * @return 装有传入的 {@code String} 对象的 {@code Set} 对象
     */
    static Set<String> toStringSet(String... args) {
        return toSet(String::trim, args);
    }

    /**
     * 将正则表达式 {@code Matcher} 对象中匹配的所有文本装入一个 {@code ListLyrics} 容器中。
     *
     * @param matcher 正则表达式 {@code Matcher} 对象
     * @return 存放歌词文本的{@code ListLyrics} 容器
     */
    static ListLyrics toLyrics(Matcher matcher) {
        List<String> result = new ArrayList<>();

        while (matcher.find())
            result.add(matcher.group(1).trim());

        return new ListLyrics(result);
    }

    /**
     * 将给定的一条或多条歌词文本装入 {@code ListLyrics} 歌词文本容器并返回。
     *
     * @param args 一条或多条歌词文本
     * @return {@code ListLyrics} 歌词文本容器
     */
    static ListLyrics toLyrics(String... args) {
        return new ListLyrics(toList(String::trim, args));
    }

    /**
     * 使用给定映射将一条或多条给定参数映射为 {@code String}，装入 {@code ListLyrics} 歌词文本容器并返回。
     *
     * @param mapper 指定映射
     * @param args   指定参数
     * @param <T>    指定参数类型
     * @return {@code ListLyrics} 歌词文本容器
     */
    @SafeVarargs
    static <T> ListLyrics toLyrics(Function<T, String> mapper, T... args) {
        return new ListLyrics(toList(e -> mapper.apply(e).trim(), args));
    }

    /**
     * 使用给定映射将给定集合类内的内容映射为 {@code String}，装入 {@code ListLyrics} 歌词文本容器并返回。
     *
     * @param mapper     指定映射
     * @param collection 指定集合类
     * @param <T>        指定集合类内容物的类型
     * @return {@code ListLyrics} 歌词文本容器
     */
    static <T> ListLyrics toLyrics(Function<T, String> mapper, Collection<T> collection) {
        List<String> data = collection.stream()
                .map(e -> mapper.apply(e).trim())
                .collect(Collectors.toList());

        return new ListLyrics(data);
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
