package com.hankcs.io.adapter;

import com.hankcs.hanlp.HanLP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DelegateIOAdapter
 *
 * @author luchaoxin
 * @version V 1.0
 * @since 2023-07-31
 */
public class DelegateIOAdapter implements IOAdapter {


    private static final DelegateIOAdapter INSTANCE = new DelegateIOAdapter();

    public static DelegateIOAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public long lastModified(String path) throws IOException {
        if (HanLP.Config.IOAdapter instanceof IOAdapter s3IOAdapter) {
            return s3IOAdapter.lastModified(path);
        }
        return 0;
    }

    @Override
    public boolean exist(String path) throws IOException {
        if (HanLP.Config.IOAdapter instanceof IOAdapter adapter) {
            return adapter.exist(path);
        }
        return false;
    }

    @Override
    public InputStream open(String path) throws IOException {
        return HanLP.Config.IOAdapter.open(path);
    }

    @Override
    public OutputStream create(String path) throws IOException {
        return HanLP.Config.IOAdapter.create(path);
    }
}
