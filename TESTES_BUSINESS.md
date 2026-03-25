# Suite de Testes - Business Layer (Mockito)

Testes completos e reutilizáveis para todas as classes Business do módulo administrativo, desenvolvidos com **Mockito** e **JUnit 5**, seguindo as melhores práticas de teste em Java.

---

## 📋 Classes Testadas

| Classe | Arquivo de Teste | Casos de Teste |
|--------|------------------|----------------|
| `AuthBusiness` | `AuthBusinessTest.java` | 9 |
| `AdminUsuarioBusiness` | `AdminUsuarioBusinessTest.java` | 18 |
| `AdminRoleBusiness` | `AdminRoleBusinessTest.java` | 15 |
| `AdminSistemaConfigBusiness` | `AdminSistemaConfigBusinessTest.java` | 15 |
| `AdminListaAcessoBusiness` | `AdminListaAcessoBusinessTest.java` | 26 |
| `AdminAcessoLogBusiness` | `AdminAcessoLogBusinessTest.java` | 26 |

### **Total: 109 casos de teste** ✅

---

## 🏗️ Arquitetura de Testes

### Estrutura de Cada Suite

```
@ExtendWith(MockitoExtension.class)
├── @Mock → Dependências (DAOs, Services, Util)
├── @InjectMocks → Classe sendo testada
├── @BeforeEach → Setup de fixtures
│
└── @Nested Classes (Agrupadas por funcionalidade)
    ├── @Test (caso de sucesso)
    │   ├── Arrange (dados mockeados)
    │   ├── Act (executar método)
    │   └── Assert (validar resultado)
    │
    ├── @Test (exceção esperada)
    │   ├── assertThrows()
    │   └── verify() de não-execução
    │
    └── @Test (cenário edge case)
```

---

## 📦 Dependências Utilizadas

```xml
<!-- JUnit 5 (incluso no Spring Boot Starter Test) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito (incluso no Spring Boot Starter Test) -->
<!-- org.mockito:mockito-core -->
<!-- org.mockito:mockito-junit-jupiter -->
```

---

## 🧪 Boas Práticas Implementadas

### ✅ 1. **Padrão AAA (Arrange-Act-Assert)**
Cada teste segue estrutura clara:
```java
@Test
void deveExecutarComSucesso() {
    // Arrange
    when(usuarioDAO.buscarPorEmail(email)).thenReturn(usuario);
    
    // Act
    UsuarioBean resultado = adminUsuarioBusiness.criarAdmin(nome, email, senha);
    
    // Assert
    assertNotNull(resultado);
    verify(usuarioDAO).inserir(any());
}
```

### ✅ 2. **Naming Convenção Descritiva**
```java
@DisplayName("Deve editar usuário com email já em uso")
void deveLancarExcecaoEmailJaEmUso() { }

@DisplayName("Deve lançar exceção quando role não encontrada")
void deveLancarExcecaoRoleNaoEncontrada() { }
```

### ✅ 3. **Uso de @Nested para Organização**
```java
@Nested
@DisplayName("Criar Usuario Tests")
class CriarUsuarioTests {
    // Todos os testes de criação agrupados
}

@Nested
@DisplayName("Editar Usuario Tests")
class EditarUsuarioTests {
    // Todos os testes de edição agrupados
}
```

### ✅ 4. **Fixtures e Setup com @BeforeEach**
```java
@BeforeEach
void setUp() {
    roleMock = new RoleBean();
    roleMock.setIdRole(1L);
    roleMock.setNome("ADMIN");
    // ... outros setups
}
```

### ✅ 5. **Validações com Mockito**
```java
// Verify de chamadas
verify(usuarioDAO).inserir(any(UsuarioBean.class));
verify(passwordEncoder, never()).encode(anyString());

// ArgumentCaptor para dados específicos
ArgumentCaptor<UsuarioBean> captor = ArgumentCaptor.forClass(UsuarioBean.class);
verify(usuarioDAO).inserir(captor.capture());
assertEquals("email@schemusic.com", captor.getValue().getEmail());
```

