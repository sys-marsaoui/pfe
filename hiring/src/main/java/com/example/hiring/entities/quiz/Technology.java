package com.example.hiring.entities.quiz;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Technology {
    @Id
    public String id;
    public String name;
    public String version;
}
