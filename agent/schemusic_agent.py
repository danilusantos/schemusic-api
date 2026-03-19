"""
SchéMusic Internal Expert Agent
================================
Agente especialista na documentação interna da API SchéMusic.
Responde sem alucinar usando RAG (base de conhecimento local privada).

Regras do agente:
  - Responde APENAS com base na documentação interna fornecida.
  - Se a informação não estiver na documentação, informa que não sabe.
  - Aceita no máximo 3 perguntas por sessão.
  - Ao término da 3ª resposta, exibe um resumo de tudo que foi discutido e encerra.
"""

from __future__ import annotations

import os
import sys
import textwrap
from pathlib import Path
from typing import Optional

try:
    from openai import OpenAI
except ImportError:
    sys.exit(
        "Dependência 'openai' não encontrada.\n"
        "Execute: pip install -r requirements.txt"
    )


# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

MAX_RESPONSES = 3
KNOWLEDGE_BASE_PATH = Path(__file__).parent / "knowledge_base" / "schemusic_internal_spec.md"

SYSTEM_PROMPT_TEMPLATE = """
Você é um agente especialista na documentação INTERNA e CONFIDENCIAL da API SchéMusic.
Essa documentação NÃO está disponível publicamente na internet.

REGRAS OBRIGATÓRIAS:
1. Responda APENAS com base no conteúdo da documentação interna fornecida abaixo.
2. Se a pergunta não puder ser respondida com o conteúdo da documentação, responda:
   "Essa informação não está disponível na documentação interna que tenho acesso."
3. NÃO invente, suponha ou alucine informações que não estejam explicitamente na documentação.
4. Seja objetivo, técnico e preciso.
5. Responda em português do Brasil.

=== DOCUMENTAÇÃO INTERNA SCHEMUSIC (CONFIDENCIAL) ===
{knowledge_base}
=== FIM DA DOCUMENTAÇÃO ===
"""

SUMMARY_PROMPT = """
Com base APENAS nas perguntas e respostas trocadas nesta sessão (listadas abaixo), gere um
RESUMO CONCISO E ESTRUTURADO dos tópicos discutidos, destacando os pontos mais importantes.
Responda em português do Brasil. Não adicione informações além do que foi discutido.

Histórico da sessão:
{history}
"""


# ---------------------------------------------------------------------------
# Agent
# ---------------------------------------------------------------------------


