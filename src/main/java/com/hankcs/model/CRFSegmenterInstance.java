package com.hankcs.model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.model.crf.CRFSegmenter;
import com.hankcs.io.adapter.DelegateIOAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description:
 * Author: Kenn
 * Create: 2021-01-30 01:22
 */
public class CRFSegmenterInstance {

    private static final Logger logger = LogManager.getLogger(CRFSegmenterInstance.class);

    private static CRFSegmenter segmenter;

    private static final CRFSegmenterInstance instance = new CRFSegmenterInstance();

    public static CRFSegmenterInstance getInstance() {
        return instance;
    }

    private CRFSegmenterInstance() {
        try {
            if (DelegateIOAdapter.getInstance().exist(HanLP.Config.CRFCWSModelPath)) {
                segmenter = new CRFSegmenter(HanLP.Config.CRFCWSModelPath);
            }
        } catch (Exception e) {
            logger.warn("can not find crf cws model from:" + HanLP.Config.CRFCWSModelPath, e);
        }
    }


    public CRFSegmenter getSegmenter() {
        return segmenter;
    }
}