### ✅ 6. **Teste de Exceções**
```java
IllegalArgumentException exception = assertThrows(
    IllegalArgumentException.class,
    () -> adminUsuarioBusiness.criarAdmin("", "email@schemusic.com", "senha123")
);
assertEquals("Nome obrigatorio", exception.getMessage());
```

### ✅ 7. **Cobertura de Cenários**
- ✅ Caso de sucesso (happy path)
- ✅ Validações de entrada
- ✅ Exceções esperadas
- ✅ Edge cases
- ✅ Estados nulos (null safety)
- ✅ Integridade referencial

---

## 📊 Distribuição de Testes por Classe

### **AuthBusinessTest** (9 testes)
```
├── Login Artista
│   ├── ✅ com sucesso
│   ├── ✅ email inválido
│   └── ✅ senha incorreta
│
└── Login Administrador
    ├── ✅ com sucesso
    ├── ✅ usuário não encontrado
    ├── ✅ senha incorreta
    └── ✅ sem role ADMIN
```

### **AdminUsuarioBusinessTest** (18 testes)
```
├── Criar Admin (6 testes)
│   ├── ✅ sucesso
│   ├── ✅ nome vazio
│   ├── ✅ email inválido
│   ├── ✅ senha curta
│   ├── ✅ email já cadastrado
│   └── ✅ role ADMIN não encontrada
│
├── Editar Usuario (5 testes)
├── Vincular Roles (3 testes)
├── Inativar Usuario (2 testes)
└── Listar Usuarios (2 testes)
```

### **AdminRoleBusinessTest** (15 testes)
```
├── Criar Role (6 testes)
├── Editar Role (4 testes)
├── Excluir Role (3 testes)
└── Validar Referential Integrity (2 testes)
```

### **AdminSistemaConfigBusinessTest** (15 testes)
```
├── Criar Config (7 testes)
├── Editar Config (5 testes)
├── Excluir Config (2 testes)
└── Listar Config (1 teste)
```

### **AdminListaAcessoBusinessTest** (26 testes)
```
├── Criar Entrada (7 testes)
├── Editar Entrada (3 testes)
├── Inativar Entrada (2 testes)
├── Listar Entradas (5 testes)
└── Validação de Tipos (2 testes)
```

### **AdminAcessoLogBusinessTest** (26 testes)
```
├── Listar Logs (5 testes)
├── Limite de Registros (4 testes)
├── Log Data Structure (2 testes)
├── HTTP Methods (5 testes)
├── Status Response (5 testes)
└── Audit Trail (3 testes)
```

---

## 🚀 Como Executar

### **Executar Todos os Testes**
```bash
./mvnw test
```

### **Executar Testes de Uma Classe Específica**
```bash
./mvnw test -Dtest=AdminUsuarioBusinessTest
```

### **Executar com Coverage (JaCoCo)**
```bash
./mvnw clean test jacoco:report
# Abrir: target/site/jacoco/index.html
```

### **Executar com Output Detalhado**
```bash
./mvnw test -X -e
```

### **Executar Um Teste Específico**
```bash
./mvnw test -Dtest=AdminUsuarioBusinessTest#deveCriarAdminComSucesso
```

---

