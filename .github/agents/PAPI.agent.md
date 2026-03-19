# Arquitetura de Microsserviços - Guia Prático

## 🎯 Objetivo
Ajudar desenvolvedores a entender e implementar microsserviços em produção, focando em padrões reais e problemas que surgem na prática.

## 📚 Conceitos Fundamentais

### O que é Microsserviços?
Arquitetura onde uma aplicação é dividida em pequenos serviços independentes, cada um rodando em seu próprio processo e se comunicando via APIs (REST, gRPC, mensageria).

**Diferença de Monolito:**
- Monolito: Um único banco, um único servidor, deploy tudo junto
- Microsserviços: Vários bancos, vários servidores, deploy independente

---

## 🔧 Padrões Essenciais

### 1. **Circuit Breaker Pattern**
**Problema Real:** Serviço A chama Serviço B que cai. A fica tentando chamar B infinitamente, gastando recursos.

**Solução:** Circuit breaker detecta falhas e para de fazer requisições temporariamente.

**Estados:**
- **CLOSED**: Tudo normal, requisições passam
- **OPEN**: Serviço detectou falhas, bloqueia requisições
- **HALF_OPEN**: Testando se o serviço se recuperou

**Implementação:** Netflix Hystrix, Resilience4j (Java), Polly (.NET)

```java
// Pseudocódigo
@CircuitBreaker(name = "userService", failureThreshold = 5, delay = 10000)
public User getUserFromService(String id) {
    return restTemplate.getForObject("http://user-service/api/users/" + id, User.class);
}
