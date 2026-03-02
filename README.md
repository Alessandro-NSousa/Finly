# 💰 Finly - Sistema de Controle de Finanças Pessoais

**Finly - Dê um fim no descontrole.**

Sistema completo de gerenciamento de finanças pessoais desenvolvido com Spring Boot, Angular e PostgreSQL.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Stack Tecnológico](#stack-tecnológico)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Execução](#instalação-e-execução)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [API Documentation](#api-documentation)
- [Uso](#uso)

## 🎯 Sobre o Projeto

O Finly é um sistema de controle de finanças pessoais que auxilia o usuário a gerenciar suas dívidas mensais com base na renda informada. O sistema permite o controle de gastos fixos, variáveis e parcelados, além de oferecer um dashboard com recomendações inteligentes baseadas na regra 50/30/20.

## ✨ Funcionalidades

### Gestão de Usuários
- ✅ Cadastro de usuários com renda mensal
- ✅ Validação de email obrigatória
- ✅ Ativação de conta por email
- ✅ Autenticação JWT
- ✅ Reenvio de email de ativação
- ✅ Atualização de renda mensal

### Gestão de Dívidas
- ✅ Cadastro de dívidas (fixas, variáveis e parceladas)
- ✅ Categorização de dívidas (Moradia, Transporte, Alimentação, etc.)
- ✅ Status de dívidas (Paga, Em aberto, Atrasada)
- ✅ Replicação automática de dívidas fixas
- ✅ Parcelamento automático com controle individual
- ✅ Visualização mensal de dívidas
- ✅ Cálculo automático de totais

### Dashboard Financeiro
- ✅ Recomendações baseadas na regra 50/30/20
- ✅ Comparação entre situação atual e ideal
- ✅ Percentual da renda comprometida
- ✅ Alertas de orçamento excedido
- ✅ Detalhamento por tipo de despesa

## 🛠️ Stack Tecnológico

### Backend
- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Security** (JWT)
- **Spring Data JPA**
- **PostgreSQL**
- **Swagger/OpenAPI**
- **Lombok**

### Frontend
- **Angular 17**
- **TypeScript**
- **RxJS**
- **HttpClient**

### DevOps
- **Docker**
- **Docker Compose**
- **Maven**
- **npm**

## 📦 Pré-requisitos

### Opção 1: Com Docker (Recomendado)
- Docker
- Docker Compose

### Opção 2: Sem Docker
- Java 21 (JDK)
- Maven 3.8+
- Node.js 18+
- npm
- PostgreSQL 16

## 🚀 Instalação e Execução

### Opção 1: Usando Docker Compose (Recomendado)

1. Clone o repositório:
```bash
git clone <repository-url>
cd finly
```

2. Execute com Docker Compose:
```bash
docker-compose up --build
```

3. Acesse as aplicações:
- **Frontend**: http://localhost:4200
- **Backend**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **PostgreSQL**: localhost:5432

### Opção 2: Execução Manual

#### Backend

1. Configure o PostgreSQL:
```sql
CREATE DATABASE finly_db;
CREATE USER finly_user WITH PASSWORD 'finly_pass';
GRANT ALL PRIVILEGES ON DATABASE finly_db TO finly_user;
```

2. Execute o backend:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

O backend estará disponível em: http://localhost:8080

#### Frontend

1. Instale as dependências:
```bash
cd frontend
npm install
```

2. Execute o servidor de desenvolvimento:
```bash
npm start
```

O frontend estará disponível em: http://localhost:4200

## 📂 Estrutura do Projeto

```
finly/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finly/
│   │   │   │   ├── config/         # Configurações (Security, Swagger)
│   │   │   │   ├── controller/     # REST Controllers
│   │   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   ├── model/          # Entidades JPA
│   │   │   │   ├── repository/     # Repositórios JPA
│   │   │   │   ├── security/       # JWT e Security
│   │   │   │   └── service/        # Lógica de Negócio
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/        # Componentes Angular
│   │   │   ├── guards/            # Route Guards
│   │   │   ├── interceptors/      # HTTP Interceptors
│   │   │   ├── models/            # Interfaces TypeScript
│   │   │   └── services/          # Serviços HTTP
│   │   ├── assets/
│   │   └── environments/
│   ├── angular.json
│   ├── package.json
│   └── Dockerfile
└── docker-compose.yml
```

## 📖 API Documentation

Após iniciar o backend, acesse a documentação Swagger:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/api-docs

### Principais Endpoints

#### Autenticação
- `POST /api/auth/register` - Registrar novo usuário (envia email de ativação)
- `POST /api/auth/login` - Fazer login (requer conta ativada)
- `GET /api/auth/activate?token={token}` - Ativar conta com token do email
- `POST /api/auth/resend-verification` - Reenviar email de verificação

#### Usuários
- `GET /api/users/me` - Obter usuário atual
- `PUT /api/users/income` - Atualizar renda mensal

#### Dívidas
- `POST /api/debts` - Criar nova dívida
- `GET /api/debts` - Listar dívidas por mês/ano
- `PUT /api/debts/{id}/status` - Atualizar status
- `DELETE /api/debts/{id}` - Deletar dívida
- `GET /api/debts/report` - Relatório mensal

#### Dashboard
- `GET /api/dashboard` - Dashboard financeiro com recomendações

## 💡 Uso

### 1. Criar Conta
Acesse  (válido e único)
- Senha
- Renda mensal

Após o cadastro, você receberá um email com link de ativação. Clique no link para ativar sua conta e poder fazer login.

**Observação**: O sistema requer ativação de conta por email. Certifique-se de configurar corretamente as variáveis de ambiente SMTP.
- Senha
- Renda mensal

### 2. Dashboard
Após o login, visualize seu dashboard financeiro com:
- Saldo disponível
- Despesas fixas e variáveis
- Percentual de poupança
- Recomendações baseadas na regra 50/30/20

### 3. Gerenciar Dívidas
Na seção "Dívidas":
- Crie novas dívidas (fixas, variáveis ou parceladas)
- Marque dívidas como pagas
- Visualize relatórios mensais
- Navegue entre meses

### 4. Dívidas Fixas
Dívidas marcadas como "fixas" são automaticamente replicadas para os próximos meses.

### 5. Dívidas Parceladas
Ao criar uma dívida parcelada, o sistema gera automaticamente todas as parcelas distribuídas nos meses correspondentes.
Ativação de conta obrigatória por email
- Validação de email única e verificada
- Tokens de ativação com expiração de 24 horas
- 
## 🔐 Segurança

- Autenticação JWT
- Senhas criptografadas com BCrypt
- Proteção CSRF desabilitada (API Stateless)
- CORS configurado para frontend
- Cada usuário acessa apenas seus próprios dados

## 🎨 Regra 50/30/20

O sistema utiliza a regra 50/30/20 para recomendações financeiras:
- **50%** da renda para despesas fixas (essenciais)
- **30%** para despesas variáveis (lazer, etc.)
- **20%** para poupança e investimentos

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/finly_db
spring.datasource.username=finly_user
spring.datasource.password=finly_pass

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Email (SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu_email@gmail.com
spring.mail.password=sua_senha_de_aplicativo
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Application URL (para links de ativação)
app.base.url=http://localhost:4200
```

**Configuração de Email**: Para usar Gmail, você precisa gerar uma "Senha de App":
1. Acesse https://myaccount.google.com/security
2. Ative a verificação em duas etapas
3. Gere uma senha de app em "Senhas de app"
4. Use essa senha na variável `spring.mail.password`

### Backend (.env)
Alternativamente, você pode usar um arquivo `.env` na raiz do backend:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finly_db
SPRING_DATASOURCE_USERNAME=finly_user
SPRING_DATASOURCE_PASSWORD=finly_pass
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=sua_senha_de_aplicativo
APP_BASE_URL=http://localhost:42=jdbc:postgresql://localhost:5432/finly_db
spring.datasource.username=finly_user
spring.datasource.password=finly_pass
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Frontend (environment.ts)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.

## 👥 Autores

- Desenvolvedor - Sistema Finly

## 🐛 Problemas Conhecidos

Nenhum problema crítico no momento.

## 🔮 Roadmap

- [ ] Integração com contas bancárias
- [ ] Exportação de relatórios em PDF
- [ ] Alertas por e-mail
- [ ] Gráficos interativos
- [ ] App mobile
- [ ] IA para recomendações personalizadas

---

**Finly** - Dê um fim no descontrole financeiro! 💰