class SchemusicAgent:
    """Agente RAG especialista na documentação interna do SchéMusic."""

    def __init__(
        self,
        knowledge_base_path: Path = KNOWLEDGE_BASE_PATH,
        model: str = "gpt-4o-mini",
        api_key: Optional[str] = None,
    ) -> None:
        resolved_key = api_key or os.environ.get("OPENAI_API_KEY")
        if not resolved_key:
            raise EnvironmentError(
                "A variável de ambiente OPENAI_API_KEY não está definida.\n"
                "Configure-a no arquivo .env ou exporte-a no shell:\n"
                "  export OPENAI_API_KEY='sk-...'"
            )

        self.client = OpenAI(api_key=resolved_key)
        self.model = model
        self.knowledge_base = self._load_knowledge_base(knowledge_base_path)
        self.conversation_history: list[dict[str, str]] = []
        self.response_count = 0

    # ------------------------------------------------------------------
    # Private helpers
    # ------------------------------------------------------------------

    def _load_knowledge_base(self, path: Path) -> str:
        if not path.exists():
            raise FileNotFoundError(
                f"Base de conhecimento não encontrada: {path}\n"
                "Certifique-se de que o arquivo existe antes de executar o agente."
            )
        return path.read_text(encoding="utf-8")

    def _build_system_prompt(self) -> str:
        return SYSTEM_PROMPT_TEMPLATE.format(knowledge_base=self.knowledge_base)

    def _call_llm(self, messages: list[dict[str, str]]) -> str:
        response = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            temperature=0.1,  # baixa temperatura = menos alucinação
            max_tokens=1024,
        )
        return response.choices[0].message.content.strip()

    def _generate_summary(self) -> str:
        """Gera um resumo estruturado de toda a conversa."""
        history_text = "\n\n".join(
            f"{'Pergunta' if msg['role'] == 'user' else 'Resposta'}: {msg['content']}"
            for msg in self.conversation_history
        )
        summary_messages = [
            {"role": "system", "content": self._build_system_prompt()},
            {
                "role": "user",
                "content": SUMMARY_PROMPT.format(history=history_text),
            },
        ]
        return self._call_llm(summary_messages)

    # ------------------------------------------------------------------
    # Public interface
    # ------------------------------------------------------------------

    @property
    def session_ended(self) -> bool:
        """True quando o limite de respostas já foi atingido."""
        return self.response_count >= MAX_RESPONSES

    def ask(self, question: str) -> tuple[str, bool]:
        """
        Envia uma pergunta ao agente.

        Returns
        -------
        answer : str
            Resposta do agente para a pergunta.
        session_ended : bool
            True se esta foi a última resposta permitida (3ª).
            Quando True, o campo `answer` já inclui o resumo da sessão ao final.
        """
        if self.session_ended:
            raise RuntimeError(
                "A sessão já foi encerrada após 3 respostas. Inicie uma nova instância."
            )

        messages = [{"role": "system", "content": self._build_system_prompt()}]
        messages.extend(self.conversation_history)
        messages.append({"role": "user", "content": question})

        answer = self._call_llm(messages)

        # Persist turn in history
        self.conversation_history.append({"role": "user", "content": question})
        self.conversation_history.append({"role": "assistant", "content": answer})
        self.response_count += 1

        if self.session_ended:
            summary = self._generate_summary()
            full_response = (
                f"{answer}\n\n"
                f"{'─' * 60}\n"
                f"📋 RESUMO DA SESSÃO\n"
                f"{'─' * 60}\n"
                f"{summary}\n"
                f"{'─' * 60}\n"
                f"✅ Sessão encerrada. Todas as 3 respostas foram fornecidas."
            )
            return full_response, True

        return answer, False


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------


def _print_banner() -> None:
    banner = textwrap.dedent(
        """
        ╔══════════════════════════════════════════════════════════╗
        ║         SchéMusic — Agente de Documentação Interna       ║
        ║  Especialista em tópicos CONFIDENCIAIS da API SchéMusic  ║
        ║                                                          ║
        ║  • Responde apenas com base na documentação interna.     ║
        ║  • Não alucina: se não souber, diz que não sabe.         ║
        ║  • Sessão limitada a 3 respostas + resumo automático.    ║
        ╚══════════════════════════════════════════════════════════╝
        """
    )
    print(banner)


def _print_separator(label: str = "") -> None:
    if label:
        print(f"\n{'─' * 60}")
        print(f"  {label}")
        print(f"{'─' * 60}")
    else:
        print(f"\n{'─' * 60}\n")


def run_cli() -> None:
    """Executa o agente em modo interativo no terminal."""
    _print_banner()

    try:
        agent = SchemusicAgent()
    except (EnvironmentError, FileNotFoundError) as exc:
        print(f"❌ Erro ao inicializar o agente:\n{exc}")
        sys.exit(1)

    remaining = MAX_RESPONSES

    while not agent.session_ended:
        _print_separator(f"Pergunta {agent.response_count + 1} de {MAX_RESPONSES}")
        try:
            question = input("Você: ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\n\n⚠️  Entrada interrompida. Encerrando sessão.")
            sys.exit(0)

        if not question:
            print("⚠️  Por favor, digite uma pergunta.")
            continue

        print("\n🤖 Agente: ", end="", flush=True)
        try:
            answer, ended = agent.ask(question)
        except (RuntimeError, EnvironmentError) as exc:
            print(f"\n❌ Erro interno do agente: {exc}")
            sys.exit(1)
        except Exception as exc:  # openai errors and unexpected failures
            print(f"\n❌ Erro ao chamar a API: {type(exc).__name__}: {exc}")
            sys.exit(1)

        print(answer)
        remaining -= 1

        if not ended and remaining > 0:
            print(f"\n[{remaining} pergunta(s) restante(s) nesta sessão]")


if __name__ == "__main__":
    run_cli()
