package com.club.rfid_access.domain.service;

import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final MinioClient minioClient;
    private final String bucket;

    public StorageService(MinioClient minioClient,
                          @Value("${rfid.storage.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        initBucket();
    }

    private void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception e) {
            log.warn("Could not init MinIO bucket (will retry on upload): {}", e.getMessage());
        }
    }

    /** Upload a file and its 200px-wide thumbnail. Returns the original object key. */
    public String uploadWithThumbnail(String key, MultipartFile file) {
        String uploaded = upload(key, file);
        if (uploaded != null) {
            try {
                byte[] thumb = generateThumbnail(file, 200);
                String thumbKey = key.replaceFirst("(\\.[^.]+)$", "_thumb$1");
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(thumbKey)
                        .stream(new java.io.ByteArrayInputStream(thumb), thumb.length, -1)
                        .contentType("image/jpeg")
                        .build());
                log.info("Uploaded thumbnail {} to MinIO bucket {}", thumbKey, bucket);
            } catch (Exception e) {
                log.warn("Failed to generate/upload thumbnail for {}: {}", key, e.getMessage());
            }
        }
        return uploaded;
    }

    byte[] generateThumbnail(MultipartFile file, int maxWidth) throws Exception {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) throw new IllegalArgumentException("Unable to read image");

        int width = original.getWidth();
        int height = original.getHeight();
        if (width > maxWidth) {
            height = (int) (height * ((double) maxWidth / width));
            width = maxWidth;
        }

        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage thumb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumb.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumb, "jpeg", baos);
        return baos.toByteArray();
    }

    /** Upload a file and return the object key (e.g. "members/42/photo.jpg") */
    public String upload(String key, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("Uploaded {} to MinIO bucket {}", key, bucket);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO: " + e.getMessage(), e);
        }
    }

    /** Get a pre-signed URL valid for 1 hour. Returns null if key is null/empty or MinIO is unavailable. */
    public String getPresignedUrl(String key) {
        if (key == null || key.isBlank()) return null;
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .method(Method.GET)
                    .expiry(1, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to generate pre-signed URL for {}: {}", key, e.getMessage());
            return null;
        }
    }

    /** Stream the file bytes — for proxy endpoints */
    public GetObjectResponse download(String key) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from MinIO: " + e.getMessage(), e);
        }
    }
}
