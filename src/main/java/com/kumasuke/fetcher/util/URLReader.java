package com.kumasuke.fetcher.util;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kumasuke.fetcher.util.Tools.*;
import static java.util.Objects.requireNonNull;

/**
 * URL 读取器，用于获取指定 URL 文档内所有文本内容<br>
 * 仅支持 Http / Https 协议
 *
 * @author Joash Lee (bearcomingx#gmail.com)
 * @version 1.1
 */
public class URLReader {
    private final HttpURLConnection urlConn;

    private String encodedRequestParameters;
    private String charsetName = "UTF-8";

    private Reader reader;

    private boolean usePost = false;
    private boolean isFinished = false;
    private boolean isSubmitted = false;

    /**
     * 构造一个 {@code URLReader} 对象。
     *
     * @param url URL 文档所在地址
     * @throws IOException           URL 连接失败
     * @throws MalformedURLException URL 格式错误
     */
    private URLReader(String url) throws IOException {
        if (!url.matches("https?://.*"))                                // 验证 url
            throw new MalformedURLException("Unsupported protocol or malformed url.");

        urlConn = (HttpURLConnection) new URL(url).openConnection();
        urlConn.setConnectTimeout(3000);                                // 设置默认等待延迟
    }

    /**
     * 获取一个 {@code URLReader} 对象，该对象连接到指定 url。<br>
     * 仅支持Http / Https 协议。
     *
     * @param url URL 文档所在地址
     * @return {@code URLReader} 对象
     * @throws IOException           URL 连接失败
     * @throws MalformedURLException URL 格式错误
     */
    public static URLReader connect(String url) throws IOException {
        return new URLReader(url);
    }

