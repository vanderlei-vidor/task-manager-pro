# 🏗️ Arquitetura do Projeto

## 📦 Estrutura atual recomendada

com.example.demo

├── controller
├── service
├── repository
├── model (entity)
├── dto
├── config
├── security
├── exception
└── util

---

## 🔁 Fluxo correto

Controller → Service → Repository → Database

Nunca:
Controller → Repository direto ❌

---

## 🧠 Regras

- Entity nunca sai direto na API
- Tudo passa por Service
- DTO obrigatório para endpoints públicos