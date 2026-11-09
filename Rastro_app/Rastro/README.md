# Rastro: Diário de Trilhas e Pescarias

Trabalho A1 de Desenvolvimento de Aplicativos Móveis (Universidade Positivo).
Android, Kotlin, Jetpack Compose, Room, Retrofit e Open-Meteo.

## Como abrir e rodar

1. Android Studio: File > Open e selecione a pasta `Rastro` (a que contém `settings.gradle.kts`).
2. Aguarde o Gradle sincronizar. Na primeira vez ele baixa as dependências (precisa de internet).
3. Crie ou escolha um emulador com imagem **Google Play** (API 26 ou superior) para ter o Google Maps.
4. Clique em Run.

Versões usadas: AGP 8.2.2, Gradle 8.7, Kotlin 1.9.22, Compose BOM 2024.02.00, Room 2.6.1,
compileSdk 34, minSdk 26, Java 17. Funciona no Android Studio Hedgehog (2023.1) ou mais novo.
Se a IDE da faculdade sugerir atualizar o AGP, **não aceite** sem testar antes.

## Verificação do enunciado, item por item

| Item do enunciado | Como o app atende | Onde está no código |
|---|---|---|
| Android em Kotlin com Jetpack Compose | Todas as telas são `@Composable`; nenhum XML de layout | `ui/` |
| Utilização da camada de abstração Room | Entidades, DAOs e `RoomDatabase` singleton | `data/local/` |
| CRUD (Create, Read, Update, Delete) | `@Insert`, `@Query`, `@Update`, `@Delete` nas duas tabelas, todos acessíveis pela interface | `LocalDao.kt`, `SaidaDao.kt` |
| Pelo menos duas tabelas | `locais` e `saidas`, relação 1:N com chave estrangeira e `CASCADE`. Há ainda `clima_cache` (auxiliar) | `Local.kt`, `Saida.kt` |
| Consumir ao menos uma API | Open-Meteo: previsão do tempo (forecast) e busca de cidades (geocoding) via Retrofit | `data/remote/OpenMeteoApi.kt` |
| Compartilhar dado com app instalado | `ACTION_SEND` envia o resumo da saída ou do local (WhatsApp, Gmail, Mensagens...); `ACTION_VIEW` com `geo:` abre o Google Maps | `util/Compartilhar.kt` |
| Funcionar no emulador da IDE | Sem dependência de GPS, câmera ou Google Play Services; só precisa de internet | `AndroidManifest.xml` |

### CRUD pela interface (o que demonstrar na defesa)

| Operação | Locais | Saídas |
|---|---|---|
| Create | Botão "Novo local" | Botão "Registrar saída" no detalhe do local |
| Read | Lista inicial (com filtro) e tela de detalhe | Lista dentro do detalhe do local |
| Update | Ícone de lápis no topo do detalhe | Ícone de lápis em cada saída |
| Delete | Ícone de lixeira no topo do detalhe (com confirmação) | Ícone de lixeira em cada saída (com confirmação) |

### Critérios avaliativos e o que cobre cada um

- **CRUD (1,5):** CRUD completo nas duas tabelas, com validação de campos obrigatórios.
- **Room (1,0):** entidades com `@Entity`, chave estrangeira, índice, DAOs com `Flow` (a tela atualiza sozinha), consulta com `LEFT JOIN` e `COUNT`.
- **API (1,0):** os dados são usados de verdade: a geocoding fornece as coordenadas que o banco salva, e a previsão aparece no detalhe (agora + 7 dias). Se a internet cair, mostra a última previsão salva no Room.
- **Compartilhamento (0,5):** dois tipos de Intent implícita (texto e mapa).
- **Interface (1,0):** Material 3, estados de carregamento, vazio e erro, diálogos de confirmação, tema claro e escuro. Estado preservado ao girar a tela (ViewModel e `rememberSaveable`), item citado na rubrica como "perda de estado".

## Arquitetura

```
UI (Compose)  ->  ViewModel  ->  Repository  ->  Room (DAOs)
                                             ->  Retrofit (Open-Meteo)
```

