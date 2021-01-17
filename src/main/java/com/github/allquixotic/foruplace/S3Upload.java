package com.github.allquixotic.foruplace;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;

public class S3Upload {
    private final MinioClient client;
    private final String bucket;

    public S3Upload(String endpoint, String accessKey, String secretAccessKey, String bucket) {
        client = MinioClient.builder().endpoint(endpoint)
                .credentials(accessKey, secretAccessKey)
                .build();
        this.bucket = bucket;
    }

    public void upload(String path, String file) throws Exception {
        Main.log.info(String.format("Uploading local file '%s' to path '%s' in bucket '%s'", file, path, bucket));
        client.uploadObject(UploadObjectArgs.builder()
        .bucket(bucket)
        .object(path)
        .filename(file)
        .build());
    }
}