    private static String encodeAndFormatEntry(String name, String value) {
        try {
            // 按照 URL 格式对 value 编码并返回，不对 name 进行编码
            return String.format("%s=%s", name, URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
            throw new AssertionError("Won't happen if coded right.");
        }
    }

    private static String encodeAndFormatEntry(Map.Entry<String, String> entry) {
        return encodeAndFormatEntry(entry.getKey(), entry.getValue());
    }

    private void checkSubmitted() {
        if (isSubmitted)
            throw new IllegalStateException("The request has been submitted.");
    }

    /**
     * 设置 URL 文档的字符集。<br>
     * 如若未设置，则默认为 <code>UTF-8</code>。
     *
     * @param charsetName 字符集名称
     * @return {@code URLReader} 对象，便于链式编程
     * @throws UnsupportedCharsetException 字符集不支持
     * @throws IllegalStateException       请求已经提交
     */
    public URLReader charset(String charsetName) {
        checkSubmitted();

        if (!Charset.isSupported(charsetName))                          // 检查是否支持指定的字符集
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
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader timeout(int timeout) {
        checkSubmitted();

        urlConn.setConnectTimeout(timeout);

        return this;
    }

    /**
     * 设置访问 URL 文档的 Http / Https 请求中的 {@code User-Agent} 字段。
     *
     * @param userAgent <code>User-Agent</code> 字符串
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader userAgent(String userAgent) {
        checkSubmitted();

        urlConn.setRequestProperty("User-Agent", requireNonNull(userAgent, "User-Agent cannot be null."));

        return this;
    }

    /**
     * 设置访问 URL 文档的的 Http / Https 请求中的 {@code Referer} 字段。
     *
     * @param referer {@code Referer} 字符串
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader referer(String referer) {
        checkSubmitted();

        urlConn.setRequestProperty("Referer", requireNonNull(referer, "Referer cannot be null."));

        return this;
    }

    /**
     * 添加访问 URL 文档的的 Http / Https 请求中的 {@code Cookie} 字段。
     *
     * @param cookie {@code Cookie} 字符串
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader cookie(String cookie) {
        checkSubmitted();

        urlConn.addRequestProperty("Cookie", requireNonNull(cookie, "Cookie cannot be null."));

        return this;
    }

    /**
     * 添加一条或多条访问 URL 文档的的 Http / Https 请求中的 {@code Cookie} 字段。
     *
     * @param cookies {@code Cookie} 字符串 {@code Set} 对象
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader cookies(Set<String> cookies) {
        checkSubmitted();

        cookies.forEach(s -> urlConn.addRequestProperty("Cookie", requireNonNull(s, "Cookie cannot be null.")));

        return this;
    }

    /**
     * 设置访问 URL 文档的的 Http / Https 请求中的 {@code X-Requested-With} 字段。
     *
     * @param xRequestedWith <code>X-Requested-With</code> 字符串
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader xRequestedWith(String xRequestedWith) {
        checkSubmitted();

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
     * @throws IllegalStateException 请求已经提交
     * @deprecated 请使用 {@link URLReader#usePost() usePost()} 代替
     */
    @Deprecated
    public URLReader usePost(boolean usePost) {
        checkSubmitted();

        if (usePost) {
            // 使能向 OutputStream 发送请求
            urlConn.setDoOutput(true);
            // 设置为 POST 请求方式
            try {
                urlConn.setRequestMethod("POST");
            } catch (ProtocolException ignored) {
                throw new AssertionError("Won't happen if coded right.");
            }
        } else {
            // 恢复成默认值，即无法向 OutputStream 发送请求
            urlConn.setDoOutput(false);
            // 设置为默认的 GET 请求方式
            try {
                urlConn.setRequestMethod("GET");
            } catch (ProtocolException ignored) {
                throw new AssertionError("Won't happen if coded right.");
            }
        }

        this.usePost = usePost;

        return this;
    }

    /**
     * 设置访问 URL 文档的 Http / Https 请求使用 POST 方式。<br>
     * 默认不采用 POST 方式，即使用 GET 方式。
     *
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader usePost() {
        checkSubmitted();

        if (!usePost) {
            // 使能向 OutputStream 发送请求
            urlConn.setDoOutput(true);
            // 设置为 POST 请求方式
            try {
                urlConn.setRequestMethod("POST");
            } catch (ProtocolException ignored) {
                throw new AssertionError("Won't happen if coded right.");
            }
            usePost = true;
        }

        return this;
    }

    /**
     * 设置一条访问 URL 文档的 Http / Https 请求的属性，之前设置的同名数据会被覆盖。
     *
     * @param name  请求字段名称
     * @param value 请求字段值
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader requestHeader(String name, String value) {
        checkSubmitted();

        urlConn.setRequestProperty(name, requireNonNull(value, name + " cannot be null."));

        return this;
    }

    /**
     * 设置一条或多条访问 URL 文档的 Http / Https 请求的属性，之前设置的同名数据会被覆盖。
     *
     * @param headers 请求字段 {@code Map} 对象
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader requestHeaders(Map<String, String> headers) {
        checkSubmitted();

        headers.forEach((k, v) -> urlConn.setRequestProperty(k, requireNonNull(v, k + " cannot be null.")));

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
     * @throws IllegalStateException 未使用 POST 方式、请求已经提交
     */
    public URLReader requestFormDatum(String name, String value) {
        if (!usePost)
            throw new IllegalStateException("Required using POST method.");
        checkSubmitted();

        // 将表单参数编码连接成 URL 形式
        String encodedString = encodeAndFormatEntry(name, value);
        // 如果当前存在参数，则继续添加，否则直接赋值
        if (nonNullAndNonEmpty(encodedRequestParameters))
            encodedRequestParameters += String.format("&%s", encodedString);
        else
            encodedRequestParameters = encodedString;

        return this;
    }

    /**
     * 设置一条或多条访问 URL 文档的 Http / Https 请求的表单参数，之前设置的同名数据会被覆盖。<br>
     * 只能够在使用 POST 方式时调用，GET 请求数据请直接添加至
     * {@link URLReader#connect(String) connect(String)} 的参数中。
     *
     * @param formData 请求参数 {@code Map} 对象
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IllegalStateException 未使用 POST 方式、请求已经提交
     */
    public URLReader requestFormData(Map<String, String> formData) {
        if (!usePost)
            throw new IllegalStateException("Required using POST method.");
        checkSubmitted();

        // 将表单参数编码连接成 URL 形式
        encodedRequestParameters = formData.entrySet()
                .stream()
                .map(URLReader::encodeAndFormatEntry)
                .collect(Collectors.joining("&"));

        return this;
    }

    /**
     * 提交 URL 访问请求。<br>
     * 如果没有手动调用该方法，将会在获取数据时自动提交。
     *
     * @return {@code URLReader} 对象，便于链式编程
     * @throws IOException           提交请求失败
     * @throws IllegalStateException 请求已经提交
     */
    public URLReader submit() throws IOException {
        checkSubmitted();

        // 如果使用 POST 方式且存在表单数据
        if (usePost && nonNullAndNonEmpty(encodedRequestParameters)) {
            // 设置 POST 请求相关属性字段
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConn.setRequestProperty("Content-Length", String.valueOf(encodedRequestParameters.length()));

            // 提交请求表单数据
            OutputStream os = urlConn.getOutputStream();
            os.write(encodedRequestParameters.getBytes());
        }

        // 使用指定 Charset 获取输入流并存储 Reader
        InputStreamReader isr = new InputStreamReader(urlConn.getInputStream(), charsetName);
        reader = new BufferedReader(isr);
        isSubmitted = true;

        return this;
    }

    private void autoSubmit() throws IOException {
        // 如果当前尚未提交，则进行提交
        if (!isSubmitted)
            submit();
    }

    /**
     * 读取 URL 文档内的内容至 {@code String} 对象中。<br>
     * 如果尚未提交请求，调用该方法会自动调用 {@link URLReader#submit() submit()} 以提交请求。<br>
     * 调用该方法后，将无法再次调用该方法或 {@link URLReader#getReader() getReader()} 方法。
     *
     * @return 文档内容字符串
     * @throws IOException           提交请求失败、读取文档失败
     * @throws IllegalStateException 已调用过同类方法，无法再次调用
     */
    public String getText() throws IOException {
        StringBuilder doc = new StringBuilder();

        // 逐字符读取数据并添加到 StringBuilder
        try (Reader in = getReader()) {
            int c;
            while ((c = in.read()) != -1)
                doc.append((char) c);
        }

        return doc.toString();
    }

    /**
     * 获取用于读取 URL 文档的 {@code Reader} 对象，便于后续处理。<br>
     * 如果尚未提交请求，调用该方法会自动调用 {@link URLReader#submit() submit()} 以提交请求。<br>
     * 使用完毕后需将 {@code Reader} 对象关闭。<br>
     * 调用该方法后，将无法再次调用该方法或 {@link URLReader#getText() getText()} 方法。
     *
     * @return {@code Reader} 对象
     * @throws IOException           提交请求失败、读取文档失败
     * @throws IllegalStateException 已调用过同类方法，无法再次调用
     */
    public Reader getReader() throws IOException {
        if (isFinished)
            throw new IllegalStateException("You cannot invoke this method again.");

        // 可能抛出异常，先提交请求以获取 Reader，保证 isFinished 值的正确
        autoSubmit();
        isFinished = true;

        return reader;
    }


    /**
     * 获取访问的 URL 文档返回请求中的 {@code Set-Cookie} 字段。<br>
     * 如果尚未提交请求，调用该方法会自动调用 {@link URLReader#submit() submit()} 以提交请求。
     *
     * @return 返回的 {@code Cookie} 字符串
     * @throws IOException 提交请求失败
     */
    public Set<String> getSetCookies() throws IOException {
        autoSubmit();

        Set<String> result = new HashSet<>();

        // 逐条遍历请求字段，查找 Set-Cookie 对应值并存储在 Set 中
        String key;
        for (int i = 1; (key = urlConn.getHeaderFieldKey(i)) != null; i++)
            if (key.equals("Set-Cookie")) {
                String value = urlConn.getHeaderField(i);
                result.add(value);
            }

        return result;
    }

    /**
     * 获取访问的 URL 文档返回请求中的指定字段。<br>
     * 如果有多条同名的字段，则会返回最后一条。<br>
     * 如果尚未提交请求，调用该方法会自动调用 {@link URLReader#submit() submit()} 以提交请求。
     *
     * @param name 指定字段名称
     * @return 指定字段值
     * @throws IOException 提交请求失败
     */
    public String getHeader(String name) throws IOException {
        autoSubmit();

        return urlConn.getHeaderField(name);
    }

    /**
     * 获取一条或多条访问的 URL 文档返回请求中的指定字段。<br>
     * 如果有多条同名的字段，则会返回最后一条。<br>
     * 如果尚未提交请求，调用该方法会自动调用 {@link URLReader#submit() submit()} 以提交请求。
     *
     * @param firstName  第一个指定字段名称
     * @param otherNames 其他指定字段名称
     * @return 指定字段 {@code Map} 对象
     * @throws IOException 提交请求失败
     */
    public Map<String, String> getHeaders(String firstName, String... otherNames) throws IOException {
        autoSubmit();

        String[] names = Arrays.copyOf(otherNames, 1 + otherNames.length);
        names[otherNames.length] = firstName;

        return Stream.of(names)
                .map(s -> p(s, urlConn.getHeaderField(s)))
                .collect(Collectors.toMap(P::getKey, P::getValue));
    }
}
