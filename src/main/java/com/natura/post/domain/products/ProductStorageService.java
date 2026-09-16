package com.natura.post.domain.products;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductStorageService {

    private final S3Client s3Client;

    @Value("${R2_BUCKET_NAME}")
    private String bucketName;

    @Value("${R2_PUBLIC_URL}")
    private String publicUrl;

    public String uploadFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(file.getOriginalFilename());

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Retorna a URL pública acessível do arquivo no R2
            return publicUrl + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao enviar arquivo para o Cloudflare R2: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(publicUrl.length() + 1);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}