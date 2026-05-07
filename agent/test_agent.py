"""
Unit tests for the SchemusicAgent class.

These tests use mocking to avoid real OpenAI API calls,
allowing validation of business logic independently.

Run:
    pip install -r requirements.txt pytest
    pytest test_agent.py -v
"""

from __future__ import annotations

import os
import sys
from pathlib import Path
from unittest.mock import MagicMock, patch

import pytest

# Ensure the agent module is importable when tests run from this directory
sys.path.insert(0, str(Path(__file__).parent))

from schemusic_agent import MAX_RESPONSES, SchemusicAgent


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------


def _make_agent(tmp_path: Path, api_key: str = "sk-test-key") -> SchemusicAgent:
    """Create a SchemusicAgent with a temporary knowledge base file."""
    kb_dir = tmp_path / "knowledge_base"
    kb_dir.mkdir()
    kb_file = kb_dir / "schemusic_internal_spec.md"
    kb_file.write_text("# SchéMusic Internal Spec\nEndpoint: GET /admin/musicos", encoding="utf-8")
    return SchemusicAgent(knowledge_base_path=kb_file, api_key=api_key)


def _mock_completion(text: str) -> MagicMock:
    """Build a mock OpenAI ChatCompletion response."""
    choice = MagicMock()
    choice.message.content = text
    completion = MagicMock()
    completion.choices = [choice]
    return completion


# ---------------------------------------------------------------------------
# Initialisation tests
# ---------------------------------------------------------------------------


class TestAgentInitialisation:
    def test_raises_when_api_key_missing(self, tmp_path: Path) -> None:
        kb = tmp_path / "knowledge_base" / "schemusic_internal_spec.md"
        kb.parent.mkdir()
        kb.write_text("content", encoding="utf-8")
        with patch.dict(os.environ, {}, clear=True):
            # Remove key if present
            os.environ.pop("OPENAI_API_KEY", None)
            with pytest.raises(EnvironmentError, match="OPENAI_API_KEY"):
                SchemusicAgent(knowledge_base_path=kb, api_key=None)

    def test_raises_when_knowledge_base_missing(self, tmp_path: Path) -> None:
        missing_path = tmp_path / "does_not_exist.md"
        with pytest.raises(FileNotFoundError, match="Base de conhecimento"):
            SchemusicAgent(knowledge_base_path=missing_path, api_key="sk-test")

    def test_successful_initialisation(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        assert agent.response_count == 0
        assert not agent.session_ended
        assert agent.model == "gpt-4o-mini"


# ---------------------------------------------------------------------------
# Core ask() logic
# ---------------------------------------------------------------------------


class TestAskMethod:
    def test_first_response_not_ended(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        with patch.object(agent.client.chat.completions, "create", return_value=_mock_completion("Resposta 1")):
            answer, ended = agent.ask("Pergunta 1")
        assert answer == "Resposta 1"
        assert ended is False
        assert agent.response_count == 1

    def test_second_response_not_ended(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        mock = _mock_completion("Resposta")
        with patch.object(agent.client.chat.completions, "create", return_value=mock):
            agent.ask("Pergunta 1")
            answer, ended = agent.ask("Pergunta 2")
        assert ended is False
        assert agent.response_count == 2

    def test_third_response_ends_session(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        responses = iter(["R1", "R2", "Terceira resposta", "Resumo gerado pelo LLM"])
        mock_create = MagicMock(side_effect=lambda **_: _mock_completion(next(responses)))
        with patch.object(agent.client.chat.completions, "create", mock_create):
            agent.ask("P1")
            agent.ask("P2")
            answer, ended = agent.ask("P3")
        assert ended is True
        assert agent.session_ended
        assert "Terceira resposta" in answer
        assert "RESUMO DA SESSÃO" in answer
        assert "Resumo gerado pelo LLM" in answer
        assert "Sessão encerrada" in answer

    def test_raises_after_session_ended(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        responses = iter(["R1", "R2", "R3", "Resumo"])
        mock_create = MagicMock(side_effect=lambda **_: _mock_completion(next(responses)))
        with patch.object(agent.client.chat.completions, "create", mock_create):
            for i in range(MAX_RESPONSES):
                agent.ask(f"Pergunta {i + 1}")
        with pytest.raises(RuntimeError, match="sessão já foi encerrada"):
            agent.ask("Pergunta extra")

    def test_conversation_history_grows_correctly(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        mock = _mock_completion("ok")
        with patch.object(agent.client.chat.completions, "create", return_value=mock):
            agent.ask("Q1")
            agent.ask("Q2")
        # 2 perguntas × 2 entradas (user + assistant) = 4
        assert len(agent.conversation_history) == 4
        assert agent.conversation_history[0]["role"] == "user"
        assert agent.conversation_history[1]["role"] == "assistant"

    def test_empty_question_is_sent_to_llm(self, tmp_path: Path) -> None:
        """Empty string questions are forwarded to the LLM without client-side validation."""
        agent = _make_agent(tmp_path)
        mock = _mock_completion("Sem resposta disponível")
        with patch.object(agent.client.chat.completions, "create", return_value=mock):
            answer, ended = agent.ask("")
        assert ended is False
        assert "Sem resposta disponível" in answer


# ---------------------------------------------------------------------------
# session_ended property
# ---------------------------------------------------------------------------


class TestSessionEnded:
    def test_false_initially(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        assert agent.session_ended is False

    def test_true_after_max_responses(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        responses = iter(["A", "B", "C", "Resumo"])
        mock_create = MagicMock(side_effect=lambda **_: _mock_completion(next(responses)))
        with patch.object(agent.client.chat.completions, "create", mock_create):
            for i in range(MAX_RESPONSES):
                agent.ask(f"Q{i}")
        assert agent.session_ended is True

    def test_max_responses_constant_is_three(self) -> None:
        assert MAX_RESPONSES == 3


# ---------------------------------------------------------------------------
# Knowledge base loading
# ---------------------------------------------------------------------------


class TestKnowledgeBase:
    def test_knowledge_base_content_loaded(self, tmp_path: Path) -> None:
        kb_dir = tmp_path / "knowledge_base"
        kb_dir.mkdir()
        kb_file = kb_dir / "schemusic_internal_spec.md"
        kb_file.write_text("Conteúdo secreto interno", encoding="utf-8")
        agent = SchemusicAgent(knowledge_base_path=kb_file, api_key="sk-test")
        assert "Conteúdo secreto interno" in agent.knowledge_base

    def test_knowledge_base_included_in_system_prompt(self, tmp_path: Path) -> None:
        agent = _make_agent(tmp_path)
        system_prompt = agent._build_system_prompt()
        assert "GET /admin/musicos" in system_prompt
        assert "DOCUMENTAÇÃO INTERNA" in system_prompt
