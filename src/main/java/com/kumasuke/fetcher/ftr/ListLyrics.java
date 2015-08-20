package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Lyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 存放歌词文本的容器。<br>
 * 使用 {@code List} 存放信息。
 */
class ListLyrics implements Lyrics {
    private final List<String> data;

    /**
     * 构造一个 {@code ListLyrics} 对象。
     */
    ListLyrics() {
        data = new ArrayList<>();
    }

    /**
     * 添加一行歌词文本。
     *
     * @param text 该行歌词文本
     */
    void addLine(String text) {
        data.add(text);
    }

    /**
     * 获取指定行数的歌词文本。
     *
     * @param index 行号，以 0 为始
     * @return 该行的歌词文本，若为空，则表示空行
     */
    @Override
    public String getLine(int index) {
        return data.get(index);
    }

    /**
     * 获取歌词文本总行数。
     *
     * @return 歌词总行数
     */
    @Override
    public int lineCount() {
        return data.size();
    }

    /**
     * 获取 {@code Iterator} 对象以便进行迭代。
     *
     * @return {@code Iterator} 对象
     */
    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }
}
