package com.datalock.service;

import com.datalock.model.Item;
import com.datalock.model.Usuario;
import com.datalock.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        int maxRetries = 10; // número máximo de tentativas
        int attempt = 0;

        while (attempt++ < maxRetries) {
            try {
                Usuario u = repo.findById(usuarioId).orElseThrow();

                // Verifica se o item existe
                Optional<Item> itemOpt = u.getItems().stream()
                        .filter(it -> it.getItemId().equals(itemId))
                        .findFirst();

                if (itemOpt.isPresent()) {
                    // Item existe → atualiza endereço
                    itemOpt.get().setEndereco(endereco);
                    repo.save(u); // dispara OptimisticLockingFailureException se versão mudou
                    System.out.println("MS3 salvou endereco com sucesso");
                    return;
                } else {
                    // Item não existe → aguarda e tenta novamente
                    System.out.println("Item não encontrado, aguardando criação pelo MS1...");
                    Thread.sleep(100); // backoff simples
                }

            } catch (OptimisticLockingFailureException e) {
                System.out.println("Conflito detectado, retry...");
                try {
                    Thread.sleep(100); // backoff simples
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new RuntimeException("Não foi possível salvar endereco: item ainda não existe após retries");
    }
}