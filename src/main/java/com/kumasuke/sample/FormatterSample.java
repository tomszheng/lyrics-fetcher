package com.kumasuke.sample;

import com.kumasuke.fetcher.Fetcher;
import com.kumasuke.fetcher.util.FetcherBuilder;
import com.kumasuke.fetcher.util.Formatter;
import com.kumasuke.fetcher.util.UserAgent;

import java.io.IOException;

public class FormatterSample {
    public static void main(String[] args) throws Exception {
        Fetcher fetcher = FetcherBuilder.builder()
                .site("Kasi-Time.com")
                .page("73631")
                .userAgent(UserAgent.getUserAgent())
                .build();

        System.out.println("Html part:");
        System.out.println(Formatter.headerToHtml(fetcher.getHeader()));
        System.out.println(Formatter.lyricsToHtml(fetcher.getLyrics()));
        pressAnyKeyToContinue();

        System.out.println("Text part:");
        System.out.println(Formatter.headerToText(fetcher.getHeader()));
        System.out.println(Formatter.lyricsToText(fetcher.getLyrics()));
        System.out.println("Done!");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void pressAnyKeyToContinue() {
        System.out.println("Press any key to continue...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}