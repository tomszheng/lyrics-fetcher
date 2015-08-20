package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Lyrics;

/**
 * 歌词分析器，以便于 {@code AbstractSplitFetcher} 进行代码复用。<br>
 * 用于分析获取歌词文本。
 */
abstract class LyricsParser extends Parser {
    /**
     * 获取歌词文本。
     *
     * @return 歌词文本
     */
    abstract Lyrics lyrics();
}
