package com.kumasuke.fetcher.util;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kumasuke.fetcher.util.Tools.nonNullAndNonEmpty;
import static java.util.Objects.requireNonNull;

/**
 * URL 读取器，用于获取指定 URL 文档内所有文本内容<br>
 * 仅支持Http / Https 协议
 */
public class URLReader {
    private final HttpURLConnection urlConn;

    private String encodedRequestParameter;
    private String charsetName = "UTF-8";

    private boolean usePost = false;
    private boolean hasGot = false;

    /**
     * 构造一个 {@code URLReader} 对象。
     *
     * @param url URL 文档所在地址
     * @throws IOException URL 格式错误或连接失败
     */
    private URLReader(String url) throws IOException {
        // 验证 url
        if (!url.matches("https?://.*"))
            throw new MalformedURLException("Unsupported protocol or malformed url.");

        urlConn = (HttpURLConnection) new URL(url).openConnection();
        urlConn.setConnectTimeout(3000);                                // 设置默认等待延迟
    }

    /**
     * 连接到指定 url，并获取一个 {@code URLReader} 对象。<br>
     * 仅支持Http / Https 协议。
     *
     * @param url URL 文档所在地址
     * @return {@code URLReader} 对象
     * @throws IOException URL 格式错误或连接失败
     */
    public static URLReader connect(String url) throws IOException {
        return new URLReader(url);
    }

    private static String encodeAndFormatEntry(Map.Entry<String, String> entry) {
        return encodeAndFormatEntry(entry.getKey(), entry.getValue());
    }

