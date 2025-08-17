# Projeto Controle de Concorrência com Optimistic Locking

## Descrição

Este projeto demonstra como gerenciar concorrência em updates de listas dentro de um documento único no MongoDB usando **Optimistic Locking (@Version)** em conjunto com **retry/backoff**.

O cenário simula três microsserviços:

1. **MS1:** cria itens e atualiza dados como nome e telefone.
3. **MS3:** consome eventos para atualizar atributos adicionais, como endereço.

O documento principal (`Usuario`) contém uma lista de objetos (`Item`).

---

## Registro de Decisão de Arquitetura (ADR 001) — Controle de concorrência para updates de lista em documento único

**Status:** Em andamento

### Contexto
- Documento único por id (`Usuario`) contém uma lista de itens (`Item`).
- MS1 cria e atualiza dados como nome e telefone, adiciona IDs de itens à lista.
- MS3 atualiza atributos adicionais (endereco).
- Em casos raros, MS3 pode processar duas mensagens paralelas que afetam o mesmo documento, causando *lost updates*.

### Decisão
- Adotar **Optimistic Locking** no documento raiz (`@Version`).
- Complementar com:
    - Retry com backoff controlado.
    - DLQ para mensagens que excederem o limite de tentativas.
    - Métricas e observabilidade para monitorar conflitos e DLQ.

### Justificativa
- Conflitos são raros → **optimista é mais eficiente** que pessimista.
- Optimistic + retries garante integridade dos dados.
- DLQ + observabilidade asseguram resolução final.

### Plano de Migração / Próximos Passos
1. Implementar `@Version` no documento raiz.
2. Implementar retry/backoff.
3. Configurar DLQ.
4. Instrumentar métricas e dashboards.
5. Testar carga e calibrar `MAX_RETRIES`.
6. Se conflitos se tornarem frequentes → avaliar **item-por-documento** (refatoração futura).  

### Estrutura de Dados
```java
@Document(collection = "usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    private String id;

//    @Version
//    private Long version;

    private List<Item> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String itemId;
    private String nome;
    private String telefone;
    private String endereco;
}
```

### Cenários de Teste

#### 1️⃣ Sem Version / Optimistic Locking
```java
MS1 salvou nome e telefone
MS3 salvou endereco
Usuario(
    id=user1, 
    items=[
        Item(
            itemId=item1, 
            nome=null,       // perdeu alteração do MS1
            telefone=null,   // perdeu alteração do MS1
            endereco=Rua ABC, 123
        )
    ]
)
```
❌ Problema: MS3 sobrescreveu os dados do MS1, resultando em lost update.


#### 2️⃣ Com Version / Optimistic Locking e Retry
```java
MS1 salvou nome e telefone
Conflito detectado no endereco, retry...
MS3 salvou endereco com sucesso
Usuario(
    id=user1, 
    items=[
        Item(
            itemId=item1, 
            nome=Carlos, 
            telefone=9999-0000, 
            endereco=Rua ABC, 123
        )
    ],
    version=2
)
```
✅ Resultado correto: ambas atualizações foram mergeadas e persistidas sem perda de dados.

💡 A validação do if-else garante que MS3 só atualize um item existente e, combinada com retry/backoff, resolve problemas de race conditional.

## Conclusão

A combinação de **Optimistic Locking + retry/backoff + DLQ + métricas** garante:

- Consistência de dados.
- Escalabilidade do MS3.
- Operação segura mesmo em cenários raros de concorrência.