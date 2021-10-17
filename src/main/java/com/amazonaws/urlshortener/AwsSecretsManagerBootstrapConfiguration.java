// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.urlshortener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.secretsmanager.AwsSecretsManagerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
 
@Configuration
@EnableConfigurationProperties(AwsSecretsManagerProperties.class)
@ConditionalOnClass({ AWSSecretsManager.class })
@ConditionalOnProperty(prefix = AwsSecretsManagerProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AwsSecretsManagerBootstrapConfiguration {
 
@Value("${region}")
private String region;

//public static final String REGION;
 
 @Bean
 @ConditionalOnMissingBean
 AWSSecretsManager smClient() {
 System.out.println("Mayur value of region within secrets manager====>"+region);
 return AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
 }
}