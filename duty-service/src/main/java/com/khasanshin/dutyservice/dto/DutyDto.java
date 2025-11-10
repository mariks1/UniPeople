package com.khasanshin.dutyservice.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DutyDto {

  UUID id;
  String name;
  String code;
}
