package com.clara.ops.challenge.document_management_service_challenge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.minio.MinioClient;

@SpringBootTest
@ActiveProfiles("test")
class DocumentManagementServiceChallengeApplicationTests {

  @Autowired private ApplicationContext applicationContext;

  @MockitoBean private MinioClient minioClient;

  @Test
  void shouldLoadApplicationContextSuccessfully() {
    assertThat(applicationContext).isNotNull();
    assertThat(applicationContext.getBeanDefinitionCount()).isGreaterThan(0);
  }
}
