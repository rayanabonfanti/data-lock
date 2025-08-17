package com.datalock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.util.List;

@Document(collection = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    private String id;
    private List<Item> items;
    //Descomentar para rodar o testWithVersionAndOptimisticLocking na classe TestRunner
    @Version
    private Long version;
}