    private static String encodeAndFormatEntry(String name, String value) {
        try {
            return String.format("%s=%s", name, URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Won't happen if coded right.");
        }
    }

    /**
     * 设置 URL 文档的字符集。<br>
     * 如若未设置，则默认为 <code>UTF-8</code>。
     *
     * @param charsetName 字符集名称
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader charset(String charsetName) {
        if (!Charset.isSupported(charsetName))
            throw new UnsupportedCharsetException("The given charset is unsupported.");

        this.charsetName = charsetName;

        return this;
    }

    /**
     * 设置访问 URL 文档的等待延迟。<br>
     * 如若未设置，则默认为 3000 {@code ms}。
     *
     * @param timeout 延迟值（单位：{@code ms}），设置 0 代表不限制
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader timeout(int timeout) {
        urlConn.setConnectTimeout(timeout);

        return this;
    }

    /**
     * 设置访问 URL 文档的 Http / Https 请求中的 <code>User-Agent</code> 字段。
     *
     * @param userAgent <code>User-Agent</code> 字符串
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader userAgent(String userAgent) {
        urlConn.setRequestProperty("User-Agent", requireNonNull(userAgent, "User-Agent cannot be null."));

        return this;
    }

    /**
     * 设置访问 URL 文档的的 Http / Https 请求中的 {@code Referer} 字段。
     *
     * @param referer {@code Referer} 字符串
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader referer(String referer) {
        urlConn.setRequestProperty("Referer", requireNonNull(referer, "Referer cannot be null."));

        return this;
    }

    /**
     * 设置访问 URL 文档的的 Http / Https 请求中的 <code>X-Requested-With</code> 字段。
     *
     * @param xRequestedWith <code>X-Requested-With</code> 字符串
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader xRequestedWith(String xRequestedWith) {
        urlConn.setRequestProperty("X-Requested-With",
                requireNonNull(xRequestedWith, "X-Requested-With cannot be null."));

        return this;
    }

    /**
     * 设置访问 URL 文档的 Http / Https 请求是否使用 POST 方式。<br>
     * 默认不采用 POST 方式，即使用 GET 方式。
     *
     * @param usePost 是否使用 POST 方式
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader usePost(boolean usePost) {
        this.usePost = usePost;

        if (usePost) {
            urlConn.setDoOutput(true);
            try {
                urlConn.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new AssertionError("Won't happen if coded right.");
            }
        } else {
            urlConn.setDoOutput(false);
            try {
                urlConn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                throw new AssertionError("Won't happen if coded right.");
            }
        }

        return this;
    }

    /**
     * 设置访问 URL 文档的 Http / Https 请求使用 POST 方式。<br>
     * 默认不采用 POST 方式，即使用 GET 方式。<br>
     * 效果等同于调用 {@code URLReader.usePost(true)}。
     *
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader usePost() {
        return usePost(true);
    }

    /**
     * 设置一条访问 URL 文档的 Http / Https 请求的属性，之前设置的同名数据会被覆盖。
     *
     * @param name  请求属性名称
     * @param value 请求属性值
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader requestHeader(String name, String value) {
        urlConn.setRequestProperty(name, requireNonNull(value, name + " cannot be null."));

        return this;
    }

    /**
     * 设置一条或多条访问 URL 文档的 Http / Https 请求的属性，之前设置的同名数据会被覆盖。
     *
     * @param properties 请求属性 {@code Map} 对象
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader requestHeader(Map<String, String> properties) {
        properties.forEach((k, v) -> urlConn.setRequestProperty(k, requireNonNull(v, k + " cannot be null.")));

        return this;
    }

    /**
     * 设置一条访问 URL 文档的 Http / Https 请求的表单参数，之前设置的同名数据会被覆盖。<br>
     * 只能够在使用 POST 方式时调用，GET 请求数据请直接添加至
     * {@link URLReader#connect(String) connect(String)} 的参数中。
     *
     * @param name  请求属性名称
     * @param value 请求属性值
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader requestFormData(String name, String value) {
        if (!usePost)
            throw new IllegalStateException("Required using POST method.");

        // 将表单参数编码连接成 URL 形式
        String encodedString = encodeAndFormatEntry(name, value);
        if (nonNullAndNonEmpty(encodedRequestParameter))
            encodedRequestParameter += String.format("&%s", encodedString);
        else
            encodedRequestParameter = encodedString;

        return this;
    }

    /**
     * 设置一条或多条访问 URL 文档的 Http / Https 请求的表单参数，之前设置的同名数据会被覆盖。<br>
     * 只能够在使用 POST 方式时调用，GET 请求数据请直接添加至
     * {@link URLReader#connect(String) connect(String)} 的参数中。
     *
     * @param parameters 请求参数 {@code Map} 对象
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader requestFormData(Map<String, String> parameters) {
        if (!usePost)
            throw new IllegalStateException("Required using POST method.");

        // 将表单参数编码连接成 URL 形式
        encodedRequestParameter = parameters.entrySet()
                .stream()
                .map(URLReader::encodeAndFormatEntry)
                .collect(Collectors.joining("&"));

        return this;
    }

    private void checkHasGot() {
        if (hasGot)
            throw new IllegalStateException("You have invoked the get method once.");
    }

    private Reader prepareReader() throws IOException {
        // 如果使用 POST 方式且存在表单数据
        if (usePost && nonNullAndNonEmpty(encodedRequestParameter)) {
            // 设置 POST 请求相关属性字段
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConn.setRequestProperty("Content-Length", String.valueOf(encodedRequestParameter.length()));

            // 提交表单数据
            OutputStream os = urlConn.getOutputStream();
            os.write(encodedRequestParameter.getBytes());
        }

        // 使用指定 Charset 读取输入流并返回
        InputStreamReader isr = new InputStreamReader(urlConn.getInputStream(), charsetName);
        return new BufferedReader(isr);
    }

    /**
     * 读取 URL 文档内的内容至 {@code String} 对象中。<br>
     * 参数设置完成后，该类方法仅可调用一次。
     *
     * @return 文档内容字符串
     * @throws IOException 读取文档失败
     */
    public String getText() throws IOException {
        checkHasGot();

        StringBuilder doc = new StringBuilder();
        try (Reader in = prepareReader()) {
            int ch;
            while ((ch = in.read()) != -1)
                doc.append((char) ch);
        }
        hasGot = true;

        return doc.toString();
    }

    /**
     * 获取用于读取 URL 文档的 {@code Reader} 对象，便于后续处理。<br>
     * 使用完毕后需将 {@code Reader} 对象关闭。<br>
     * 参数设置完成后，该类方法仅可调用一次。
     *
     * @return {@code Reader} 对象
     * @throws IOException 读取文档失败
     */
    public Reader getReader() throws IOException {
        checkHasGot();

        // 可能抛出异常，先获取 Reader 以保证 hasGot 的值的正确
        Reader in = prepareReader();
        hasGot = true;

        return in;
    }
}
