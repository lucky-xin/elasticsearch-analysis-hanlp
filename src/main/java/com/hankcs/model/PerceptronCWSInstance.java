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
public class PerceptronCWSInstance {

    private static final Logger logger = LogManager.getLogger(PerceptronCWSInstance.class);

    private static LinearModel linearModel;

    private static final PerceptronCWSInstance instance = new PerceptronCWSInstance();

    public static PerceptronCWSInstance getInstance() {
        return instance;
    }

    private PerceptronCWSInstance() {
        try {
            if (DelegateIOAdapter.getInstance().exist(HanLP.Config.PerceptronCWSModelPath)) {
                linearModel = new LinearModel(HanLP.Config.PerceptronCWSModelPath);
            }
        } catch (Exception e) {
            logger.warn("can not find perceptron cws model from:" + HanLP.Config.PerceptronCWSModelPath, e);
        }
    }

    public LinearModel getLinearModel() {
        return linearModel;
    }
}
