package com.hankcs.utility;

import com.hankcs.dic.DictionaryFile;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.utility.LexiconUtility;
import com.hankcs.hanlp.utility.Predefine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: 自定义分词工具类
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class CustomDictionaryUtility {

    private static final Logger logger = LogManager.getLogger(CustomDictionaryUtility.class);

    public static boolean reload(List<DictionaryFile> modifiedFiles) {
        logger.debug("hanlp custom dictionary model size before reload: {}",
                CustomDictionary.DEFAULT.dat.getSize());
        if (modifiedFiles == null || modifiedFiles.isEmpty()) {
            return false;
        }
        TreeMap<String, CoreDictionary.Attribute> map = new TreeMap<>();
        LinkedHashSet<Nature> customNatureCollector = new LinkedHashSet<>();
        try {
            for (DictionaryFile df : modifiedFiles) {
                Nature defaultNature = Nature.n;
                // 有默认词性
                String nature = df.getNature();
                String path = df.getPath();
                if (nature != null) {
                    try {
                        defaultNature = LexiconUtility.convertStringToNature(nature, customNatureCollector);
                    } catch (Exception e) {
                        logger.error(() -> new ParameterizedMessage("hanlp config file [{}] write error", path), e);
                        continue;
                    }
                }
                logger.debug("hanlp begin reload custom dictionary: {}, default nature: {}", path, defaultNature);
                if (!load(path, defaultNature, map, customNatureCollector)) {
                    logger.warn("hanlp reload error, custom dictionary: {}", path);
                }
            }

            if (map.isEmpty()) {
                logger.warn("hanlp does not reload any words");
                // 当作空白占位符
                map.put(Predefine.TAG_OTHER, null);
            }
            logger.debug("hanlp begin build double array trie");
            CustomDictionary.DEFAULT.dat = new DoubleArrayTrie<>();
            CustomDictionary.DEFAULT.dat.build(map);
            // 缓存成dat文件，下次加载会快很多
            logger.debug("hanlp converting custom dictionary cache to dat file");
            // 缓存值文件
            logger.debug("hanlp traversing custom dictionary words");
            List<CoreDictionary.Attribute> attributeList = new LinkedList<>(map.values());
            logger.debug("hanlp traverse custom dictionary successfully");
            try (OutputStream out = IOUtil.newOutputStream(HanLP.Config.CustomDictionaryPath[0] + Predefine.BIN_EXT);
                 DataOutputStream dos = new DataOutputStream(out);) {
                // 缓存用户词性
                IOUtil.writeCustomNature(dos, customNatureCollector);
                // 缓存正文
                logger.debug("hanlp traversing custom words to write into file");
                dos.writeInt(attributeList.size());
                for (CoreDictionary.Attribute attribute : attributeList) {
                    attribute.save(dos);
                }
                logger.debug("hanlp traverse custom words to write into file successfully");
                CustomDictionary.DEFAULT.dat.save(dos);
            }

            logger.debug("hanlp custom dictionary model size after reload: {}", CustomDictionary.DEFAULT.dat.getSize());
        } catch (FileNotFoundException e) {
            logger.error(() ->
                    new ParameterizedMessage("hanlp custom dictionary main path [{}] is not exist",
                            HanLP.Config.CustomDictionaryPath[0]), e);
            return false;
        } catch (IOException e) {
            logger.error(() ->
                    new ParameterizedMessage("hanlp custom dictionary main path [{}] read failed",
                            HanLP.Config.CustomDictionaryPath[0]), e);
            return false;
        } catch (Exception e) {
            logger.error(() ->
                    new ParameterizedMessage("hanlp custom dictionary cache failed, main path: {}",
                            HanLP.Config.CustomDictionaryPath[0]), e);
            return false;
        }
        return true;
    }

    /**
     * 加载用户词典（追加）
     *
     * @param path                  词典路径
     * @param defaultNature         默认词性
     * @param customNatureCollector 收集用户词性
     * @return 成功返回true，失败返回false
     */
    private static boolean load(String path,
                                Nature defaultNature,
                                TreeMap<String, CoreDictionary.Attribute> map,
                                LinkedHashSet<Nature> customNatureCollector) {
        try (InputStreamReader isr = new InputStreamReader(IOUtil.newInputStream(path), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr);) {
            String splitter = "\\s";
            if (path.endsWith(".csv")) {
                splitter = ",";
            }
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    line = IOUtil.removeUTF8BOM(line);
                    firstLine = false;
                }
                String[] param = line.split(splitter);
                // 排除空行
                if (param[0].isEmpty()) {
                    continue;
                }
                // 正规化
                if (HanLP.Config.Normalization) {
                    param[0] = CharTable.convert(param[0]);
                }
                int natureCount = (param.length - 1) / 2;
                CoreDictionary.Attribute attribute;
                if (natureCount == 0) {
                    attribute = new CoreDictionary.Attribute(defaultNature);
                } else {
                    attribute = new CoreDictionary.Attribute(natureCount);
                    for (int i = 0; i < natureCount; ++i) {
                        attribute.nature[i] = LexiconUtility.convertStringToNature(param[1 + 2 * i], customNatureCollector);
                        attribute.frequency[i] = Integer.parseInt(param[2 + 2 * i]);
                        attribute.totalFrequency += attribute.frequency[i];
                    }
                }
                map.put(param[0], attribute);
            }
        } catch (Exception e) {
            logger.error(() -> new ParameterizedMessage("hanlp custom dictionary [{}] read failed!", path), e);
            return false;
        }
        return true;
    }
}
