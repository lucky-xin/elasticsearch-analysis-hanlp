package com.hankcs.dic;

import com.hankcs.cfg.Configuration;
import com.hankcs.cfg.HanlpPath;
import com.hankcs.dic.cache.DictionaryFileCache;
import com.hankcs.io.adapter.DelegateIOAdapter;
import com.hankcs.utility.CustomDictionaryUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.plugin.analysis.hanlp.AnalysisHanLPPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: 自定义词典监控线程
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
@SuppressWarnings("all")
public class ExtMonitor implements Runnable {

    private static final Logger logger = LogManager.getLogger(ExtMonitor.class);
    Configuration config;


    ExtMonitor(Configuration config) {
        this.config = config;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
    }

    @Override
    public void run() {
        List<DictionaryFile> oldDictionaryFiles = DictionaryFileCache.getCustomDictionaryFiles();
        String[] newDictionaryPaths = reloadDictionaryPaths();
        List<DictionaryFile> newDictionaryFiles = getCurrentDictionaryFiles(newDictionaryPaths);
        List<DictionaryFile> modifiedFiles = new ArrayList<>(newDictionaryFiles.size());
        for (DictionaryFile currentDictionaryFile : newDictionaryFiles) {
            if (!oldDictionaryFiles.contains(currentDictionaryFile)) {
                modifiedFiles.add(currentDictionaryFile);
            }
        }

        if (modifiedFiles.isEmpty()) {
            logger.debug("hanlp custom dictionary isn't modified, so no need reload");
            return;
        }
        DictionaryFileCache.setCustomDictionaryFiles(newDictionaryFiles);
        reload(modifiedFiles);
    }

    private static void reload(List<DictionaryFile> modifiedFiles) {
        logger.info("reloading hanlp custom dictionary");
        try {
            CustomDictionaryUtility.reload(modifiedFiles);
        } catch (Exception e) {
            logger.error("can not reload hanlp custom dictionary", e);
        }

        DictionaryFileCache.writeCache();
        logger.info("finish reload hanlp custom dictionary");
    }

    private String[] reloadDictionaryPaths() {
        Properties p = new Properties();
        Path path = config.getEnvironment().configFile()
                .resolve(AnalysisHanLPPlugin.PLUGIN_NAME)
                .resolve(HanlpPath.HANLP_PROPERTIES_PATH)
                .toAbsolutePath();
        try (InputStream in = new FileInputStream(path.toString());
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            p.load(isr);
            String root = p.getProperty("root", "").replace("\\\\", "/");
            if (!root.isEmpty() && !root.endsWith("/")) {
                root += "/";
            }

            String[] pathArray = p.getProperty("CustomDictionaryPath",
                    "data/dictionary/custom/CustomDictionary.txt").split(";");
            String prePath = root;
            for (int i = 0; i < pathArray.length; ++i) {
                if (pathArray[i].startsWith(" ")) {
                    pathArray[i] = prePath + pathArray[i].trim();
                } else {
                    pathArray[i] = root + pathArray[i];
                    int lastSplash = pathArray[i].lastIndexOf('/');
                    if (lastSplash != -1) {
                        prePath = pathArray[i].substring(0, lastSplash + 1);
                    }
                }
            }
            return pathArray;
        } catch (Exception e) {
            logger.error("can not find hanlp.properties", e);
        }
        return new String[0];
    }

    private List<DictionaryFile> getCurrentDictionaryFiles(String[] customDictionaryPaths) {
        List<DictionaryFile> dictionaryFileList = new ArrayList<>();
        for (String customDictionaryPath : customDictionaryPaths) {
            String[] tuple = customDictionaryPath.split(" ");
            String tmp = tuple[0];
            int cut = tmp.indexOf(' ');
            String path = tmp;
            String nature = null;
            if (cut > 0) {
                // 有默认词性
                nature = tmp.substring(cut + 1);
                path = tmp.substring(0, cut);
            }
            logger.debug("hanlp custom path: {}", path);
            long lastModified = 0;
            try {
                lastModified = DelegateIOAdapter.getInstance().lastModified(path);
            } catch (Exception e) {
                logger.error("get s3 file last modified error", e);
            }

            if (tuple.length > 1) {
                if (tuple[1] == null || tuple[1].isEmpty()) {
                    dictionaryFileList.add(new DictionaryFile(path, lastModified));
                } else {
                    dictionaryFileList.add(new DictionaryFile(path, tuple[1].trim(), lastModified, nature));
                }
            } else {
                dictionaryFileList.add(new DictionaryFile(path, lastModified));
            }
        }
        return dictionaryFileList;
    }
}
