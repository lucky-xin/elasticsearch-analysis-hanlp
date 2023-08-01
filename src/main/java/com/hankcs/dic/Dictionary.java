package com.hankcs.dic;

import com.hankcs.cfg.Configuration;
import com.hankcs.cfg.HanlpPath;
import com.hankcs.dic.cache.DictionaryFileCache;
import com.hankcs.dic.config.RemoteDictConfig;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.plugin.analysis.hanlp.AnalysisHanLPPlugin;

import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: 词典类
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
@SuppressWarnings("all")
public class Dictionary {
    /**
     * Hanlp远程词典配置文件名
     */
    private static final String REMOTE_CONFIG_FILE_NAME = "hanlp-remote.xml";
    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1, new ThreadFactory() {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            String threadName = "remote-dict-monitor-" + counter.getAndIncrement();
            return new Thread(r, threadName);
        }
    });
    /**
     * 词典实例
     */
    private static Dictionary singleton;
    private Configuration conf;

    private Dictionary(Configuration conf) {
        this.conf = conf;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
    }

    public static synchronized void initial(Configuration config) {
        if (singleton == null) {
            synchronized (Dictionary.class) {
                if (singleton == null) {
                    singleton = new Dictionary(config);
                    singleton.setUp();
                    if (config.isEnableDynamicCustomDictionary()) {
                        pool.scheduleAtFixedRate(new ExtMonitor(config), 10, 5 * 60, TimeUnit.SECONDS);
                    } else {
                        pool.schedule(new ExtMonitor(config), 10, TimeUnit.SECONDS);
                    }

                    if (config.isEnableRemoteDict()) {
                        for (String location : RemoteDictConfig.getSingleton().getRemoteExtDictionaries()) {
                            pool.scheduleAtFixedRate(new RemoteMonitor(location, "custom"), 10, 5 * 60, TimeUnit.SECONDS);
                        }
                        for (String location : RemoteDictConfig.getSingleton().getRemoteExtStopWordDictionaries()) {
                            pool.scheduleAtFixedRate(new RemoteMonitor(location, "stop"), 10, 5 * 60, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        }
    }

    private void setUp() {
        AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            DictionaryFileCache.loadCache();
            Path path = conf.getEnvironment()
                    .configFile()
                    .resolve(AnalysisHanLPPlugin.PLUGIN_NAME)
                    .resolve(HanlpPath.REMOTE_CONFIG_FILE_NAME)
                    .toAbsolutePath();
            RemoteDictConfig.initial(path.toString());
            return null;
        });
    }
}
