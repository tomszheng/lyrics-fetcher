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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kumasuke.fetcher.util.Tools.isNullOrEmpty;
import static com.kumasuke.sample.MultithreadedBatchFetcherSample.Pair.makePair;
import static java.util.Objects.nonNull;

/**
 * 批量歌词获取示例（多线程）
 */
public class MultithreadedBatchFetcherSample {
    private static int RETRY_TIME_BOUND = 5;
    private static int THREAD_NUMBER = 5;
    private static boolean ENABLE_RUBY_OUTPUT = false;
    private static boolean ENABLE_INDEX_NUMBER = false;

    private static String FILE_INPUT;
    private static String DIRECTORY_OUTPUT;
    private static String SITE = "*";

    private static String FILE_NAME_FORMAT;
    private static int PAGES_SIZE;

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
        // 保存页面总数，供其它方法使用
        PAGES_SIZE = pages.size();

        // 检查并创建目标输出目录
        checkAndCreateDir(DIRECTORY_OUTPUT);
        if (ENABLE_RUBY_OUTPUT)
            checkAndCreateDir(DIRECTORY_OUTPUT + "\\Ruby");

        // 处理文件名序号格式
        if (ENABLE_INDEX_NUMBER)
            FILE_NAME_FORMAT = "[%0" + getNumberBits(pages.size()) + "d] %s";

        // 创建下载参数类，以便其他线程同步地获取参数而不失准确性
        DownloadParametersProvider parametersProvider = new DownloadParametersProvider(pages);
        // 用于记录线程是否全部退出的 CountDownLatch 类对象
        CountDownLatch doneSignal = new CountDownLatch(THREAD_NUMBER);
        // 建立线程池，并创建指定数目的线程并执行
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);
        for (int i = 0; i < THREAD_NUMBER; i++) {
            DownloadTask task = new DownloadTask(parametersProvider, i, doneSignal);
            executor.execute(task);
        }

        // 等待全部线程执行完毕
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            System.err.println("出现未知错误，请稍后重试！");
            e.printStackTrace();
            System.exit(1);
        }

        // 结束线程池
        executor.shutdown();
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
                case "-th":                 // 最大同时下载线程数
                    try {
                        THREAD_NUMBER = Integer.parseInt(args[++i]);
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
        if (RETRY_TIME_BOUND < 0) {
            System.err.println("重试次数上限过低，请检查输入！");
            System.exit(1);
        }

        if (THREAD_NUMBER < 1 || THREAD_NUMBER > 35) {
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

    private static void printCurProgress(int threadId, int i, int retryTime) {
        if (retryTime == 0)
            System.out.printf("[#%02d] 正在下载第 %d 首，共 %d 首...%n", threadId, i, PAGES_SIZE);
        else
            System.out.printf("[#%02d] 正在下载第 %d 首，共 %d 首... (第 %d 次重试)%n", threadId, i, PAGES_SIZE, retryTime);
    }

    /**
     * 下载参数类，是线程安全的。
     */
    public static class DownloadParametersProvider {
        private final AtomicInteger index = new AtomicInteger();
        private final Iterator<String> pagesIterator;
        private final Object lock = new Object();

        /**
         * 构造一个下载参数类。
         *
         * @param pages 需要下载的页面地址列表
         */
        public DownloadParametersProvider(List<String> pages) {
            this.pagesIterator = pages.iterator();
        }

        /**
         * 获取下一个计数和页面地址参数。
         *
         * @return 装有所需数据的数据值对
         */
        public Pair<Integer, String> getNext() {
            synchronized (lock) {
                if (pagesIterator.hasNext())
                    return makePair(index.incrementAndGet(), pagesIterator.next());
                else
                    return null;
            }
        }
    }

    /**
     * 下载任务类，用于多线程。<br>
     * 失败时，将在指定次数内进行重试。
     */
    public static class DownloadTask implements Runnable {
        private final DownloadParametersProvider parametersProvider;
        private final FetcherBuilder fetcherBuilder;
        private final int threadId;
        private final CountDownLatch doneSignal;

        /**
         * 创建一个下载任务。
         *
         * @param parametersProvider 下载参数提供器
         * @param threadId           线程编号
         * @param doneSignal         用于记录线程操作是否完成的 {@code CountDownLatch} 对象
         */
        public DownloadTask(DownloadParametersProvider parametersProvider, int threadId, CountDownLatch doneSignal) {
            this.parametersProvider = parametersProvider;
            fetcherBuilder = FetcherBuilder.builder();
            this.threadId = threadId;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            // 获取初始参数
            Pair<Integer, String> parameters = parametersProvider.getNext();

            // 当参数不为空时
            while (nonNull(parameters)) {
                boolean finished = false;
                int retryTime = 0;

                // 提取计数和页面参数
                int index = parameters.getFirst();
                String page = parameters.getSecond();

                // 判断是否完成或超出重试次数
                while (!finished && retryTime < RETRY_TIME_BOUND) {
                    try {
                        // 进度显示
                        printCurProgress(threadId, index, retryTime);

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
                            filename = String.format(FILE_NAME_FORMAT, index, filename);

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
                                System.err.printf("[#%02d] 网络连接错误，第 %d 首下载失败！%n", threadId, index);
                        } else {
                            // 无法处理的异常，输出异常信息并结束循环
                            finished = true;
                            System.err.printf("[#%02d] 第 %d 首下载失败！%n", threadId, index);
                            e.printStackTrace();
                        }
                    } finally {
                        // 重置构造器
                        fetcherBuilder.reset();
                    } // try-catch-finally
                } // while

                // 当前任务完成，获取下一组参数
                parameters = parametersProvider.getNext();
            }

            // 提示线程结束
            doneSignal.countDown();
        }
    }

    /**
     * 数据值对，如果参数均是不可修改的引用类型或者原始类型，则该类是线程安全的。
     *
     * @param <T> 第一个值的类型
     * @param <U> 第二个值得类型
     */
    public static class Pair<T, U> {
        private final T first;
        private final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        /**
         * 构造一个数据值对。
         *
         * @param first  第一个值
         * @param second 第二个值
         * @param <T>    第一个值的类型
         * @param <U>    第二个值得类型
         * @return 构造好的数据值对
         */
        public static <T, U> Pair<T, U> makePair(T first, U second) {
            return new Pair<>(first, second);
        }

        /**
         * 返回第一个值。
         *
         * @return 第一个值
         */
        public T getFirst() {
            return first;
        }

        /**
         * 返回第二个值。
         *
         * @return 第二个值
         */
        public U getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Pair))
                return false;

            Pair pair = (Pair) obj;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return String.format("Pair: (%s, %s)", getFirst(), getSecond());
        }
    }
}

