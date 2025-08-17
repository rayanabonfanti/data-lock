package com.datalock.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    private String itemId;
    private String nome;
    private String telefone;
    private String endereco;
}