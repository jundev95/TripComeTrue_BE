package com.haejwo.tripcometrue.global.s3.response;

public record S3UploadResponseDto(
        String imageUrl
) {
    public S3UploadResponseDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
