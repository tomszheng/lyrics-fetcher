package com.kumasuke.fetcher.util;

import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 格式化器，用于格式化相应文本，便于显示和操作
 */
public class Formatter {
    // Html 换行符
    private static final String HTML_BR = "<br>";
    // 系统依赖的字符串换行符
    private static final String TEXT_BR = System.getProperty("line.separator");

    // 工具类，防止被创建
    private Formatter() {
        throw new AssertionError();
    }

    /**
     * 提取给定 {@code Header} 对象中的 标题和艺术家信息，并按照指定格式和扩展名生成文件名。<br>
     *
     * @param header    {@code Header} 对象
     * @param format    文件名格式字符串，其语法如下：<br>
     *                  %ar% 艺术家<br>
     *                  %ti% 标题<br>
     *                  <p>示例（歌曲名：君への嘘，艺术家：VALSHE，扩展名：txt）：<br>
     *                  输入格式字符串为 {@code "%ar% - %ti%"}，则输出文件名为 "VALSHE - 君への嘘.txt"</p>
     * @param extension 文件扩展名，可省略点号（.）
     * @return 按照给定格式生成的文件名
     */
    public static String getFilename(Header header, String format, String extension) {
        String ar = header.getArtist()
                .stream()
                .collect(Collectors.joining(","));
        String ti = header.getTitle();
        String ext = extension.replaceAll("\\.?(\\w+)", ".$1");

        String result = format.replace("%ar%", ar)
                .replace("%ti%", ti) + ext;

        return result.replaceAll("[\\\\/:*?\"<>|]", "");
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
     * 换行符为 <code>System.getProperty("line.separator")</code>。
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
     * 换行符为 <code>System.getProperty("line.separator")</code>。
     *
     * @param lyrics {@code Lyrics} 对象
     * @return 纯文本的歌词文本
     */
    public static String lyricsToText(Lyrics lyrics) {
        return formatLyrics(lyrics, TEXT_BR);
    }

    private static String formatSet(Set<String> set) {
        return set.stream()
                .collect(Collectors.joining(", "));
    }

    private static String formatHeader(Header header, String br) {
        StringBuilder text = new StringBuilder();

        text.append(header.getTitle())
                .append(br)
                .append(br);
        text.append("\u6b4c\u624b\uff1a")
                .append(formatSet(header.getArtist()))
                .append(br);
        text.append("\u4f5c\u8a5e\uff1a")
                .append(formatSet(header.getLyricist()))
                .append(br);
        text.append("\u4f5c\u66f2\uff1a")
                .append(formatSet(header.getComposer()));

        // 处理非必须的编曲
        Set<String> arg = header.getArranger();
        if (arg != null && !arg.isEmpty())
            text.append(br)
                    .append("\u7de8\u66f2\uff1a")
                    .append(formatSet(arg));

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
