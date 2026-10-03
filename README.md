# RevMinions

**Autor: eulopeesz** · PaperSpigot 1.8.8 · versão inicial 0.1.1.

Minions feitos de ArmorStand pequeno, com roupas, RGB, partículas, espada, holograma,
progressão por gemas e armazém individual. Esta é uma **versão para testes na host**.
Compilação e testes automatizados passaram; não houve execução em servidor real nesta entrega.

## Instalação

1. Use uma cópia do servidor para o primeiro teste.
2. Instale PlotSquared e yPlugins com o produto **ySpawnersV2 ativo**.
3. Coloque `dist/RevMinions-0.1.1.jar` em `plugins/` e reinicie.
4. No `config.yml`, troque `minions.mundos` pelo nome real do mundo de plots.
5. O modo `LOCAL` funciona sem MySQL. Dados são salvos em `plugins/RevMinions/data.yml`.
6. Dê um item: `/minions give minion SEU_NOME zumbi 1`.
7. Clique com botão direito na face de cima de um bloco do **seu próprio plot**, em espaço livre.
8. Clique no ArmorStand para abrir o menu. `/minions` lista seus minions colocados.

Se o serviço `SpawnerV2APIHolder` não registrar, o plugin aguarda e depois se desativa com diagnóstico.
PlotSquared é obrigatório; falha de verificação de plot recusa colocação e combate.
Minions trabalham apenas em chunks carregados, sem carregar chunks à força.
Por padrão, pausam quando o dono sai. Ativar trabalho offline mantém apenas chunks já carregados.

## Arquivos

| Arquivo | Configurações |
| --- | --- |
| `config.yml` | Storage/MySQL, limites, mundos, permissão, amigos, gemas, actionbar, cores e ativadores |
| `minions.yml` | Tipos, item, nome/lore, roupas, mobs, drops base e evolução |
| `menus.yml` | Títulos e textos; slots e materiais do menu principal |
| `sword.yml` | Espada, custo, dano e sete encantamentos |
| `recompensas.yml` | Recompensas extras, chance, filtro de mobs e aplicação de Fortuna |
| `messages.yml` | Chat, ajuda, reload, erros, estados e título da Horda |
| `holograms.yml` | Linhas, altura e espaçamento |

Os sete arquivos são recarregados por `/minions reload`. Layouts dos submenus são fixos nesta versão;
textos são configuráveis. Novos tipos de minion, cores, partículas e recompensas podem ser adicionados.
Não é possível adicionar uma nova lógica de encantamento apenas por YAML: os sete implementados são configuráveis.
Valores inválidos ou YAML quebrado recusam o reload. Mudança do storage/MySQL exige reinício e avisa no chat.
Reduzir níveis máximos limita os níveis salvos; faça backup antes de alterar.
Remover um tipo pausa seus minions sem apagá-los; restaure o ID para retomá-los.

## Gemas e comandos

Gemas são **inteiros não negativos**, pertencem ao jogador e valem para todos os seus minions.
Somente o dono pode evoluir minion/espada/encantamentos e coletar gemas, XP e comandos.
Amigos podem coletar itens e personalizar apenas se a configuração permitir. Por padrão,
amigos coletam itens, mas não personalizam. Adição e remoção de amigos são exclusivas do dono.
O botão de adicionar pede um nome no chat; o jogador precisa já ter entrado no servidor.
Clicar num amigo remove-o. Digite `cancelar` para sair de uma pergunta de nome/amigo.

| Comando | Acesso |
| --- | --- |
| `/minions` | `revminions.use` |
| `/minions ajuda` | Público |
| `/minions gemas` | Consulta saldo |
| `/minions gemas top` | Ranking de saldo atual, incluindo offline |
| `/minions gemas give <player> <quantidade>` | `revminions.admin` |
| `/minions gemas set <player> <quantidade>` | `revminions.admin` |
| `/minions give minion <player> <tipo> [quantidade]` | `revminions.admin` |
| `/minions give cor <player> <id> [quantidade]` | `revminions.admin` |
| `/minions give particula <player> <id> [quantidade]` | `revminions.admin` |
| `/minions give rgb <player> [quantidade]` | `revminions.admin` |
| `/minions give gemas <player> <valor> [quantidade]` | `revminions.admin` |
| `/minions reload` | `revminions.admin` |

`gemas give` credita saldo diretamente; `give gemas` entrega o item consumível.
Give de itens exige jogador online e slot livre, respeitando o tamanho máximo de uma pilha.
Give/set de saldo aceitam offline que já tenham perfil salvo.
Colocação exige `revminions.place` e a permissão do tipo em `minions.yml`.
Administradores não ignoram automaticamente mundos, plot ou limite; permissões de limite podem aumentá-lo.

## Armazém e recompensas

Tudo vai primeiro ao armazém: itens, gemas, experiência e recompensas de comandos.
Gemas só entram no saldo do dono quando ele coleta. Comandos ficam pendentes e só executam no resgate;
cada clique resgata uma entrada de comando. O exemplo de comandos vem desativado.
Itens são entregues apenas até caber no inventário, e o restante permanece no armazém.
Para recolher o minion, o armazém deve estar vazio e deve existir um slot livre.
O item recolhido mantém a identidade, dono, nível, espada, encantamentos e amigos.
Duplicar esse item não cria duas instâncias: uma identidade só pode ficar colocada uma vez.

Capacidade usa unidades combinadas: um item, gema ou ponto de XP ocupa uma unidade;
uma entrada de comando ocupa uma unidade. Antes de atacar, o minion reserva capacidade para o
pior resultado possível da próxima morte. Assim evita matar e perder uma recompensa por falta de espaço.
Se aparecer “Armazém cheio” antes de 100%, colete ou aumente a capacidade.

