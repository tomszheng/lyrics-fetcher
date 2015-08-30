package com.kumasuke.sample;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.Header;
import com.kumasuke.fetcher.Lyrics;
import com.kumasuke.fetcher.util.FetcherBuilder;
import com.kumasuke.fetcher.util.Formatter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.kumasuke.fetcher.util.Tools.isNullOrEmpty;
import static java.util.Objects.nonNull;

/**
 * 批量歌词获取示例
 */
public class BatchFetcherSample {
    private static int RETRY_TIME_BOUND = 5;
    private static boolean ENABLE_RUBY_OUTPUT = false;
    private static boolean ENABLE_INDEX_NUMBER = false;

    private static String FILE_INPUT;
    private static String DIRECTORY_OUTPUT;
    private static String SITE = "*";

    public static void main(String[] args) {
        // 分析命令行参数并检查设置是否正确
        parseCmdArgs(args);
        checkOptions(FILE_INPUT, DIRECTORY_OUTPUT);

        // 记录下载开始时间
        long startTime = System.nanoTime();

        // 输出下载的歌词文件
        List<String> pages = loadPages();
        outputFiles(pages);
        // 记录下载结束时间并计算经过的时间
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        // 下载完成，显示提示和用时
        System.out.printf("歌词下载完成，用时 %.0f 秒！%n", elapsedTime / 1e9);
    }

    private static void outputFiles(List<String> pages) {
        // 检查并创建目标输出目录
        checkAndCreateDir(DIRECTORY_OUTPUT);
        if (ENABLE_RUBY_OUTPUT)
            checkAndCreateDir(DIRECTORY_OUTPUT + "\\Ruby");

        // 处理文件名序号格式
        String filenameFormat = "";
        if (ENABLE_INDEX_NUMBER)
            filenameFormat = "[%0" + getNumberBits(pages.size()) + "d] %s";

        // 获取构造器
        FetcherBuilder fetcherBuilder = Fetcher.builder();
        // 逐条获取歌词
        for (int i = 0; i < pages.size(); i++) {
            String page = pages.get(i);
            int retryTime = 0;
            boolean finished = false;

            // 判断是否完成或超出重试次数
            while (!finished && retryTime < RETRY_TIME_BOUND) {
                try {
                    // 进度显示
                    printCurProgress(i, pages.size(), retryTime);

                    // 构造获取器
                    Fetcher fetcher = fetcherBuilder
                            .site(SITE)
                            .page(page)
                            .build();
                    Header header = fetcher.getHeader();
                    Lyrics lyrics = fetcher.getLyrics();

                    // 获取并处理输出文件名
                    String filename = Formatter.headerToFormattedString(header, "%ar%「%ti%」.txt");
                    if (ENABLE_INDEX_NUMBER)
                        filename = String.format(filenameFormat, i + 1, filename);

                    // 输出歌词文件到指定目录
                    writeTo(DIRECTORY_OUTPUT + "\\" + filename, header, lyrics);
                    // 如果有需要，则输出带有注音的歌词文本
                    if (ENABLE_RUBY_OUTPUT) {
                        @SuppressWarnings("deprecation")
                        Lyrics lyricsWithRuby = fetcher.getLyricsWithRuby();

                        if (nonNull(lyricsWithRuby))
                            writeTo(DIRECTORY_OUTPUT + "\\Ruby\\" + filename, header, lyricsWithRuby);
                        else if (!SITE.equals("*"))
                            // 该站点不支持获取含有注音的歌词
                            ENABLE_RUBY_OUTPUT = false;
                    }

                    // 下载完成，结束循环
                    finished = true;
                } catch (IllegalArgumentException e) {
                    System.err.println("输入文件中的地址有误。无法解析，请检查！");
                    e.printStackTrace();
                    System.exit(1);
                } catch (Exception e) {
                    // 当处于连接超时或页面下载不全导致的空指针异常情况时，进行重试
                    if ((e instanceof IOException && e.getMessage().contains("timed out"))
                            || e instanceof NullPointerException) {
                        // 超出重试次数，显示错误
                        if (retryTime++ >= RETRY_TIME_BOUND)
                            System.err.printf("%n网络连接错误，第 %d 首下载失败！%n", i + 1);
                    } else {
                        // 无法处理的异常，输出异常信息并结束循环
                        finished = true;
                        System.err.printf("%n第 %d 首下载失败！%n", i + 1);
                        e.printStackTrace();
                    }
                } finally {
                    // 重置构造器
                    fetcherBuilder.reset();
                } // try-catch-finally
            } // while
        } // for
    }

    private static void parseCmdArgs(String[] args) {
        if (args.length == 0) {
            System.err.println("命令行参数为空，请先输入命令行参数！");
            System.exit(1);
        }

        // 提取命令行参数信息
        for (int i = 0; i < args.length; i++)
            switch (args[i]) {
                case "-rt":                 // 重试次数上限
                    try {
                        RETRY_TIME_BOUND = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("重试次数上限无法解析，请检查输入！");
                        System.exit(1);
                    }
                    break;
                case "-fi":                 // 输入参数文件，内容为按行存放的歌曲地址或代码
                    FILE_INPUT = args[++i];
                    break;
                case "-do":                 // 歌词输出目录
                    DIRECTORY_OUTPUT = args[++i];
                    break;
                case "-s":                  // 站点域名，可省略（即开启自动匹配）
                    SITE = args[++i];
                    break;
                case "-r":                  // 获取含有注音的歌词文本并存放在 Ruby 目录下（需站点支持）
                    ENABLE_RUBY_OUTPUT = true;
                    break;
                case "-i":                  // 开启后，输出的歌词文件名带有序号
                    ENABLE_INDEX_NUMBER = true;
                    break;
                default:
                    System.err.println("无法解析的命令行参数，请检查输入！");
                    System.exit(1);
            }
    }

    private static void checkOptions(String... options) {
        if (RETRY_TIME_BOUND < 0) {
            System.err.println("重试次数上限过低，请检查输入！");
            System.exit(1);
        }

        for (String s : options)
            if (isNullOrEmpty(s)) {
                System.err.println("命令行参数输入不完全，请检查输入！");
                System.exit(1);
            }
    }

    private static List<String> loadPages() {
        List<String> result = new ArrayList<>();
        Path file = Paths.get(FILE_INPUT);

        // 按行读取文件并添加的结果列表中
        try {
            Files.lines(file)
                    .map(String::trim)
                    .forEach(result::add);
        } catch (IOException e) {
            System.err.println("文件未找到或读取错误，请检查命令行参数输入！");
            System.exit(1);
        }

        return result;
    }

    private static void writeTo(String filePath, Header header, Lyrics lyrics) throws IOException {
        try (PrintWriter out = new PrintWriter(filePath, "UTF-8")) {
            out.println(Formatter.headerToText(header));
            out.println();
            out.println(Formatter.lyricsToText(lyrics));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void checkAndCreateDir(String directory) {
        File dir = new File(directory);
        if (!dir.exists())
            dir.mkdirs();
    }

    private static int getNumberBits(int number) {
        int bits = 1;
        while ((number /= 10) != 0)
            bits++;

        return bits;
    }

    private static void printCurProgress(int i, int allCount, int retryTime) {
        float progress = (float) i / allCount * 100;
        if (retryTime == 0)
            System.out.printf("(%.2f%%) 正在下载第 %d 首，共 %d 首...\r", progress, i + 1, allCount);
        else
            System.out.printf("(%.2f%%) 正在下载第 %d 首，共 %d 首... (第 %d 次重试)\r",
                    progress, i + 1, allCount, retryTime);
    }
}
