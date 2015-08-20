package com.kumasuke.fetcher.util;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * URL 读取器，用于获取指定 URL 文档内所有文本内容<br>
 * 仅支持Http / Https 协议
 */
public class URLReader {
    private final HttpURLConnection urlConn;
    private boolean usePost = false;
    private Map<String, String> requestParameters;
    private String charsetName;

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

        URL docUrl = new URL(url);

        urlConn = (HttpURLConnection) docUrl.openConnection();
        urlConn.setConnectTimeout(3000);                        // 默认等待延迟
        charsetName = "UTF-8";                                  // 默认字符集
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

    /**
     * 设置 URL 文档的字符集。<br>
     * 如若未设置，则默认为 <code>UTF-8</code>。
     *
     * @param charsetName 字符集名称
     * @return {@code URLReader} 对象，便于链式编程
     * @throws UnsupportedCharsetException 不被支持的字符集名称
     */
    public URLReader charset(String charsetName) throws UnsupportedCharsetException {
        if (!Charset.isSupported(charsetName))
            throw new UnsupportedCharsetException("The given charset name is unavailable.");

        this.charsetName = charsetName;

        return this;
    }

    /**
     * 设置访问 URL 文档的等待延迟。<br>
     * 如若未设置，则默认为 3000 {@code ms}。
     *
     * @param timeout 延迟值（单位：{@code ms}）
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
        if (userAgent == null)
            throw new IllegalArgumentException("User-Agent cannot be null.");

        urlConn.setRequestProperty("User-Agent", userAgent);

        return this;
    }

    /**
     * 设置访问 URL 文档的的 Http / Https 请求中的 {@code Referer} 字段。
     *
     * @param referer {@code Referer} 字符串
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader referer(String referer) {
        if (referer == null)
            throw new IllegalArgumentException("Referer cannot be null.");

        urlConn.setRequestProperty("Referer", referer);

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
                e.printStackTrace();
            }
        } else {
            urlConn.setDoOutput(false);
            try {
                urlConn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    /**
     * 添加访问 URL 文档的 Http / Https 请求的表单参数，可重复调用添加多条数据。<br>
     * 只能够在使用 POST 方式时调用，GET 请求数据请直接添加至
     * {@link URLReader#connect(String) connect(String)} 的参数中。
     * <p>
     * 不会覆盖之前添加的数据，而是继续添加。</p>
     * <p>
     * 保证添加的数据按照添加顺序排列。</p>
     *
     * @param name  请求数据参数名
     * @param value 请求数据参数值
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader addRequestParameter(String name, String value) {
        if (!usePost)
            throw new IllegalStateException("Required using POST method.");

        if (requestParameters == null)
            requestParameters = new LinkedHashMap<>();

        requestParameters.put(name, value);

        return this;
    }

    /**
     * 设置访问 URL 文档的 Http / Https 请求的表单参数，之前添加的数据会被覆盖。<br>
     * 只能够在使用 POST 方式时调用，GET 请求数据请直接添加至
     * {@link URLReader#connect(String) connect(String)} 的参数中。
     *
     * @param parameters 请求参数 {@code Map} 对象
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader setRequestParameter(Map<String, String> parameters) {
        if (!usePost)
            throw new IllegalStateException("Required using POST method.");

        if (requestParameters == null)
            requestParameters = new LinkedHashMap<>();
        else
            requestParameters.clear();

        requestParameters.putAll(parameters);

        return this;
    }

    /**
     * 设置访问 URL 文档的的 Http / Https 请求中的 <code>X-Requested-With</code> 字段。
     *
     * @param xRequestedWith <code>X-Requested-With</code> 字符串
     * @return {@code URLReader} 对象，便于链式编程
     */
    public URLReader xRequestedWith(String xRequestedWith) {
        if (xRequestedWith == null)
            throw new IllegalArgumentException("X-Requested-With cannot be null.");

        urlConn.setRequestProperty("X-Requested-With", xRequestedWith);

        return this;
    }

    private void commitData() throws IOException {
        StringBuilder parameters = new StringBuilder();

        try {
            // 将表单参数编码连接成 URL 形式
            int i = 0;
            for (Map.Entry<String, String> e : requestParameters.entrySet()) {
                parameters.append(e.getKey())
                        .append("=")
                        .append(URLEncoder.encode(e.getValue(), "UTF-8"));

                if (++i != requestParameters.size())
                    parameters.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String encodedParameters = parameters.toString();

        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Content-Length", String.valueOf(encodedParameters.length()));

        // 提交表单数据
        OutputStream os = urlConn.getOutputStream();
        os.write(encodedParameters.getBytes());
    }

    /**
     * 读取 URL 文档内的内容至 {@code String} 对象中。<br>
     * 参数设置完成后，该方法仅可调用一次。
     *
     * @return 文档内容字符串
     * @throws IOException 读取文档失败
     */
    public String get() throws IOException {
        if (hasGot)
            throw new IllegalStateException("You have invoked the get method once.");

        if (usePost && requestParameters != null && !requestParameters.isEmpty())
            commitData();

        InputStreamReader isr = new InputStreamReader(urlConn.getInputStream(), charsetName);
        Reader in = new BufferedReader(isr);
        StringBuilder doc = new StringBuilder();

        int ch;
        while ((ch = in.read()) != -1)
            doc.append((char) ch);

        in.close();

        hasGot = true;

        return doc.toString();
    }
}
