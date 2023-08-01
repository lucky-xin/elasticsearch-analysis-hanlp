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
public class PerceptronPOSInstance {

    private static final Logger logger = LogManager.getLogger(PerceptronPOSInstance.class);

    private LinearModel linearModel;

    private static final PerceptronPOSInstance instance = new PerceptronPOSInstance();

    public static PerceptronPOSInstance getInstance() {
        return instance;
    }

    private PerceptronPOSInstance() {
        try {
            if (DelegateIOAdapter.getInstance().exist(HanLP.Config.PerceptronPOSModelPath)) {
                linearModel = new LinearModel(HanLP.Config.PerceptronPOSModelPath);
            }
        } catch (Exception e) {
            logger.warn("can not find perceptron pos model from:" + HanLP.Config.PerceptronPOSModelPath, e);
        }
    }

    public LinearModel getLinearModel() {
        return linearModel;
    }
}
