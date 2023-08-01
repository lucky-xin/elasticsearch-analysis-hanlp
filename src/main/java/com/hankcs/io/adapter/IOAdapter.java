package com.hankcs.io.adapter;

import com.hankcs.hanlp.corpus.io.IIOAdapter;

import java.io.IOException;

/**
 * IOAdapter
 *
 * @author luchaoxin
 * @version V 1.0
 * @since 2023-07-31
 */
public interface IOAdapter extends IIOAdapter {

    /**
     * 最后编辑时间
     *
     * @param path
     * @return
     * @throws IOException
     */
    long lastModified(String path) throws IOException;

    /**
     * 是否存在
     *
     * @param path
     * @return
     * @throws IOException
     */
    boolean exist(String path) throws IOException;
}
