package com.hankcs.lucene;

import com.hankcs.cfg.Configuration;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import org.apache.lucene.analysis.Analyzer;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: N-最短路径分析器
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class HanLPNShortAnalyzer extends Analyzer {
    /**
     * 分词配置
     */
    private final Configuration conf;

    public HanLPNShortAnalyzer(Configuration conf) {
        super();
        this.conf = conf;
        enableConfiguration();
    }

    @Override
    @SuppressWarnings("all")
    protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
        return new Analyzer.TokenStreamComponents(TokenizerBuilder.tokenizer(
                AccessController.doPrivileged(
                        (PrivilegedAction<Segment>) () ->
                                new NShortSegment()
                                        .enableCustomDictionary(false)
                                        .enablePlaceRecognize(true)
                                        .enableOrganizationRecognize(true)
                ),
                conf
        ));
    }

    private void enableConfiguration() {
        this.conf.enableCustomDictionary(false)
                .enablePlaceRecognize(true)
                .enableOrganizationRecognize(true);
    }
}
