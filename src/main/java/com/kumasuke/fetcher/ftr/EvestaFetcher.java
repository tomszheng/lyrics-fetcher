package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;

import java.io.IOException;

/**
 * ���٥��� (Evesta.jp) �ĸ�ʻ�ȡ����
 */
public class EvestaFetcher extends AbstractUnitedFetcher<EvestaUnitedParser> {
    /**
     * ����һ�� {@code EvestaFetcher} �������ڻ�ȡ��Ӧ��վ��������Ϣ��<br>
     * Ҳ��ʹ�� {@link FetcherBuilder FetcherBuilder} �����й��졣
     *
     * @param page      ���ҳ��ַ
     * @param userAgent {@code UserAgent} �ַ���
     * @throws IOException ҳ�����ӡ�����ʧ��
     */
    public EvestaFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        parser = new EvestaUnitedParser(page, userAgent);
    }
}
