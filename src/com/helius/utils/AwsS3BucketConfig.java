package com.helius.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class AwsS3BucketConfig {
	
	String bucketName = Utils.getAwsprop("aws.bucketname");
	String acessKey = Utils.getAwsprop("aws.acesskey");
	String scretKey = Utils.getAwsprop("aws.scretkey");
	String region = Utils.getAwsprop("aws.region");

	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(acessKey, scretKey);

		return S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(region)).build();

	}
}
