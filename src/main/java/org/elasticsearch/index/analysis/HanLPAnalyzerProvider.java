package org.elasticsearch.index.analysis;

import com.hankcs.cfg.Configuration;
import com.hankcs.lucene.*;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: Hanlp analyzer provider
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class HanLPAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {

    private final Analyzer analyzer;

    public HanLPAnalyzerProvider(
            IndexSettings indexSettings,
            Environment env,
            String name,
            Settings settings,
            HanLPType hanLPType) {
        super(name, settings);
        Configuration configuration = new Configuration(env, settings);
        switch (hanLPType) {
            case HANLP -> analyzer = new HanLPAnalyzer(configuration);
            case STANDARD -> analyzer = new HanLPStandardAnalyzer(configuration);
            case INDEX -> analyzer = new HanLPIndexAnalyzer(configuration);
            case NLP -> analyzer = new HanLPNLPAnalyzer(configuration);
            case CRF -> analyzer = new HanLPCRFAnalyzer(configuration);
            case N_SHORT -> analyzer = new HanLPNShortAnalyzer(configuration);
            case DIJKSTRA -> analyzer = new HanLPDijkstraAnalyzer(configuration);
            case SPEED -> analyzer = new HanLPSpeedAnalyzer(configuration);
            default -> analyzer = null;
        }
    }

    public static HanLPAnalyzerProvider getHanLPAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                 Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.HANLP);
    }

    public static HanLPAnalyzerProvider getHanLPStandardAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                         Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.STANDARD);
    }

    public static HanLPAnalyzerProvider getHanLPIndexAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                      Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.INDEX);
    }

    public static HanLPAnalyzerProvider getHanLPNLPAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                    Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.NLP);
    }

    public static HanLPAnalyzerProvider getHanLPCRFAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                    Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.CRF);
    }

    public static HanLPAnalyzerProvider getHanLPNShortAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                       Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.N_SHORT);
    }

    public static HanLPAnalyzerProvider getHanLPDijkstraAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                         Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.DIJKSTRA);
    }

    public static HanLPAnalyzerProvider getHanLPSpeedAnalyzerProvider(IndexSettings indexSettings, Environment env, String name,
                                                                      Settings settings) {
        return new HanLPAnalyzerProvider(indexSettings, env, name, settings, HanLPType.SPEED);
    }

    @Override
    public Analyzer get() {
        return this.analyzer;
    }
}
