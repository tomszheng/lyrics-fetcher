package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.io.IOException;

/**
 * アニメソングの梧�~ならここにおまかせ�� (Jtw.Zaq.Ne.jp/AnimeSong) 議梧簡資函匂。
 */
public class AnimeSongFetcher extends AbstractUnitedFetcher<AnimeSongUnitedParser> {
    /**
     * 更夛匯倖 {@code AnimeSongFetcher} 斤�鵤�喘噐資函斤哘利嫋梧簡�犢慚渡◆�<br>
     * 匆辛聞喘 {@link FetcherBuilder FetcherBuilder} 栖序佩更夛。
     *
     * @param page      梧簡匈仇峽
     * @param userAgent {@code UserAgent} 忖憲堪
     * @throws IOException 匈中銭俊、侃尖払移
     */
    public AnimeSongFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        parser = new AnimeSongUnitedParser(page, userAgent);
    }
}
