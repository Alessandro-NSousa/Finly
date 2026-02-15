# 🔐 Guia de Segurança - Finly

## Configuração de Variáveis de Ambiente

Este projeto usa **variáveis de ambiente** para proteger informações sensíveis como senhas e chaves secretas.

### ⚙️ Desenvolvimento Local

1. **Copie o arquivo de exemplo:**
   ```bash
   cd backend
   cp .env.example .env
   ```

2. **Edite o arquivo `.env` com suas credenciais:**
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finly
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=sua_senha_aqui

   JWT_SECRET=sua_chave_jwt_em_base64
   JWT_EXPIRATION=86400000
   ```

3. **O arquivo `.env` já está no `.gitignore`** e nunca será commitado.

### 🐳 Docker

O `docker-compose.yml` já configura todas as variáveis necessárias automaticamente.

### 🔑 Gerando uma Nova JWT Secret

Para produção, gere uma nova chave JWT secreta:

```bash
# Linux/Mac
echo -n "minha-chave-super-secreta-$(date +%s)" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("minha-chave-super-secreta"))
```

### ⚠️ NUNCA Commite:

- ❌ Arquivo `.env`
- ❌ Senhas no código
- ❌ Chaves API no código
- ❌ Tokens de acesso

### ✅ Pode Commitar:

- ✅ Arquivo `.env.example` (sem valores reais)
- ✅ Configurações usando `${VARIAVEL_AMBIENTE}`
- ✅ Documentação sobre variáveis necessárias

---

**Importante:** Sempre use variáveis de ambiente diferentes para desenvolvimento, staging e produção!
