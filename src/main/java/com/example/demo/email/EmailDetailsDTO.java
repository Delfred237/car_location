package com.example.demo.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailDetailsDTO {

    private String recipient;
    private String subject;
    private String templateName;
    private Map<String, Object> templateModel = new HashMap<>();
}
