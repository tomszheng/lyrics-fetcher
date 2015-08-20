package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;

/**
 * 统合分析器，以便于 {@code AbstractUnitedFetcher} 进行代码复用。<br>
 * 用于分析获取歌曲信息和歌词文本。
 */
abstract class UnitedParser extends Parser {
    /**
     * 获取歌曲基本信息。
     *
     * @return 歌曲基本信息
     */
    abstract Header header();

    /**
     * 获取歌词文本。
     *
     * @return 歌词文本
     */
    abstract Lyrics lyrics();

    /**
     * 获取歌词页地址。
     *
     * @return 歌词页地址
     */
    abstract String songPageUrl();
}
