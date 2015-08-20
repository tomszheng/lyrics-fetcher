package com.kumasuke.fetcher;

import java.util.Iterator;

/**
 * 存放歌词文本的容器，无法进行修改
 */
public interface Lyrics extends Iterable<String> {
    /**
     * 获取指定行数的歌词文本。
     *
     * @param index 行号，以 0 为始
     * @return 该行的歌词文本，若为空，则表示空行
     */
    String getLine(int index);

    /**
     * 获取歌词文本总行数。
     *
     * @return 歌词总行数
     */
    int lineCount();

    /**
     * 获取 {@code Iterator} 对象以便进行迭代。<br>
     * 如果对返回的 {@code Iterator} 对象进行修改操作将会抛出
     * {@code UnsupportedOperationException} 异常。
     *
     * @return {@code Iterator} 对象
     */
    Iterator<String> iterator();
}
