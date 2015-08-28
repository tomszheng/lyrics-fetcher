package com.kumasuke.fetcher;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 歌词获取器，可以获取歌曲信息、歌词和歌词来源
 */
public interface Fetcher {
    /**
     * 获取一个新的 {@code FetcherBuilder} 对象<br>
     * 仅为使用方便而存在的方法，效果等同于调用 {@link FetcherBuilder#builder()}。
     *
     * @return {@code FetcherBuilder} 对象
     */
    static FetcherBuilder builder() {
        return FetcherBuilder.builder();
    }

    /**
     * 获取歌曲基本信息，包括标题、歌手、作词和作曲等。
     *
     * @return 装有歌曲信息的 {@code Header} 容器
     * @see Header
     */
    Header getHeader();

    /**
     * 获取歌词文本，按行存放在 {@code Lyrics} 对象中，如果存在空行则该行对应字符串为空。
     *
     * @return 装有歌词文本的 {@code Lyrics} 对象
     * @see Lyrics
     */
    Lyrics getLyrics();

    /**
     * 获取含有注音的歌词文本，按行存放在 {@code Lyrics} 对象中，如果存在空行则该行对应字符串为空。<br>
     * 只有部分站点支持获取含注音的歌词文本，当站点不支持时将会返回 {@code null} 值。
     *
     * @return 装有歌词文本的 {@code Lyrics} 对象或 {@code null} 值
     * @see Lyrics
     * @deprecated 使用反射实现的临时方法，今后可能更改或者删除
     */
    @Deprecated
    default Lyrics getLyricsWithRuby() {
        // 底层实现可确保一定返回非 null 值
        Class<?> superClass = this.getClass().getSuperclass();

        // 获取存储 Parser 的字段列表
        List<Field> fetcherFields = Stream.of(superClass.getDeclaredFields())
                .collect(Collectors.toList());
        // 查找存储用于获取歌词的 Parser 的字段
        Predicate<Field> isParser = f -> f.getName().equals("parser");
        Predicate<Field> isLyricsParser = f -> f.getName().equals("lyricsParser");
        Field parserField = fetcherFields.stream()
                .filter(isParser.or(isLyricsParser))
                .findFirst().get();
        // 设置访问权限
        parserField.setAccessible(true);

        // 获取歌词 Parser 对象
        Object parserObject;
        try {
            parserObject = parserField.get(this);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Won't happen if coded right.");
        }

        // 查找是否存在名为 lyricsWithRuby 的方法
        Class<?> parserClass = parserObject.getClass();
        Method lyricsWithRubyMethod;
        try {
            lyricsWithRubyMethod = parserClass.getDeclaredMethod("lyricsWithRuby");
        } catch (NoSuchMethodException e) {
            // 不存在该方法，返回 null 值
            return null;
        }
        // 设置访问权限
        lyricsWithRubyMethod.setAccessible(true);

        // 调用该方法，并获取返回值
        Lyrics lyricsWithRuby;
        try {
            lyricsWithRuby = (Lyrics) lyricsWithRubyMethod.invoke(parserObject);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Won't happen if coded right.");
        }

        return lyricsWithRuby;
    }

    /**
     * 获取歌词来源地址。
     *
     * @return 歌词来源地址
     */
    String getSource();
}
