package com.hankcs.model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.model.crf.CRFPOSTagger;
import com.hankcs.io.adapter.DelegateIOAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description:
 * Author: Kenn
 * Create: 2021-01-30 01:22
 */
public class CRFPOSTaggerInstance {

    private static final Logger logger = LogManager.getLogger(CRFPOSTaggerInstance.class);

    private static CRFPOSTagger tagger;

    private static final CRFPOSTaggerInstance instance = new CRFPOSTaggerInstance();

    public static CRFPOSTaggerInstance getInstance() {
        return instance;
    }

    private CRFPOSTaggerInstance() {
        try {
            if (DelegateIOAdapter.getInstance().exist(HanLP.Config.CRFPOSModelPath)) {
                tagger = new CRFPOSTagger(HanLP.Config.CRFPOSModelPath);
            }
        } catch (Exception e) {
            logger.warn("can not find crf pos model from :" + HanLP.Config.CRFPOSModelPath, e);
        }
    }

    public CRFPOSTagger getTagger() {
        return tagger;
    }
}
