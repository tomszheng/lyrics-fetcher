package com.kumasuke.fetcher;

/**
 * 歌词获取器，可以获取歌曲信息、歌词和歌词来源
 */
public interface Fetcher {
    /**
     * 获取歌曲基本信息，包括标题、歌手、作词和作曲等。
     *
     * @return 装有歌曲信息的 {@code Header} 容器，key 值使用枚举型
     * @see Header
     */
    Header getHeader();

    /**
     * 获取歌词文本，按行存放在 {@code List} 容器中，如果存在空行则该行对应字符串为空。
     *
     * @return 装有歌词文本的 {@code List} 容器
     */
    Lyrics getLyrics();

    /**
     * 获取歌词来源地址。
     *
     * @return 歌词来源地址
     */
    String getSource();
}
