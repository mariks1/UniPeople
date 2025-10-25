package com.khasanshin.employmentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CloseEmploymentDto {

  @JsonProperty("end_date")
  @JsonAlias("end_date")
  LocalDate endDate;
}
