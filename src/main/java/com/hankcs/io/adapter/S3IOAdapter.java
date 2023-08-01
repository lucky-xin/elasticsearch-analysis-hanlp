package com.hankcs.io.adapter;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.monitoring.EnvironmentVariableCsmConfigurationProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.Optional;

/**
 * S3 IO适配器
 *
 * @author luchaoxin
 * @version V 1.0
 * @since 2023-07-31
 */
public class S3IOAdapter implements IOAdapter {

    private final String bucket = env("S3_BUCKET", null);
    private final String key = env("S3_KEY", null);

    private final AmazonS3 s3Client;

    public S3IOAdapter() {
        String endpoint = env("S3_ENDPOINT", "oss-cn-shenzhen.aliyuncs.com");
        String region = env("S3_REGION", "us-east-1");
        boolean forcePathStyle = env("S3_FORCE_PATH_STYLE", "true").equals("true");
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(forcePathStyle)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withClientSideMonitoringConfigurationProvider(new EnvironmentVariableCsmConfigurationProvider())
                .build();
    }

    @SuppressWarnings("all")
    public long lastModified(String fp) throws IOException {
        return AccessController.doPrivileged((PrivilegedAction<Long>) () -> {
            try {
                String path = genPath(fp);
                ObjectMetadata meta = s3Client.getObjectMetadata(bucket, path);
                return Optional.ofNullable(meta.getLastModified())
                        .map(Date::getTime)
                        .orElse(0L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @SuppressWarnings("all")
    public boolean exist(String fp) throws IOException {
        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
            try {
                String path = genPath(fp);
                return s3Client.doesObjectExist(bucket, path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @SuppressWarnings("all")
    public InputStream open(String fp) throws IOException {
        return AccessController.doPrivileged((PrivilegedAction<InputStream>) () -> {
            try {
                String path = genPath(fp);
                S3Object object = s3Client.getObject(bucket, path);
                return object.getObjectContent();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @SuppressWarnings("all")
    public OutputStream create(String fp) throws IOException {
        return AccessController.doPrivileged((PrivilegedAction<OutputStream>) () -> {
            try {
                String path = genPath(fp);
                return new S3OutputStream(s3Client, bucket, path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }


    private String genPath(String path) {
        return key.startsWith("/") ? key + path : key + "/" + path;
    }

    private String env(String name, String defaultVal) {
        String val = System.getenv(name);
        if (val == null || val.isEmpty()) {
            return defaultVal;
        }
        return val;
    }

    class S3OutputStream extends OutputStream {
        private final AmazonS3 s3Client;

        private final String bucket;
        private final String key;

        private final ByteArrayOutputStream bos;

        public S3OutputStream(AmazonS3 s3Client, String bucket, String key) {
            this.s3Client = s3Client;
            this.bucket = bucket;
            this.key = key;
            this.bos = new ByteArrayOutputStream(1024 * 1024);
        }

        @Override
        public void write(int b) throws IOException {
            this.bos.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.bos.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.bos.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            ObjectMetadata meta = new ObjectMetadata();
            byte[] byteArray = this.bos.toByteArray();
            meta.setContentLength(byteArray.length);
            meta.setLastModified(new Date());
            PutObjectRequest req = new PutObjectRequest(
                    this.bucket,
                    this.key,
                    new ByteArrayInputStream(byteArray),
                    meta
            );
            this.s3Client.putObject(req);
        }

        @Override
        public void close() throws IOException {
            flush();
            this.bos.close();
        }
    }
}
