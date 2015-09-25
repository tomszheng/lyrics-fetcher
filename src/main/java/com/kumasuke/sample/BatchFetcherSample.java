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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.kumasuke.fetcher.util.Tools.isNullOrEmpty;
import static java.lang.Math.min;
import static java.util.Objects.nonNull;

/**
 * 批量歌词获取示例（多线程）
 */
public class BatchFetcherSample {
    private static int retryTimeBound = 5;
    private static int maximumThreadNumber = 5;
    private static boolean enableRubyOutput = false;
    private static boolean enableIndexNumber = false;

    private static String fileInput;
    private static String directoryOutput;
    private static String site = "*";
    private static String fileNameFormat;

    public static void main(String[] args) {
        // 分析命令行参数并检查设置是否正确
        parseCmdArgs(args);
        checkOptions(fileInput, directoryOutput);

        // 记录下载开始时间
        long startTime = System.nanoTime();

        // 输出下载的歌词文件
        List<String> pages = loadPages();
        outputFiles(pages);

        // 记录下载结束时间并计算经过的时间
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        // 下载完成，显示提示和用时
        System.out.printf("[%s] 歌词下载完成，用时 %.0f 秒！%n", new Date(), elapsedTime / 1e9);
    }

    private static void outputFiles(List<String> pages) {
        // 检查并创建目标输出目录
        checkAndCreateDir(directoryOutput);
        if (enableRubyOutput)
            checkAndCreateDir(directoryOutput + "\\Ruby");

        // 处理文件名序号格式
        if (enableIndexNumber)
            fileNameFormat = "[%0" + getNumberBits(pages.size()) + "d] %s";

        System.out.printf("[%s] 开始下载共 %d 首歌词...%n", new Date(), pages.size());
        // 计算所需线程的实际个数
        final int nThreads = min(pages.size(), maximumThreadNumber);
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        // 添加任务至 ExecutorService 中
        for (int i = 0; i < pages.size(); i++) {
            DownloadTask task = new DownloadTask(i + 1, pages.get(i));
            executor.execute(task);
        }

        // 结束 ExecutorService，使其不能再添加新任务，
        // 并将在所有任务执行完毕后关闭所有新建的线程
        executor.shutdown();
        try {
            // 等待所有任务执行完毕
            while (!executor.isTerminated())
                executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // 出现中断异常，结束程序
            System.err.printf("[%s] 出现致命性错误，即将退出！%n", new Date());
            System.exit(1);
        }
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
                        retryTimeBound = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("重试次数上限无法解析，请检查输入！");
                        System.exit(1);
                    }
                    break;
                case "-fi":                 // 输入参数文件，内容为按行存放的歌曲地址或代码
                    fileInput = args[++i];
                    break;
                case "-do":                 // 歌词输出目录
                    directoryOutput = args[++i];
                    break;
                case "-s":                  // 站点域名，可省略（即开启自动匹配）
                    site = args[++i];
                    break;
                case "-r":                  // 获取含有注音的歌词文本并存放在 Ruby 目录下（需站点支持）
                    enableRubyOutput = true;
                    break;
                case "-i":                  // 开启后，输出的歌词文件名带有序号
                    enableIndexNumber = true;
                    break;
                case "-th":                 // 最大同时下载线程数
                    try {
                        maximumThreadNumber = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("最大同时下载线程数无法解析，请检查输入！");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("无法解析的命令行参数，请检查输入！");
                    System.exit(1);
            }
    }

    private static void checkOptions(String... options) {
        if (retryTimeBound < 0) {
            System.err.println("重试次数上限过低，请检查输入！");
            System.exit(1);
        }

        if (maximumThreadNumber < 1 || maximumThreadNumber > 35) {
            System.err.println("最大同时下载线程数过小或过大，请检查输入！");
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
        Path file = Paths.get(fileInput);

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

    private static void printCurProgress(int i, int retryTime) {
        Date date = new Date();
        if (retryTime == 0)
            System.out.printf("[%s] 正在下载第 %d 首...%n", date, i);
        else
            System.out.printf("[%s] 正在下载第 %d 首... (第 %d 次重试)%n", date, i, retryTime);
    }

    /**
     * 下载任务类，用于多线程。<br>
     * 失败时，将在指定次数内进行重试。
     */
    public static class DownloadTask implements Runnable {
        private final FetcherBuilder fetcherBuilder;
        private final int index;
        private final String page;

        /**
         * 创建一个下载任务。
         *
         * @param index 该任务的下载序号
         * @param page  改任务的页面地址
         */
        public DownloadTask(int index, String page) {
            fetcherBuilder = FetcherBuilder.newBuilder();
            this.index = index;
            this.page = page;
        }

        @Override
        public void run() {
            boolean finished = false;
            int retryTime = 0;

            // 判断是否完成或超出重试次数
            while (!finished && retryTime <= retryTimeBound) {
                try {
                    // 进度显示
                    printCurProgress(index, retryTime);

                    // 构造获取器
                    Fetcher fetcher = fetcherBuilder
                            .site(site)
                            .page(page)
                            .build();
                    Header header = fetcher.getHeader();
                    Lyrics lyrics = fetcher.getLyrics();

                    // 获取并处理输出文件名
                    String filename = Formatter.headerToFormattedString(header, "%ar%「%ti%」.txt");
                    if (enableIndexNumber)
                        filename = String.format(fileNameFormat, index, filename);

                    // 输出歌词文件到指定目录
                    writeTo(directoryOutput + "\\" + filename, header, lyrics);
                    // 如果有需要，则输出带有注音的歌词文本
                    if (enableRubyOutput) {
                        @SuppressWarnings("deprecation")
                        Lyrics lyricsWithRuby = fetcher.getLyricsWithRuby();

                        if (nonNull(lyricsWithRuby))
                            writeTo(directoryOutput + "\\Ruby\\" + filename, header, lyricsWithRuby);
                        else if (!site.equals("*"))
                            // 该站点不支持获取含有注音的歌词
                            enableRubyOutput = false;
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
                        if (retryTime++ >= retryTimeBound)
                            System.err.printf("[%s] 网络连接错误，第 %d 首下载失败！%n", new Date(), index);
                    } else {
                        // 无法处理的异常，输出异常信息并结束循环
                        finished = true;
                        System.err.printf("[%s] 第 %d 首下载失败！%n", new Date(), index);
                        e.printStackTrace();
                    }
                } finally {
                    // 重置构造器
                    fetcherBuilder.reset();
                } // try-catch-finally
            } // while
        }
    }
}
