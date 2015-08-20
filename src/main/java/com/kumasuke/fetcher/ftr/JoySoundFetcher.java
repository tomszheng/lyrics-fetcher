package com.kumasuke.fetcher.ftr;

import com.kumasuke.fetcher.util.FetcherBuilder;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * JoySound (JoySound.com) 的歌词获取器。
 */
public class JoySoundFetcher extends AbstractUnitedFetcher<JoySoundUnitedParser> {
    /**
     * 构造一个 {@code JoySoundFetcher} 对象，用于获取对应网站歌词相关信息。<br>
     * 也可使用 {@link FetcherBuilder FetcherBuilder} 来进行构造。
     *
     * @param page      歌词页地址或歌曲代码
     * @param userAgent {@code UserAgent} 字符串
     * @throws IOException 页面连接、处理失败
     */
    public JoySoundFetcher(String page, String userAgent) throws IOException {
        super(page, userAgent);

        try {
            parser = new JoySoundUnitedParser(page, userAgent);
        } catch (ParseException e) {
            // 包装 ParseException 为 IOException
            throw new IOException(e);
        }
    }
}
