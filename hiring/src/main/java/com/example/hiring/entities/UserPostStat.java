package com.example.hiring.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPostStat {
    private String name;
    private String result;
    private Double target;
    private String status;
    private LocalDateTime dateP;
}
