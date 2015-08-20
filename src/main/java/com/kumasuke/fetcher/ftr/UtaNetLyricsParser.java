package com.kumasuke.fetcher.ftr;

import com.sun.org.apache.xerces.internal.impl.Constants;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.util.List;

/**
 * 歌ネット (Uta-Net.com) 的歌词分析器。<br>
 * 使用 {@code dom4j} 包获取页面信息。
 */
class UtaNetLyricsParser extends LyricsParser {
    private Element root;

    private ListLyrics lyrics;

    /**
     * 构造一个 {@code UtaNetLyricsParser} 对象。
     *
     * @param songPage {@code UtaNetSongPageParser} 对象<br>
     * @throws DocumentException XML 文档连接、解析失败
     */
    UtaNetLyricsParser(UtaNetSongPageParser songPage) throws DocumentException {
        SAXReader reader = new SAXReader();

        // 关闭解析外部 dtd，避免因无法访问的 dtd 地址引起异常
        try {
            reader.setFeature(Constants.XERCES_FEATURE_PREFIX
                    + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (SAXException e) {
            e.printStackTrace();
        }

        String url = songPage.lrcUrl();
        Document doc = reader.read(url);

        root = doc.getRootElement().element("g");
    }

    /**
     * 获取歌词文本。<br>
     *
     * @return 装有歌词文本的 {@code Lyrics} 容器
     * @implSpec 初次调用时，会初始化需要返回的对象，这将耗费一定的时间。
     */
    @Override
    ListLyrics lyrics() {
        if (lyrics == null) {
            lyrics = new ListLyrics();

            // 根据 dom4j 文档可以安全转换
            @SuppressWarnings("unchecked")
            List<Element> lrc = (List<Element>) root.elements("text");
            lrc.stream()
                    .map(Element::getText)
                    .map(String::trim)
                    .forEach(lyrics::addLine);
        }

        return lyrics;
    }
}