# Task Manager Pro — Enterprise Project Management Solution

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java Version](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-DDD--Inspired%20%7C%20Multi--Tenant-informational)](#architecture)

Task Manager Pro é uma plataforma corporativa robusta de gerenciamento de tarefas projetada sob os mais rigorosos padrões de engenharia de software do mercado. O sistema combina uma arquitetura back-end altamente segura e isolada por conceito de **Multi-tenancy** com uma experiência de usuário (UX/UI) de nível premium que inclui quadros Kanban, calendários multidimensionais e relatórios analíticos em tempo real.

---

## 🌟 Principais Funcionalidades

### 🎮 Interface do Usuário Premium & DataViz
- **Painel Analítico:** Dashboard centralizado com estatísticas consolidadas e gráficos dinâmicos de produtividade.
- **Visualização Multidimensional:** Gestão por Quadro Kanban fluido e Visualização de Calendário tripla (Mensal, Semanal e Diária).
- **Gamificação da Produtividade:** Gráfico de Heatmap integrado ao perfil do usuário no mais puro estilo de contribuições do GitHub.
- **Produtividade Acelerada:** Atalhos de teclado globais, sistema nativo de tags contextuais, notificações em tempo real através de Toasts e Modo Escuro (Dark Mode) adaptativo.
- **Exportação de Dados B2B:** Motores dedicados de renderização de dados para relatórios em formatos **PDF (PDF-Engine)** e **Excel (Apache POI Engine)**.

### 🔒 Segurança & Isolamento Corporativo (Enterprise-Grade)
- **Stateful-to-Stateless Hybrid Auth:** Autenticação de APIs controlada por tokens JWT com tempo de vida curto (Access Token: 15min).
- **Mecanismo de Resiliência de Sessão:** Sistema nativo de **Refresh Token (7 dias)** com rotação automatizada (*Token Rotation*) para prevenção de ataques de replay e rotina agendada cronometrada para expurgo de tokens obsoletos (*Automatic Token Cleanup*).
- **Isolamento de Dados (Multi-tenancy):** Camada de dados blindada logicamente por identificadores de usuário, garantindo que um locatário jamais acesse ou infira dados de outro.

---

## 🏗️ Arquitetura de Software & Design Patterns

O projeto foi estruturado seguindo os princípios de alta coesão e baixo acoplamento, dividindo-se de forma clara em responsabilidades específicas:

└─ src/main/java/com/example/demo/
├─ config/       # Centralização de propriedades tipadas (ApplicationProperties) e Segurança
├─ controller/   # Controllers desacoplados para APIs RESTful e renderização de Páginas (Thymeleaf)
├─ dto/          # Objetos imutáveis para transferência de dados nas bordas da aplicação
├─ exception/    # Tratamento global de erros baseado em Domínios e Enums de Causa (ReasonType)
├─ model/        # Entidades ricas mapeadas via JPA (PostgreSQL Domain)
├─ repository/   # Camada de persistência otimizada com tratamento transacional atômico
└─ service/      # Orquestração do domínio de negócios e engines de infraestrutura (PDF, Excel, JWT)


### 💎 Padrões de Projeto & Tecnologias Adotadas
* **MapStruct:** Mapeamento de tipos em tempo de compilação, eliminando código manual de conversão de dados (Mappers) e garantindo performance superior frente à reflexão.
* **Lombok:** Utilização de `@Builder`, `@Getter` e construtores customizados para manter os modelos e DTOs limpos e legíveis.
* **Domain Exception Pattern:** Exceções de negócio encapsuladas (`InvalidTokenException`, `NotFoundException`) que guiam respostas HTTP limpas através de barramentos de tratamento globais.

---

## 🛠️ Stack Tecnológico

### Back-End Core
- **Linguagem Principal:** Java 17
- **Framework Base:** Spring Boot 3.x (com Spring Security)
- **Mapeador de Objetos:** MapStruct 1.5+
- **Camada de Persistência:** Spring Data JPA / Hibernate

### Armazenamento & Banco de Dados
- **Banco de Produção:** PostgreSQL

### Front-End & Relatórios
- **Engine de Renderização:** Thymeleaf / HTML5 & CSS Intermediário (Design System baseado em Variáveis Nativas)
- **Engines de Exportação:** Apache POI (Excel) / OpenPDF (PDF)

---

## 🚀 Status do Projeto & Matriz de Maturidade

O Task Manager Pro encontra-se atualmente em **70% de sua completude total**, operando com as fases centrais estabilizadas.

| Dimensão / Fase | Status | Descrição Técnica |
| :--- | :---: | :--- |
| **Fase 0: Base & DTOs** | `100% ✅` | Modelagem de DTOs concluída, exceções tipadas e centralização de propriedades. |
| **Fase 1: Arquitetura Core** | `100% ✅` | Serviços desacoplados, Repositories otimizados e Mappers MapStruct funcionais. |
| **Fase 2: Segurança Enterprise** | `60% ⚠️` | Autenticação, Rotação de Refresh Token e Multi-tenancy ativos. *Pendente: CSP, HSTS, Rate Limiting.* |
| **Fase 3: Banco de Dados** | `40% ⚠️` | Queries otimizadas e relacionamentos OK. *Pendente: Migrações Flyway e Soft Delete.* |
| **Fase 6: UX/UI Design** | `95% ✅` | Dashboard, Kanban, Relatórios, Atalhos de Teclado e Gráficos funcionando. |
| **Fase 7: Performance** | `30% ⚠️` | Queries estruturadas. *Pendente: Paginação e Cache via Redis.* |
| **Fases 4, 5, 8, 9 (Testes/DevOps)** | `0% ❌` | Planejado nos próximos sprints do Roadmap Tecnológico. |

---

## ⚙️ Pré-requisitos e Execução Local

### Pré-requisitos Mínimos
- **Java JDK 17** devidamente configurado nas variáveis de ambiente.
- Instância ativa do **PostgreSQL**.








