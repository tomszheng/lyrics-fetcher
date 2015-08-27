package com.kumasuke.fetcher.util;

import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kumasuke.fetcher.util.Tools.nonNullAndNonEmpty;

/**
 * 格式化器，用于格式化相应文本，便于显示和操作
 */
public class Formatter {
    // Html 换行符
    private static final String HTML_BR = "<br>";
    // 系统依赖的字符串换行符
    private static final String TEXT_BR = System.lineSeparator();

    // 工具类，防止被创建
    private Formatter() {
        throw new AssertionError();
    }

    /**
     * 提取给定 {@code Header} 对象中的 标题和艺术家信息，并按照指定格式生成字符串。<br>
     * 多个艺术家间的分隔符为 {@code ,}。<br>
     * 如果无法获得指定信息，则相应位置将为 {@code [unknown]}。
     *
     * @param header {@code Header} 对象
     * @param format 文件名格式字符串，其语法如下：<br>
     *               %ti% 标题<br>
     *               %ar% 演唱者<br>
     *               %lr% 作词者<br>
     *               %co% 作曲者<br>
     *               %ag% 编曲者
     *               <p>示例（歌曲名：君への嘘，艺术家：VALSHE）：<br>
     *               输入格式字符串为 {@code "%ar% - %ti%"}，则输出字符串为 "VALSHE - 君への嘘"</p>
     * @return 按照给定格式生成的字符串
     */
    public static String headerToFormattedString(Header header, String format) {
        return headerToFormattedString(header, format, ",");
    }

    /**
     * 提取给定 {@code Header} 对象中的 标题和艺术家信息，并按照指定格式和分隔生成字符串。<br>
     * 如果无法获得指定信息，则相应位置将为 {@code [unknown]}。
     *
     * @param header    {@code Header} 对象
     * @param format    文件名格式字符串，其语法如下：<br>
     *                  %ti% 标题<br>
     *                  %ar% 演唱者<br>
     *                  %lr% 作词者<br>
     *                  %co% 作曲者<br>
     *                  %ag% 编曲者
     *                  <p>示例（歌曲名：君への嘘，艺术家：VALSHE）：<br>
     *                  输入格式字符串为 {@code "%ar% - %ti%"}，则输出文件名为 "VALSHE - 君への嘘"</p>
     * @param delimiter 多个艺术家的分隔符
     * @return 按照给定格式生成的字符串
     */
    public static String headerToFormattedString(Header header, String format, String delimiter) {
        Map<String, Boolean> isTagFound = checkTags(format);
        Map<String, String> tagToContent = getContents(header, delimiter);

        String result = format;
        for (Map.Entry<String, Boolean> e : isTagFound.entrySet())
            if (e.getValue()) {
                String tag = e.getKey();
                String content = tagToContent.get(tag);

                result = result.replace(tag, content);
            }

        return result.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    private static Map<String, String> getContents(Header header, String delimiter) {
        Map<String, String> tagToContent = new HashMap<>(5);

        tagToContent.put("%ti%", header.getTitle());
        tagToContent.put("%ar%", formatSet(header.getArtist(), delimiter));
        tagToContent.put("%lr%", formatSet(header.getLyricist(), delimiter));
        tagToContent.put("%co%", formatSet(header.getComposer(), delimiter));
        tagToContent.put("%ag%", formatSet(header.getArranger(), delimiter));

        return tagToContent;
    }

    private static Map<String, Boolean> checkTags(String format) {
        Map<String, Boolean> isTagFound = new HashMap<>(5);

        checkTag(format, "%ti%", isTagFound);
        checkTag(format, "%ar%", isTagFound);
        checkTag(format, "%lr%", isTagFound);
        checkTag(format, "%co%", isTagFound);
        checkTag(format, "%ag%", isTagFound);

        return isTagFound;
    }

    private static void checkTag(String format, String tag, Map<String, Boolean> isTagFound) {
        isTagFound.put(tag, format.contains(tag));
    }

    /**
     * 转换 {@code Header} 对象中的信息至 Html 代码。<br>
     * Html 代码遵守 HTML5 规范，换行符为 <code>&lt;br&gt;</code>。
     *
     * @param header {@code Header} 对象
     * @return Html格式的歌曲基本信息文本
     */
    public static String headerToHtml(Header header) {
        return formatHeader(header, HTML_BR);
    }

    /**
     * 转换 {@code Header} 对象中的信息至纯文本。<br>
     * 换行符为与系统相关。
     *
     * @param header {@code Header} 对象
     * @return 纯文本格式的歌曲基本信息文本
     */
    public static String headerToText(Header header) {
        return formatHeader(header, TEXT_BR);
    }

    /**
     * 转换 {@code Lyrics} 对象中的歌词文本至 Html 代码。<br>
     * Html 代码遵守 HTML5 规范，换行符为 <code>&lt;br&gt;</code>。
     *
     * @param lyrics {@code Lyrics} 对象
     * @return Html格式的歌词文本
     */
    public static String lyricsToHtml(Lyrics lyrics) {
        return formatLyrics(lyrics, HTML_BR);
    }

    /**
     * 转换 {@code Lyrics} 对象中的歌词文本至纯文本。<br>
     * 换行符为与系统相关。
     *
     * @param lyrics {@code Lyrics} 对象
     * @return 纯文本的歌词文本
     */
    public static String lyricsToText(Lyrics lyrics) {
        return formatLyrics(lyrics, TEXT_BR);
    }

    private static String formatSet(Set<String> set, String delimiter) {
        if (nonNullAndNonEmpty(set))
            return set.stream()
                    .collect(Collectors.joining(delimiter));
        else
            return "[unknown]";
    }

    private static String formatHeader(Header header, String br) {
        StringBuilder text = new StringBuilder();

        text.append(header.getTitle())
                .append(br)
                .append(br);
        text.append("\u6b4c\u624b\uff1a")
                .append(formatSet(header.getArtist(), ", "));

        // 处理非必须的部分
        Set<String> lr = header.getLyricist();
        if (nonNullAndNonEmpty(lr))
            text.append(br)
                    .append("\u4f5c\u8a5e\uff1a")
                    .append(formatSet(lr, ", "));

        Set<String> co = header.getComposer();
        if (nonNullAndNonEmpty(co))
            text.append(br)
                    .append("\u4f5c\u66f2\uff1a")
                    .append(formatSet(co, ", "));

        Set<String> ag = header.getArranger();
        if (nonNullAndNonEmpty(ag))
            text.append(br)
                    .append("\u7de8\u66f2\uff1a")
                    .append(formatSet(ag, ", "));

        return text.toString();
    }

    private static String formatLyrics(Lyrics lyrics, String br) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < lyrics.lineCount(); i++) {
            text.append(lyrics.getLine(i));

            // 若不是最后一行，则添加换行符
            if (i != lyrics.lineCount() - 1)
                text.append(br);
        }

        return text.toString();
    }
}
