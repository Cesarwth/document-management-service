package com.clara.ops.challenge.document_management_service_challenge.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "document-management")
@Data
@Validated
public class DocumentManagementProperties {

  @NotNull private Upload upload = new Upload();

  @NotNull private Pagination pagination = new Pagination();

  @Data
  public static class Upload {
    @Min(1)
    @Max(1024)
    private long maxFileSizeMb = 550L;

    public long getMaxFileSizeBytes() {
      return maxFileSizeMb * 1024 * 1024;
    }
  }

  @Data
  public static class Pagination {
    @Min(1)
    @Max(100)
    private int defaultSize = 20;

    @Min(1)
    @Max(1000)
    private int maxSize = 100;
  }
}
