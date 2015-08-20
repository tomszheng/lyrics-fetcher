package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.io.IOException;

/**
 * ���˥᥽�󥰤θ��~�ʤ餳���ˤ��ޤ����� (Jtw.Zaq.Ne.jp/AnimeSong) �ĸ�ʻ�ȡ����
 */
public class AnimeSongFetcher extends AbstractUnitedFetcher<AnimeSongUnitedParser> {
    /**
     * ����һ�� {@code AnimeSongFetcher} �������ڻ�ȡ��Ӧ��վ��������Ϣ��<br>
     * Ҳ��ʹ�� {@link FetcherBuilder FetcherBuilder} �����й��졣
     *
     * @param page      ���ҳ��ַ
     * @param userAgent {@code UserAgent} �ַ���
     * @throws IOException ҳ�����ӡ�����ʧ��
     */
    public AnimeSongFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        parser = new AnimeSongUnitedParser(page, userAgent);
    }
}
