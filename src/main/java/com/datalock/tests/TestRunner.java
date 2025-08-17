package com.datalock.tests;

import com.datalock.model.Item;
import com.datalock.model.Usuario;
import com.datalock.repositories.UsuarioRepository;
import com.datalock.service.Microsservico1Service;
import com.datalock.service.Microsservico3Service;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestRunner implements CommandLineRunner {

    private final Microsservico1Service ms1;
    private final Microsservico3Service ms3;
    private final UsuarioRepository repo;

    public TestRunner(Microsservico1Service ms1, Microsservico3Service ms3, UsuarioRepository repo) {
        this.ms1 = ms1;
        this.ms3 = ms3;
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        //Descomentar se quiser testar o teste com version e optimistic locking
        testWithVersionAndOptimisticLocking();

        //Comentar para rodar o teste acima com version e optimistic locking
        //testWithoutVersionAndOptimisticLocking();

        //Observação: quando comentar um teste e descomentar o outro, necessário apagar o registro usuario do banco
    }

    private void testWithoutVersionAndOptimisticLocking() throws InterruptedException {
        String usuarioId = "user1";
        String itemId = "item1";

        Usuario u = Usuario.builder()
                .id(usuarioId)
                .items(List.of(Item.builder().itemId(itemId).build()))
                .build();
        repo.save(u);

        Thread t1 = new Thread(() -> ms1.atualizarNomeTelefone(usuarioId, itemId, "Carlos", "9999-0000"));
        Thread t3 = new Thread(() -> ms3.atualizarEndereco(usuarioId, itemId, "Rua ABC, 123"));

        t1.start();
        t3.start();
        t1.join();
        t3.join();

        Usuario finalUser = repo.findById(usuarioId).get();
        System.out.println(finalUser);
    }

    private void testWithVersionAndOptimisticLocking() throws InterruptedException {
        String usuarioId = "user1";
        String itemId = "item1";

        Usuario u = Usuario.builder()
                .id(usuarioId)
                .items(List.of(Item.builder().itemId("other").build()))
                .build();
        repo.save(u);

        Thread t1 = new Thread(() -> ms1.atualizarNomeTelefone(usuarioId, itemId, "Carlos", "9999-0000"));
        Thread t3 = new Thread(() -> ms3.atualizarEnderecoOptimistic(usuarioId, itemId, "Rua ABC, 123"));

        t1.start();
        t3.start();
        t1.join();
        t3.join();

        Usuario finalUser = repo.findById(usuarioId).get();
        System.out.println(finalUser);
    }
}