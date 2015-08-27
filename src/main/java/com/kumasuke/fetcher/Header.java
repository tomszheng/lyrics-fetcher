package com.kumasuke.fetcher;

import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * 存放歌曲基本信息的容器，无法进行修改
 */
public interface Header extends Iterable<Header.Item> {
    /**
     * 获取歌曲标题。
     *
     * @return 歌曲标题
     */
    String getTitle();

    /**
     * 获取歌曲演唱者。
     *
     * @return 歌曲演唱者
     */
    Set<String> getArtist();

    /**
     * 获取歌曲作词者。<br>
     * 大多数网站支持获取该信息，没有该条目时将返回 {@code null}。
     *
     * @return 歌曲作词者
     */
    Set<String> getLyricist();

    /**
     * 获取歌曲作曲者。<br>
     * 大多数网站支持获取该信息，没有该条目时将返回 {@code null}。
     *
     * @return 歌曲作曲者
     */
    Set<String> getComposer();

    /**
     * 获取歌曲编曲者。<br>
     * 只有部分网站支持获取该信息，没有该条目时将返回 {@code null}。
     *
     * @return 歌曲编曲者
     */
    Set<String> getArranger();

    /**
     * 获取 {@code Iterator} 对象以便进行迭代。<br>
     * <p>
     * 将会按照标题、歌手、作词、作曲和编曲的顺序进行排列，如果不存在该项信息，则会跳过该信息。<br>
     * 如果对返回的 {@code Iterator} 对象进行修改操作将会抛出
     * {@code UnsupportedOperationException} 异常。</p>
     *
     * @return {@code Iterator} 对象
     */
    Iterator<Item> iterator();


    /**
     * 在每个 {@code Header.Item} 对象上执行指定操作。<br>
     * 该操作接受 2 个参数，分别对应 {@code Header.Item} 对象的名称和值。
     *
     * @param action 指定的操作
     */
    default void forEach(BiConsumer<String, Object> action) {
        requireNonNull(action);

        for (Item i : this)
            action.accept(i.getName(), i.getValue());
    }

    /**
     * 包含名称和值的歌曲基本信息条目，无法进行修改，用于迭代器
     */
    interface Item {
        /**
         * 获取条目名称。
         *
         * @return 条目名称
         */
        String getName();

        /**
         * 获取条目值。<br>
         * 如果该条目为标题，则为 {@code String} 类型；否则将为 {@code Set<String>} 类型。
         *
         * @return 条目值
         */
        Object getValue();

        /**
         * 返回包含条目信息的字符串的字符串。
         *
         * @return 包含条目信息的字符串，其格式为 <i>Name</i> = <i>Value</i>
         */
        String toString();
    }
}
