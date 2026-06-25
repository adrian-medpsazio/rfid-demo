package com.club.rfid_access.domain.service;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Test
    void getPresignedUrl_withNullKey_shouldReturnNull() {
        var service = new StorageService(minioClient, "test-bucket");
        assertNull(service.getPresignedUrl(null));
    }

    @Test
    void getPresignedUrl_withBlankKey_shouldReturnNull() {
        var service = new StorageService(minioClient, "test-bucket");
        assertNull(service.getPresignedUrl("   "));
    }

    @Test
    void generateThumbnail_shouldResizeTo200pxWidth(@TempDir Path tempDir) throws Exception {
        // Create a 400x300 test JPEG
        BufferedImage original = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = original.createGraphics();
        g.drawRect(0, 0, 399, 299);
        g.dispose();

        Path input = tempDir.resolve("test.jpg");
        ImageIO.write(original, "jpeg", input.toFile());

        var file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(java.nio.file.Files.newInputStream(input));
        when(file.getContentType()).thenReturn("image/jpeg");

        var service = new StorageService(minioClient, "test-bucket");
        byte[] thumbBytes = service.generateThumbnail(file, 200);

        assertNotNull(thumbBytes);
        BufferedImage thumb = ImageIO.read(new java.io.ByteArrayInputStream(thumbBytes));
        assertNotNull(thumb, "Thumbnail should be a valid image");
        assertEquals(200, thumb.getWidth(), "Thumbnail width should be 200px");
        assertEquals(150, thumb.getHeight(), "Thumbnail height should maintain aspect ratio (300 * 200/400 = 150)");
    }

    @Test
    void generateThumbnail_withSmallImage_shouldNotUpscale(@TempDir Path tempDir) throws Exception {
        BufferedImage original = new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB);
        Path input = tempDir.resolve("small.jpg");
        ImageIO.write(original, "jpeg", input.toFile());

        var file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(java.nio.file.Files.newInputStream(input));
        when(file.getContentType()).thenReturn("image/jpeg");

        var service = new StorageService(minioClient, "test-bucket");
        byte[] thumbBytes = service.generateThumbnail(file, 200);

        BufferedImage thumb = ImageIO.read(new java.io.ByteArrayInputStream(thumbBytes));
        assertNotNull(thumb);
        assertEquals(100, thumb.getWidth(), "Image smaller than maxWidth should keep original width");
        assertEquals(80, thumb.getHeight(), "Image smaller than maxWidth should keep original height");
    }

    @Test
    void generateThumbnail_withInvalidImage_shouldThrow() {
        var file = mock(MultipartFile.class);
        try {
            when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("not-an-image".getBytes()));
        } catch (IOException e) {
            fail("Unexpected mock exception");
        }

        var service = new StorageService(minioClient, "test-bucket");
        assertThrows(IllegalArgumentException.class,
                () -> service.generateThumbnail(file, 200),
                "Invalid image should throw IllegalArgumentException");
    }
}