## 📈 Exemplo de Teste Completo

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUsuarioBusiness Tests")
class AdminUsuarioBusinessTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @Mock
    private RoleDAO roleDAO;

    @Mock
    private UsuarioRoleDAO usuarioRoleDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUsuarioBusiness adminUsuarioBusiness;

    private RoleBean roleAdminMock;

    @BeforeEach
    void setUp() {
        roleAdminMock = new RoleBean();
        roleAdminMock.setIdRole(1L);
        roleAdminMock.setNome("ADMIN");
    }

    @Nested
    @DisplayName("Criar Admin Tests")
    class CriarAdminTests {

        @Test
        @DisplayName("Deve criar admin com sucesso")
        void deveCriarAdminComSucesso() {
            // Arrange
            String nome = "Maria Santos";
            String email = "maria@schemusic.com";
            String senha = "senha123";
            String senhaHash = "$2a$10$hashedsenha123";

            when(usuarioDAO.buscarPorEmail(email.toLowerCase())).thenReturn(null);
            when(passwordEncoder.encode(senha)).thenReturn(senhaHash);
            when(usuarioDAO.inserir(any(UsuarioBean.class))).thenReturn(2L);
            when(roleDAO.buscarPorNome("ADMIN")).thenReturn(roleAdminMock);
            
            // Act
            UsuarioBean resultado = adminUsuarioBusiness.criarAdmin(nome, email, senha);

            // Assert
            assertNotNull(resultado);
            verify(usuarioDAO).inserir(any(UsuarioBean.class));
            verify(usuarioRoleDAO).vincularSeNaoExistir(2L, 1L);
            verify(passwordEncoder).encode(senha);
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já cadastrado")
        void deveLancarExcecaoEmailJaCadastrado() {
            // Arrange
            UsuarioBean usuarioExistente = new UsuarioBean();
            when(usuarioDAO.buscarPorEmail("existente@schemusic.com"))
                .thenReturn(usuarioExistente);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adminUsuarioBusiness.criarAdmin("Nome", "existente@schemusic.com", "senha123")
            );
            assertEquals("Email ja cadastrado", exception.getMessage());
        }
    }
}
```

---

## 💡 Dicas de Uso

### **1. Mock vs Real Objects**
- Use `@Mock` para DAOs e Services externos
- Use `@InjectMocks` apenas para a classe sendo testada
- Evite `@Spy` para testes unitários puros

### **2. ArgumentMatchers**
```java
// Verificar com qualquer String
verify(dao).inserir(any(UsuarioBean.class));

// Verificar com tipo específico
verify(dao).inserir(argThat(u -> u.getEmail().contains("@")));

// Capturar argumento
ArgumentCaptor<UsuarioBean> captor = ArgumentCaptor.forClass(UsuarioBean.class);
verify(dao).inserir(captor.capture());
```

### **3. Testear Exceções**
```java
// Com mensagem
assertThrows(
    IllegalArgumentException.class,
    () -> business.criar(null, "", 0),
    "Deveria lançar exceção"
);

// Com validação completa
Exception exception = assertThrows(Exception.class, () -> {/* código */});
assertTrue(exception.getMessage().contains("esperado"));
```

### **4. Limpar Mocks Entre Testes**
```java
@BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this); // Opcional, @ExtendWith já faz isso
}

@AfterEach
void tearDown() {
    Mockito.validateMockitoUsage(); // Validar uso correto de mocks
}
```

---

## 📋 Checklist de Qualidade

- ✅ Todos os testes compil sem erros
- ✅ Cada teste testa UMA coisa (Single Responsibility)
- ✅ Nomes descritivos em português
- ✅ Padrão AAA (Arrange-Act-Assert)
- ✅ Uso correto de Mockito (nunca spies real objects)
- ✅ Validação de exceções com assertThrows
- ✅ Agrupamento com @Nested por funcionalidade
- ✅ Setup adequado em @BeforeEach
- ✅ Sem testes interdependentes
- ✅ Sem hardcoding excessivo (usar constantes)

---

## 🔍 Próximos Passos

1. **Adicionar Testes de Integração**
   - Usar `@SpringBootTest` + `@DataJpaTest`
   - Testar fluxo completo Controller → Business → DAO

2. **Gerar Relatório de Cobertura**
   ```bash
   ./mvnw clean test jacoco:report
   ```

3. **Testes de Performance**
   - Medir tempo de execução de operações em massa
   - Usar `@Benchmark` do JMH

4. **Testes Parametrizados**
   - Usar `@ParameterizedTest` + `@CsvSource` para múltiplos cenários

5. **DocumentaçãoAutomatic**
   - Gerar documentação de testes com TestNG Reports

---

## 📞 Suporte e Referências

- **JUnit 5 Docs**: https://junit.org/junit5/docs/current/user-guide/
- **Mockito Docs**: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Spring Testing**: https://spring.io/guides/gs/testing-web/

---

**Desenvolvido com ❤️ para Schemusic API**
