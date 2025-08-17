package com.datalock.service;

import com.datalock.model.Item;
import com.datalock.model.Usuario;
import com.datalock.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Microsservico1Service {

    @Autowired
    private UsuarioRepository repo;

    public void atualizarNomeTelefone(String usuarioId, String itemId, String nome, String telefone) {
        Usuario u = repo.findById(usuarioId).orElseThrow();

        u.setItems((List.of(Item.builder()
                .itemId(itemId)
                .nome(nome)
                .telefone(telefone)
                .build())));

        repo.save(u);
        System.out.println("MS1 salvou nome e telefone");
    }
}