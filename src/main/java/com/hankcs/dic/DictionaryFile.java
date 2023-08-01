package com.hankcs.dic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Project: elasticsearch-analysis-hanlp
 * Description: 自定义词典文件信息
 * Author: Kenn
 * Create:
 * 2018-12-14 15:10
 */
public class DictionaryFile {

    private String path;

    private String type;
    private String nature;

    private long lastModified;

    public DictionaryFile() {
    }

    public DictionaryFile(String path, long lastModified) {
        this.path = path;
        this.lastModified = lastModified;
    }

    public DictionaryFile(String path, String type, long lastModified, String nature) {
        this(path, lastModified);
        this.type = type;
        this.nature = nature;
    }

    public void write(DataOutputStream out) throws IOException {
        if (path != null && !path.isEmpty()) {
            byte[] bytes = path.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        } else {
            out.writeInt(0);
        }
        if (type != null && !type.isEmpty()) {
            byte[] bytes = type.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        } else {
            out.writeInt(0);
        }
        out.writeLong(lastModified);
    }

    @SuppressWarnings("all")
    public void read(DataInputStream in) throws IOException {
        int pathLength = in.readInt();
        if (pathLength != 0) {
            byte[] bytes = new byte[pathLength];
            in.read(bytes);
            path = new String(bytes, StandardCharsets.UTF_8);
        }

        int typeLength = in.readInt();
        if (typeLength != 0) {
            byte[] bytes = new byte[typeLength];
            in.read(bytes);
            type = new String(bytes, StandardCharsets.UTF_8);
        }
        lastModified = in.readLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DictionaryFile that = (DictionaryFile) o;
        return lastModified == that.lastModified
                && Objects.equals(path, that.path)
                && Objects.equals(type, that.type)
                && Objects.equals(nature, that.nature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, type, nature, lastModified);
    }

    @Override
    public String toString() {
        return "DictionaryFile{" +
                "path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", nature='" + nature + '\'' +
                ", lastModified=" + lastModified +
                '}';
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getNature() {
        return nature;
    }
}
