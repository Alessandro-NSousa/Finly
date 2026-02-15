# Finly - Quick Start Guide

## Início Rápido com Docker 🐳

### 1. Certifique-se de ter Docker e Docker Compose instalados

### 2. Execute o comando:
```bash
docker-compose up --build
```

### 3. Acesse:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### 4. Crie sua conta e comece a usar!

## Desenvolvimento Local (sem Docker)

### Backend:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend:
```bash
cd frontend
npm install
npm start
```

### PostgreSQL:
Certifique-se de ter o PostgreSQL rodando localmente na porta 5432 com:
- Database: finly_db
- User: finly_user
- Password: finly_pass

---

Veja o README.md completo para mais detalhes!
