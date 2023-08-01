package org.elasticsearch.index.analysis;

import com.hankcs.cfg.Configuration;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Other.DoubleArrayTrieSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.lucene.TokenizerBuilder;
import com.hankcs.model.*;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: Hanlp tokenizer factory
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class HanLPTokenizerFactory extends AbstractTokenizerFactory {
    /**
     * 分词类型
     */
    private final HanLPType hanLPType;
    /**
     * 分词配置
     */
    private final Configuration config;

    public HanLPTokenizerFactory(IndexSettings indexSettings,
                                 Environment env,
                                 String name,
                                 Settings settings,
                                 HanLPType hanLPType) {
        super(indexSettings, settings, name);
        this.hanLPType = hanLPType;
        this.config = new Configuration(env, settings);
    }

    public static HanLPTokenizerFactory getHanLPTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                 Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.HANLP);
    }

    public static HanLPTokenizerFactory getHanLPStandardTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                         Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.STANDARD);
    }

    public static HanLPTokenizerFactory getHanLPIndexTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                      Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.INDEX);
    }

    public static HanLPTokenizerFactory getHanLPNLPTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                    Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.NLP);
    }

    public static HanLPTokenizerFactory getHanLPCRFTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                    Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.CRF);
    }

    public static HanLPTokenizerFactory getHanLPNShortTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                       Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.N_SHORT);
    }

    public static HanLPTokenizerFactory getHanLPDijkstraTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                         Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.DIJKSTRA);
    }

    public static HanLPTokenizerFactory getHanLPSpeedTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                      Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.SPEED);
    }

    @Override
    @SuppressWarnings("all")
    public Tokenizer create() {
        switch (this.hanLPType) {
            case INDEX -> {
                config.enableIndexMode(true);
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                HanLP.newSegment()
                                        .enableIndexMode(true)
                        ),
                        config
                );
            }
            case NLP -> {
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                new PerceptronLexicalAnalyzer(
                                        PerceptronCWSInstance.getInstance().getLinearModel(),
                                        PerceptronPOSInstance.getInstance().getLinearModel(),
                                        PerceptronNERInstance.getInstance().getLinearModel()
                                )
                        ),
                        config
                );
            }
            case CRF -> {
                if (CRFPOSTaggerInstance.getInstance().getTagger() == null) {
                    return TokenizerBuilder.tokenizer(
                            AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                    new CRFLexicalAnalyzer(CRFSegmenterInstance.getInstance().getSegmenter())
                            ),
                            config
                    );
                } else if (CRFNERecognizerInstance.getInstance().getRecognizer() == null) {
                    return TokenizerBuilder.tokenizer(
                            AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                    new CRFLexicalAnalyzer(
                                            CRFSegmenterInstance.getInstance().getSegmenter(),
                                            CRFPOSTaggerInstance.getInstance().getTagger()
                                    )
                            ),
                            config);
                } else {
                    return TokenizerBuilder.tokenizer(
                            AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                    new CRFLexicalAnalyzer(
                                            CRFSegmenterInstance.getInstance().getSegmenter(),
                                            CRFPOSTaggerInstance.getInstance().getTagger(),
                                            CRFNERecognizerInstance.getInstance().getRecognizer()
                                    )
                            ),
                            config
                    );
                }
            }
            case N_SHORT -> {
                config.enableCustomDictionary(false)
                        .enablePlaceRecognize(true)
                        .enableOrganizationRecognize(true);
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                new NShortSegment()
                                        .enableCustomDictionary(false)
                                        .enablePlaceRecognize(true)
                                        .enableOrganizationRecognize(true)
                        ),
                        config
                );
            }
            case DIJKSTRA -> {
                config.enableCustomDictionary(false)
                        .enablePlaceRecognize(true)
                        .enableOrganizationRecognize(true);
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                new DijkstraSegment()
                                        .enableCustomDictionary(false)
                                        .enablePlaceRecognize(true)
                                        .enableOrganizationRecognize(true)
                        ),
                        config
                );
            }
            case SPEED -> {
                config.enableCustomDictionary(false);
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                new DoubleArrayTrieSegment()
                                        .enableCustomDictionary(false)
                        ),
                        config
                );
            }
            default -> {
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                HanLP.newSegment()
                        ),
                        config
                );
            }
        }
    }
}