Recompensas extras são sorteadas **independentemente por unidade de mob abatida**.
Fortuna é aumento relativo: chance de 10% com nível 4 e 5% por nível passa a 12%, limitada a 100%.
`fortuna: false` mantém a chance original da recompensa.
Drops base e gemas por mob são configurados separadamente dos sorteios extras.

## Espada

- **Dano:** soma dano por nível.
- **Velocidade:** reduz intervalo; piso de 10% do intervalo original.
- **Pilhagem:** multiplica drops base, arredondando para cima.
- **Experiência:** aumenta XP armazenada por unidade abatida.
- **Varredura:** adiciona alvos por rodada, respeitando orçamento global de ataques.
- **Fortuna:** aumenta chance das recompensas extras marcadas.
- **Horda Zumbi:** chance por ataque de invocar zumbis bebês aliados.

A Horda respeita plot, alvo, duração, cooldown e limite global. Os zumbis não recebem dano,
não pegam itens nem têm drops. Ataques vanilla deles são cancelados; o plugin aplica dano
controlado contra o alvo selecionado. Não ativam outra Horda. Ao morrer o alvo, recolher o minion,
reload, unload de chunk ou desligar, os aliados são removidos. O título vai ao dono online.
Actionbar e títulos usam packets de 1.8 por reflexão; forks diferentes precisam de teste.

## Integrações conferidas nos arquivos enviados

- PlotSquared `18.12.12-be48507-2053`: `BukkitUtil.getLocation`, `Plot.getPlot`, `Plot.isOwner`.
- yPlugins `3.7.0`: `SpawnerV2APIHolder.getEntityStackAmount` e `setEntityStackAmount`.

O plugin usa esses serviços por reflexão, sem incluir jars comerciais no projeto.
A inspeção e o teste de assinatura confirmam os métodos disponíveis, **não confirmam comportamento de combate**.
Para pilhas com mais de uma unidade, o golpe letal consulta cancelamento de dano, remove uma unidade via API,
restaura a vida do mob representante e gera uma recompensa. Na última unidade, usa dano normal e captura a morte.
Os drops vanilla do abate creditado são suprimidos para não duplicar os configurados.

**Drops base do minion vêm de `minions.yml`; não são importados das tabelas internas do ySpawners.**
Recompensas extras vêm de `recompensas.yml`. Não há sincronização com o armazém interno do ySpawners.
A API fornecida não expõe, nesses métodos, uma identificação inequívoca da origem do mob:
o plugin filtra tipo permitido, terreno e quantidade reportada. Se a API reportar 1 para mobs comuns,
esses mobs também podem ser atacados dentro do plot. Confira esse comportamento na host.
Filtros/proteções/drops de outros addons e morte por dano controlado precisam de teste conjunto.

## Dados, segurança e backup

- LOCAL usa `data.yml` com troca atômica de arquivo.
- MYSQL usa `revminions_state`, um snapshot do servidor com perfis, minions e armazéns juntos.
- IO roda numa fila serial, fora do loop de combate. Autosave e shutdown salvam o progresso.
- Falha de leitura desativa o plugin; não cria dados vazios em cima dos existentes.
- MySQL selecionado e indisponível não muda automaticamente para LOCAL.
- Se o save final falhar, tenta criar `recovery.yml`; restauração é manual após comparar os dados.
- LOCAL/MYSQL não migram automaticamente. **Não use vários servidores escrevendo a mesma tabela.**

Faça backup do banco/`data.yml` e de `item-secret.key`.
A chave HMAC impede ativação por simples renomeação/cópia de lore visível; não publique a chave no GitHub.
Colete/itens ativadores são protegidos contra vestir, crafting e bigorna pelas interações monitoradas.
Quedas abruptas podem perder mudanças desde o último autosave. Resgates e comandos externos não têm
transação distribuída com o disco/banco: não há garantia de exatamente uma execução após crash.

## Compilar e publicar no GitHub

Requisitos: JDK 17 e Maven 3.9+. Bytecode final é Java 8.

```sh
mvn clean verify
```

Saída: `target/RevMinions-0.1.1.jar`. O ZIP já contém `dist/RevMinions-0.1.1.jar`.
Não instale o jar `original-...`. O GitHub Actions compila automaticamente.
Os jars enviados não são necessários para compilar e não são redistribuídos.
Para executar o teste opcional com suas assinaturas:

```sh
mvn verify -Drev.test.jars=/caminho/para/os/jars
```

A pasta precisa conter `yplugins-3.7.0.jar` e `PlotSquared.jar`.
Escolha uma licença antes de publicar caso queira conceder redistribuição; nenhuma foi escolhida automaticamente.
Consulte `TESTE-HOST.md` para verificar a primeira instalação.

## Correção 0.1.1

Corrigida a colocação quando `limites-por-permissao` contém chaves pontuadas,
como `revminions.limit.10`. O Bukkit 1.8 converte essas chaves em MemorySections;
o leitor agora percorre os nós e reconstrói a permissão completa, sem cast direto.
A configuração antiga é aceita sem alteração. Limites não inteiros ou não positivos
são recusados ao carregar/recarregar, antes de uma tentativa de colocação.

Atualização: pare o servidor, substitua o jar 0.1.0 por 0.1.1 e reinicie.
Mantenha a pasta `plugins/RevMinions/`, `data.yml` e `item-secret.key`.
Dados, progresso e ativadores existentes conservam o mesmo formato.
