package com.hankcs.io.adapter;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * FileIOAdapter
 *
 * @author luchaoxin
 * @version V 1.0
 * @since 2023-07-31
 */
@SuppressWarnings("all")
public class LocalIOAdapter implements IOAdapter {

    @Override

    public InputStream open(String path) throws FileNotFoundException {
        return AccessController.doPrivileged((PrivilegedAction<FileInputStream>) () -> {
            try {
                return new FileInputStream(path);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public OutputStream create(String path) throws FileNotFoundException {
        return AccessController.doPrivileged((PrivilegedAction<FileOutputStream>) () -> {
            try {
                return new FileOutputStream(path);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public long lastModified(String path) throws IOException {
        return AccessController.doPrivileged((PrivilegedAction<Long>) () -> {
            try {
                File f = new File(path);
                if (f.exists()) {
                    return f.lastModified();
                }
                return 0L;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public boolean exist(String path) throws IOException {
        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
            try {
                return new File(path).exists();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
