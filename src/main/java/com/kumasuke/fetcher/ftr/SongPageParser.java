package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;

/**
 * 歌词页分析器，以便于 {@code AbstractSplitFetcher} 进行代码复用。<br>
 * 用于分析获取歌词文本。
 */
abstract class SongPageParser extends Parser {
    /**
     * 获取歌曲基本信息。
     *
     * @return 歌曲基本信息
     */
    abstract Header header();

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    abstract String songPageUrl();
}
