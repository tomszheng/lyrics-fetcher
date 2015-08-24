package com.kumasuke.sample;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.Lyrics;
import com.kumasuke.fetcher.ftr.UtaTenFetcher;
import com.kumasuke.fetcher.util.UserAgent;

import java.util.Scanner;

public class UtaTenSample {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String utaTenPage = scanner.next();
        Fetcher fetcher = new UtaTenFetcher(utaTenPage, UserAgent.getUserAgent());

        printFetcher(fetcher);
    }

    private static void printFetcher(Fetcher fetcher) {
        fetcher.getHeader()
                .forEach(System.out::println);
        System.out.println("------------------------------------");
        fetcher.getLyrics()
                .forEach(System.out::println);
        System.out.println("------------------------------------");

        @SuppressWarnings("deprecation")
        Lyrics lyricsWithRuby = fetcher.getLyricsWithRuby();
        if (lyricsWithRuby != null)
            lyricsWithRuby
                    .forEach(System.out::println);
    }
}
