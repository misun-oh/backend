package com.example.hr.jpa.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "JOB")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @Column(name = "JOB_CODE", length = 2)
    private String jobCode;

    @Column(name = "JOB_NAME", length = 35)
    private String jobName;
}
