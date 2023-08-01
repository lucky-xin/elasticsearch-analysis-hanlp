package com.hankcs.model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.model.crf.CRFNERecognizer;
import com.hankcs.io.adapter.DelegateIOAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description:
 * Author: Kenn
 * Create: 2021-01-30 01:22
 */
public class CRFNERecognizerInstance {

    private static final Logger logger = LogManager.getLogger(CRFNERecognizerInstance.class);

    private static CRFNERecognizer recognizer;

    private static final CRFNERecognizerInstance instance = new CRFNERecognizerInstance();

    public static CRFNERecognizerInstance getInstance() {
        return instance;
    }

    private CRFNERecognizerInstance() {
        try {
            if (DelegateIOAdapter.getInstance().exist(HanLP.Config.CRFNERModelPath)) {
                recognizer = new CRFNERecognizer(HanLP.Config.CRFNERModelPath);
            }
        } catch (Exception e) {
            logger.error("can not find crf ner model from:" + HanLP.Config.CRFNERModelPath, e);
        }
    }

    public CRFNERecognizer getRecognizer() {
        return recognizer;
    }
}
