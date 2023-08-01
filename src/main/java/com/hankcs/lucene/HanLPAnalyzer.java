package com.hankcs.lucene;

import com.hankcs.cfg.Configuration;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import org.apache.lucene.analysis.Analyzer;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: Hanlp默认分析器
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class HanLPAnalyzer extends Analyzer {
    /**
     * 分词配置
     */
    private final Configuration conf;

    public HanLPAnalyzer(Configuration conf) {
        super();
        this.conf = conf;
    }

    @SuppressWarnings("all")
    @Override
    protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
        return new Analyzer.TokenStreamComponents(
                TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) () -> HanLP.newSegment()),
                        conf
                )
        );
    }
}
