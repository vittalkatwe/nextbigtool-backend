package com.nextbigtool.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private String ventureImagesBucket;

    @Value("${cloud.aws.credentials.region}")
    private String region;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String ext = getExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        PutObjectRequest request = new PutObjectRequest(ventureImagesBucket, key, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        amazonS3.putObject(request);

        return "https://" + ventureImagesBucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            amazonS3.deleteObject(ventureImagesBucket, key);
        } catch (Exception ignored) {}
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return "." + filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
