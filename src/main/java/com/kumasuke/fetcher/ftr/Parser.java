package com.kumasuke.fetcher.ftr;

import org.jsoup.Jsoup;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通用分析器，提供公用常量与方法。
 */
abstract class Parser {
    // Flash 播放器版本
    static final String FLASH_VERSION = "ShockwaveFlash/18.0.0.232";
    // 通用 Http 请求属性 X-Requested-With 键值对
    static final P X_REQUESTED_WITH_PROPERTY = p("X-Requested-With", FLASH_VERSION);

    // 用于去除两端半角 / 全角空格的正则表达式
    static final Pattern SUPER_TRIM_PATTERN;
    // 用于去除两端 Html 和普通空格的正则表达式
    static final Pattern HTML_TRIM_PATTERN;

    static {
        SUPER_TRIM_PATTERN = Pattern.compile("[\\u3000\\s]*(.*?)[\\u3000\\s]*");
        HTML_TRIM_PATTERN = Pattern.compile("[\\u00a0\\s]*(.*?)[\\u00a0\\s]*");
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
     * 使用给定的截取器截取字符串。
     *
     * @param trimmer 字符串截取器
     * @param args    需要转换的 {@code String} 对象
     * @return 装有传入的 {@code String} 对象的 {@code Set} 对象
     */
    static Set<String> toSet(Function<String, String> trimmer, String... args) {
        return Stream.of(args)
                .map(trimmer)
                .collect(Collectors.toSet());
    }

    /**
     * 生成一个 {@code String} 键值对。
     *
     * @param key   {@code key} 值
     * @param value {@code value} 值
     * @return 生成的键值对，可作为 {@link Parser#toMap(P...) toMap(E...)} 的参数
     */
    static P p(String key, String value) {
        return new P(key, value);
    }

    /**
     * 将一个或多个 {@code String} 键值对转换为一个 {@code Map} 对象。
     *
     * @param args 需要转换的 {@code String} 键值对，由 {@link Parser#p(String, String) p(String, String)} 方法生成
     * @return 装有传入的 {@code String} 键值对的 {@code Map} 对象
     */
    static Map<String, String> toMap(P... args) {
        return Stream.of(args)
                .collect(Collectors.toMap(P::getKey, P::getValue));
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
     * @param mapper 修饰方法
     * @param dest   {@code ListLyrics} 歌词容器
     * @param args   歌词文本
     */
    static void addTo(Function<String, String> mapper, ListLyrics dest, String... args) {
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

    /**
     * 临时键值对，外部仅可使用 {@link Parser#p(String, String) p(String, String)} 方法创建。<br>
     * 无法修改，且无法从外部访问内部值。
     */
    static class P {
        private final String key;
        private final String value;

        private P(String key, String value) {
            this.key = Objects.requireNonNull(key, "The 'key' must not be null.");
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }
    }
}
