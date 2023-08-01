package com.hankcs.lucene;

import com.hankcs.cfg.Configuration;
import com.hankcs.hanlp.seg.Other.DoubleArrayTrieSegment;
import com.hankcs.hanlp.seg.Segment;
import org.apache.lucene.analysis.Analyzer;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: 极速词典分析器
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class HanLPSpeedAnalyzer extends Analyzer {
    /**
     * 分词配置
     */
    private final Configuration conf;

    public HanLPSpeedAnalyzer(Configuration conf) {
        super();
        this.conf = conf;
        enableConfiguration();
    }

    @Override
    @SuppressWarnings("all")
    protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
        return new Analyzer.TokenStreamComponents(
                TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged(
                                (PrivilegedAction<Segment>) () ->
                                        new DoubleArrayTrieSegment().enableCustomDictionary(false)
                        ),
                        conf
                )
        );
    }

    private void enableConfiguration() {
        this.conf.enableCustomDictionary(false);
    }
}
