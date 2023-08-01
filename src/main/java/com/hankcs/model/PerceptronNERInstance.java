package com.hankcs.model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.model.perceptron.model.LinearModel;
import com.hankcs.io.adapter.DelegateIOAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description:
 * Author: Kenn
 * Create: 2020-10-09 09:47
 */
public class PerceptronNERInstance {

    private static final Logger logger = LogManager.getLogger(PerceptronNERInstance.class);
    private LinearModel linearModel;

    private static final PerceptronNERInstance instance = new PerceptronNERInstance();

    public static PerceptronNERInstance getInstance() {
        return instance;
    }

    private PerceptronNERInstance() {
        try {
            if (DelegateIOAdapter.getInstance().exist(HanLP.Config.PerceptronNERModelPath)) {
                linearModel = new LinearModel(HanLP.Config.PerceptronNERModelPath);
            }
        } catch (Exception e) {
            logger.warn("can not find perceptron ner model from:" + HanLP.Config.PerceptronNERModelPath, e);
        }
    }

    public LinearModel getLinearModel() {
        return linearModel;
    }
}
