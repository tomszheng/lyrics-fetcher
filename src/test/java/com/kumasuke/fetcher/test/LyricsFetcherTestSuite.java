package com.kumasuke.fetcher.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * LyricsFetcher 测试套装
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AutoMatchAndFetchTest.class,
        FetchAndPrintResultTest.class
})
public class LyricsFetcherTestSuite {
}
