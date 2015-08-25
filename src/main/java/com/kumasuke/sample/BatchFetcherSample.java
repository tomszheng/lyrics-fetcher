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
        // 分析命令行参数并检查是否完全
        parseCmdArgs(args);
        checkArgs(FILE_INPUT, DIRECTORY_OUTPUT, SITE);

        // 输出下载的歌词文件
        List<String> pages = loadPages();
        outputFile(pages);

        // 下载完成，显示提示
        System.out.println("(100.00%) 歌词下载完成！");
    }

    private static void outputFile(List<String> pages) {
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

        // 逐条获取
        for (int i = 0; i < pages.size(); i++) {
            String page = pages.get(i);
            int retryTime = 0;
            boolean finished = false;

            // 判断是否完成或超出重试次数
            while (!finished && retryTime < RETRY_TIME_BOUND) {
                try {
                    // 进度计算与显示
                    float progress = (float) i / pages.size() * 100;
                    if (retryTime == 0)
                        System.out.printf("(%.2f%%) 正在下载第 %d 首，共 %d 首...\r", progress, i + 1, pages.size());
                    else
                        System.out.printf("(%.2f%%) 正在重试第 %d 首，共 %d 首...\r", progress, i + 1, pages.size());

                    // 构造获取器
                    Fetcher fetcher = fetcherBuilder.site(SITE)
                            .page(page)
                            .build();
                    Header header = fetcher.getHeader();
                    Lyrics lyrics = fetcher.getLyrics();

                    // 获取并处理输出文件名
                    String filename = Formatter.headerToFormattedString(header, "%ar%「%ti%」.txt");
                    if (ENABLE_INDEX_NUMBER)
                        filename = String.format(filenameFormat, i + 1, filename);
                    // 输出文件
                    printOutTo(DIRECTORY_OUTPUT + "\\" + filename, header, lyrics);

                    // 输出带有注音的歌词文本
                    if (ENABLE_RUBY_OUTPUT) {
                        @SuppressWarnings("deprecation")
                        Lyrics lyricsWithRuby = fetcher.getLyricsWithRuby();

                        if (lyricsWithRuby != null)
                            printOutTo(DIRECTORY_OUTPUT + "\\Ruby\\" + filename, header, lyricsWithRuby);
                        else if (!SITE.equals("*"))
                            // 该站点不支持获取含有注音的歌词
                            ENABLE_RUBY_OUTPUT = false;
                    }

                    // 下载完成，结束循环
                    finished = true;
                } catch (IOException e) {
                    // 连接超时，进行重试
                    if (e.getMessage().contains("timed out")) {
                        // 超出重试次数，显示错误
                        if (retryTime++ >= RETRY_TIME_BOUND)
                            System.err.printf("网络连接错误，第 %d 首下载失败！%n", i + 1);
                    } else {
                        // 无法处理的异常，输出异常信息并结束循环
                        finished = true;
                        System.err.printf("第 %d 首下载失败！%n", i + 1);
                        e.printStackTrace();
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("输入文件中的地址有误。无法解析，请检查！");
                    e.printStackTrace();
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
                case "-rt":
                    try {
                        RETRY_TIME_BOUND = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("无法解析的命令行参数，请检查输入！");
                        System.exit(1);
                    }
                    break;
                case "-fi":
                    FILE_INPUT = args[++i];
                    break;
                case "-fo":
                    DIRECTORY_OUTPUT = args[++i];
                    break;
                case "-s":
                    SITE = args[++i];
                    break;
                case "-r":
                    ENABLE_RUBY_OUTPUT = true;
                    break;
                case "-i":
                    ENABLE_INDEX_NUMBER = true;
                    break;
                default:
                    System.err.println("无法解析的命令行参数，请检查输入！");
                    System.exit(1);
            }
    }

    private static void checkArgs(String... args) {
        for (String s : args)
            if (s == null || s.isEmpty()) {
                System.err.println("命令行参数输入不全，请检查输入！");
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

    private static void printOutTo(String filePath, Header header, Lyrics lyrics) throws IOException {
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
}
