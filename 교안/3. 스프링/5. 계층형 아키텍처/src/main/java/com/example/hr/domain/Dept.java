package com.example.hr.domain;

import lombok.*;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class Dept {
    private Long deptId;
    private String deptName;
    private String location;
}
