package com.datalock.service;

import com.datalock.model.Usuario;
import com.datalock.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class Microsservico3Service {

    @Autowired
    private UsuarioRepository repo;

    public void atualizarEndereco(String usuarioId, String itemId, String endereco) {
        Usuario u = repo.findById(usuarioId).orElseThrow();
        u.getItems().forEach(it -> {
            if (it.getItemId().equals(itemId)) {
                it.setEndereco(endereco);
            }
        });

        repo.save(u);
        System.out.println("MS3 salvou endereco");
    }

    public void atualizarEnderecoOptimistic(String usuarioId, String itemId, String endereco) {
        int retries = 3;
        while (retries-- > 0) {
            try {
                Usuario u = repo.findById(usuarioId).orElseThrow();
                u.getItems().forEach(it -> {
                    if (it.getItemId().equals(itemId)) {
                        it.setEndereco(endereco);
                    }
                });
                repo.save(u);
                System.out.println("MS3 salvou endereco com sucesso");
                return; // sucesso
            } catch (OptimisticLockingFailureException e) {
                System.out.println("Conflito detectado no endereco, retry...");
                try {
                    Thread.sleep(100); // backoff simples
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Não foi possível salvar endereco após retries");
    }
}