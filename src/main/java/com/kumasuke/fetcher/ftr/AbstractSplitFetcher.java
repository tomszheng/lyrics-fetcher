package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;

import static java.util.Objects.requireNonNull;

/**
 * 用于歌曲基本信息与歌词文本分开获取的站点的歌词获取器。
 *
 * @param <S> {@code SongPageParser} 对象，用于获取歌曲基本信息
 * @param <L> {@code LyricsParser} 对象，用于获取歌词文本
 */
abstract class AbstractSplitFetcher<S extends SongPageParser, L extends LyricsParser> implements Fetcher {
    S songPageParser;
    L lyricsParser;

    /**
     * 构造一个 {@code AbstractSplitFetcher} 对象，用于获取对应网站歌词相关信息。<br>
     * 本构造器主要用于检查传入参数。
     *
     * @param page      歌词页地址
     * @param userAgent {@code UserAgent} 字符串
     */
    AbstractSplitFetcher(String page, String userAgent) {
        requireNonNull(page, "The parameter 'page' should be non-null value.");
        requireNonNull(userAgent, "The parameter 'userAgent' should be non-null value.");

        if (page.isEmpty() || userAgent.isEmpty())
            throw new IllegalArgumentException("The parameters shouldn't be empty value.");
    }

    /**
     * 获取歌曲基本信息，包括标题、歌手、作词和作曲等。
     *
     * @return 装有歌曲信息的 {@code Map} 容器，该容器不可修改
     */
    @Override
    public final Header getHeader() {
        return songPageParser.header();
    }

    /**
     * 获取歌词文本。
     *
     * @return 装有歌词文本的 {@code List} 容器，该容器不可修改
     */
    @Override
    public final Lyrics getLyrics() {
        return lyricsParser.lyrics();
    }

    /**
     * 获取歌词来源地址。
     *
     * @return 歌词来源地址
     */
    @Override
    public final String getSource() {
        return songPageParser.songPageUrl();
    }
}
