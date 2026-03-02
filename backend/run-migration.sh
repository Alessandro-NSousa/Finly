#!/bin/bash
# =====================================================
# Script de Migração - Linux/Mac
# Execute este arquivo para adicionar as colunas
# =====================================================

echo "Executando migração do banco de dados..."
echo ""
echo "Entre com a senha do PostgreSQL quando solicitado"
echo ""

psql -U postgres -d finly -f database-migration-add-email-verification.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "MIGRAÇÃO CONCLUÍDA COM SUCESSO!"
    echo "========================================"
    echo ""
    echo "Agora você pode:"
    echo "1. Reiniciar o backend"
    echo "2. Tentar cadastrar novamente"
    echo ""
else
    echo ""
    echo "========================================"
    echo "ERRO NA MIGRAÇÃO!"
    echo "========================================"
    echo ""
    echo "Verifique se:"
    echo "1. PostgreSQL está instalado"
    echo "2. Banco 'finly' existe"
    echo "3. Senha do usuário 'postgres' está correta"
    echo ""
fi
