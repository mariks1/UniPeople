package com.khasanshin.leaveservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateLeaveRequestDto {

  @JsonAlias("date_from")
  @JsonProperty("date_from")
  LocalDate dateFrom;

  @JsonAlias("date_to")
  @JsonProperty("date_to")
  LocalDate dateTo;

  @Size(max = 1000)
  String comment;
}