```
br.edu.up.rastro
├── RastroApp.kt              Application: cria banco e repositório uma vez
├── MainActivity.kt           Activity única, chama o NavHost
├── data/
│   ├── RastroRepository.kt   Único ponto de acesso a dados
│   ├── local/                Room: entidades, DAOs, AppDatabase
│   └── remote/               Retrofit: interface da API e modelos JSON
├── ui/
│   ├── AppNavHost.kt         Rotas e navegação
│   ├── AppViewModelProvider.kt  Fábrica dos ViewModels
│   ├── locais/               Lista de locais
│   ├── localform/            Cadastro e edição de local (com busca de cidade)
│   ├── detalhe/              Detalhe: clima, mapa, compartilhar, saídas
│   ├── saidaform/            Cadastro e edição de saída
│   └── theme/                Cores e tema
└── util/                     Formatação, clima (códigos WMO) e compartilhamento
```

## Roteiro de demonstração (cerca de 3 minutos)

1. Lista vazia: mostrar o estado vazio.
2. Novo local: nome "Pico do Marumbi", tipo Trilha, buscar "Morretes", selecionar, salvar.
3. Abrir o local: mostrar a previsão vinda da API.
4. Registrar duas saídas; editar uma; excluir outra.
5. Compartilhar uma saída (escolher Mensagens ou Gmail no emulador).
6. Abrir no Maps.
7. Editar o local; voltar à lista e mostrar o contador de saídas e o filtro.
8. Girar o emulador com um formulário preenchido: nada se perde.
9. Excluir o local e explicar o `CASCADE`.

## Perguntas prováveis na defesa (estudem todos)

A nota individual é multiplicada pelo FER. Cada membro deve saber responder:

1. **O que é uma `@Entity` e o que faz `autoGenerate = true`?** Classe que vira tabela; o id é gerado pelo SQLite.
2. **Como as tabelas se relacionam?** `Saida.localId` é chave estrangeira para `Local.id`; `onDelete = CASCADE` apaga as saídas junto com o local.
3. **Por que os DAOs de leitura retornam `Flow` e os de escrita são `suspend`?** `Flow` emite de novo a cada mudança no banco (a tela se atualiza sozinha); `suspend` roda fora da thread principal, e o Room proíbe acesso ao banco na thread principal.
4. **O que faz o `LEFT JOIN` em `listarComTotal`?** Conta as saídas de cada local, incluindo locais com zero saídas.
5. **Para que serve o Repository?** Isola a origem dos dados: o ViewModel não sabe se veio do Room ou da API.
6. **Para que serve o ViewModel?** Guarda o estado da tela e sobrevive à rotação; executa coroutines no `viewModelScope`.
7. **Como o Retrofit funciona aqui?** Interface com `@GET` e `@Query`; o Retrofit gera a implementação e o Gson converte o JSON nas data classes (`@SerializedName` mapeia nomes como `temperature_2m`).
8. **O que acontece se a internet cair?** O repositório captura a exceção e devolve a última previsão salva na tabela `clima_cache`; sem cache, a tela mostra erro com botão de tentar de novo.
9. **Como funciona o compartilhamento?** Intent implícita `ACTION_SEND` com `text/plain`; o Android mostra os apps que aceitam texto. O Maps usa `ACTION_VIEW` com URI `geo:`.
10. **Como os argumentos chegam às telas?** Pela rota de navegação (`detalhe/{localId}`); o ViewModel lê via `SavedStateHandle`.
11. **Por que `collectAsStateWithLifecycle`?** Converte o `Flow` em estado do Compose e para de coletar quando a tela não está visível.
12. **O que é recomposição?** Quando um estado lido por um `@Composable` muda, o Compose redesenha só aquela parte.
13. **Por que a data é salva em UTC?** O `DatePicker` do Material 3 devolve meia-noite UTC; formatar em UTC evita mostrar o dia anterior no fuso de Brasília.

## Checklist antes da defesa

- [ ] Testar na máquina da faculdade (versão do Android Studio e do emulador).
- [ ] Levar o projeto no GitHub e em um pen drive.
- [ ] Ter dados de exemplo já cadastrados, mas mostrar o cadastro ao vivo.
- [ ] Confirmar que o emulador tem internet antes de começar.
- [ ] Cada membro explica uma camada diferente e consegue explicar as outras.
