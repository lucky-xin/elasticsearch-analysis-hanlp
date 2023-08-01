package com.hankcs.dic.cache;

import com.hankcs.dic.DictionaryFile;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hankcs.io.adapter.DelegateIOAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: 自定义词典文件信息缓存
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class DictionaryFileCache {

    private static final Logger logger = LogManager.getLogger(DictionaryFileCache.class);
    private static final String DICTIONARY_FILE_CACHE_RECORD_FILE = "plugins/analysis-hanlp/data/hanlp.cache";
    private static List<DictionaryFile> dictionaryFiles = new ArrayList<>();

    public static void loadCache() {

        List<DictionaryFile> dictionaryFileList = new ArrayList<>();
        try {
            if (HanLP.Config.IOAdapter != null) {
                logger.info(" HanLP.Config.IOAdapter:" + HanLP.Config.IOAdapter.getClass());
            }
            boolean exist = DelegateIOAdapter.getInstance().exist(DICTIONARY_FILE_CACHE_RECORD_FILE);
            if (!exist) {
                return;
            }
        } catch (Exception e) {
            logger.warn("can not check exist cache file:" + DICTIONARY_FILE_CACHE_RECORD_FILE, e);
        }

        try (DataInputStream in = new DataInputStream(IOUtil.newInputStream(DICTIONARY_FILE_CACHE_RECORD_FILE))) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                DictionaryFile dictionaryFile = new DictionaryFile();
                dictionaryFile.read(in);
                dictionaryFileList.add(dictionaryFile);
            }
            setCustomDictionaryFiles(dictionaryFileList);
        } catch (Exception e) {
            logger.error("can not load custom dictionary cache file:" + DICTIONARY_FILE_CACHE_RECORD_FILE, e);
        }
    }

    public static void writeCache() {
        try (OutputStream os = IOUtil.newOutputStream(DICTIONARY_FILE_CACHE_RECORD_FILE);
             DataOutputStream out = new DataOutputStream(os)) {
            out.writeInt(dictionaryFiles.size());
            StringJoiner sj = new StringJoiner(",");
            for (DictionaryFile dictionaryFile : dictionaryFiles) {
                sj.add(dictionaryFile.getPath());
                dictionaryFile.write(out);
            }
            logger.info("begin write down HanLP custom dictionary file cache, file path: {}, " +
                            "custom dictionary file list: {}",
                    DICTIONARY_FILE_CACHE_RECORD_FILE, sj);
            logger.info("write down HanLP custom dictionary file cache successfully");
        } catch (IOException e) {
            logger.warn("can not write down HanLP custom dictionary file cache", e);
        }
    }

    public static List<DictionaryFile> getCustomDictionaryFiles() {
        return dictionaryFiles;
    }

    public static synchronized void setCustomDictionaryFiles(List<DictionaryFile> customDictionaryFileList) {
        DictionaryFileCache.dictionaryFiles = customDictionaryFileList;
    }
}
