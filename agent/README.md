# SchéMusic — Agente Especialista de Documentação Interna

Agente Python que responde perguntas sobre tópicos **não disponíveis publicamente** — especificamente a documentação interna e confidencial da API SchéMusic — sem alucinar.

## Objetivo

Este agente implementa um padrão de **RAG (Retrieval-Augmented Generation)** simples: em vez de depender do conhecimento geral do LLM (que pode estar desatualizado ou simplesmente não existir para documentos privados), o agente **injeta a base de conhecimento local** como contexto no prompt, garantindo que todas as respostas venham exclusivamente dessa fonte.

**Regras da sessão:**

- O agente responde **somente** com base na documentação interna fornecida em `knowledge_base/schemusic_internal_spec.md`.
- Se a informação não estiver na documentação, o agente informa que não sabe (sem alucinar).
- A sessão é limitada a **3 respostas**.
- Ao término da 3ª resposta, um **resumo automático** de toda a conversa é gerado e exibido, e a sessão é encerrada.

## Estrutura

```
agent/
├── schemusic_agent.py              # Implementação do agente e CLI
├── test_agent.py                   # Testes unitários (sem chamadas reais à API)
├── requirements.txt                # Dependências Python
├── .env.example                    # Modelo de variáveis de ambiente
└── knowledge_base/
    └── schemusic_internal_spec.md  # Documentação interna confidencial
```

## Pré-requisitos

- Python 3.10+
- Uma [chave de API OpenAI](https://platform.openai.com/api-keys)

## Instalação

```bash
cd agent

# Instale as dependências
pip install -r requirements.txt

# Configure a chave de API
cp .env.example .env
# Edite .env e insira sua OPENAI_API_KEY
```

## Uso

```bash
# Carregue as variáveis de ambiente
export OPENAI_API_KEY="sk-..."

# Execute o agente interativo
python schemusic_agent.py
```

Exemplo de sessão:

```
╔══════════════════════════════════════════════════════════╗
║         SchéMusic — Agente de Documentação Interna       ║
║  Especialista em tópicos CONFIDENCIAIS da API SchéMusic  ║
║                                                          ║
║  • Responde apenas com base na documentação interna.     ║
║  • Não alucina: se não souber, diz que não sabe.         ║
║  • Sessão limitada a 3 respostas + resumo automático.    ║
╚══════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────
  Pergunta 1 de 3
──────────────────────────────────────────────────────────────
Você: Quais entidades existem no banco de dados?

🤖 Agente: O banco de dados da SchéMusic possui três tabelas principais: ...

[2 pergunta(s) restante(s) nesta sessão]

──────────────────────────────────────────────────────────────
  Pergunta 2 de 3
──────────────────────────────────────────────────────────────
Você: Como funciona a autenticação JWT?
...

  Pergunta 3 de 3
──────────────────────────────────────────────────────────────
Você: O que está planejado para a versão v1.1?

🤖 Agente: Para a versão v1.1, estão planejados: ...

────────────────────────────────────────────────────────────
📋 RESUMO DA SESSÃO
────────────────────────────────────────────────────────────
Nesta sessão foram abordados três tópicos: ...
────────────────────────────────────────────────────────────
✅ Sessão encerrada. Todas as 3 respostas foram fornecidas.
```

## Testes

```bash
pip install pytest
pytest test_agent.py -v
```

Os testes são completamente unitários — utilizam mocks para simular as respostas da OpenAI, não sendo necessária uma chave de API real.

## Como funciona

```
Pergunta do usuário
       │
       ▼
┌─────────────────────────────┐
│ SchemusicAgent.ask()        │
│                             │
│  1. Monta o system prompt   │
│     injetando a base de     │
│     conhecimento interna    │
│                             │
│  2. Chama o LLM com         │
│     temperatura = 0.1       │
│     (mínima alucinação)     │
│                             │
│  3. Incrementa contador     │
│     de respostas            │
│                             │
│  4. Se foi a 3ª resposta:   │
│     gera resumo + encerra   │
└─────────────────────────────┘
       │
       ▼
  Resposta + indicador
  de fim de sessão
```

## Personalização

Para adaptar o agente a outro domínio privado:

1. Substitua o conteúdo de `knowledge_base/schemusic_internal_spec.md` pela sua documentação interna.
2. Ajuste as mensagens do `SYSTEM_PROMPT_TEMPLATE` em `schemusic_agent.py` conforme o novo domínio.
3. Altere `MAX_RESPONSES` se quiser um número diferente de respostas por sessão.
