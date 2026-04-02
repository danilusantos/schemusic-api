# Novos Endpoints de Exclusão - Catálogo de Permissões

## Resumo
Foram implementados dois novos endpoints POST que permitem deletar múltiplos grupos de permissões ou telas de permissões através de um request body contendo um array de IDs.

## Endpoints Implementados

### 1. Excluir Grupos de Permissões
**Endpoint:** `POST /administration/access-control/catalog/groups/delete`

**Descrição:** Deleta múltiplos grupos de permissões. Ao deletar um grupo, todas as permissões de tela vinculadas a ele também serão removidas, assim como todos os vículos em `ROLE_PERMISSAO` e `USUARIO_PERMISSAO`.

**Request Body:**
```json
{
  "ids": [1, 2, 3]
}
```

**Field Descriptions:**
- `ids` (array of numbers, obrigatório): Array contendo os IDs dos grupos a serem deletados.

**Response Sucesso (200 OK):**
```json
{
  "grupos": [...],
  "telas": [...],
  "totalGrupos": 3,
  "totalTelas": 12,
  "permissoesPorGrupo": {...}
}
```

**Response Erro (400 Bad Request):**
```json
{
  "erro": "Grupo nao encontrado: 999"
}
```

**Validações:**
- Campo `ids` é obrigatório
- Array `ids` não pode estar vazio
- Todos os IDs da lista devem existir no banco

---

### 2. Excluir Telas de Permissões
**Endpoint:** `POST /administration/access-control/catalog/screens/delete`

**Descrição:** Deleta múltiplos registros de permissões de tela. Ao deletar uma permissão de tela, todos os vículos em `ROLE_PERMISSAO` e `USUARIO_PERMISSAO` serão removidos automaticamente através de foreign keys.

**Request Body:**
```json
{
  "ids": [5, 7, 9]
}
```

**Field Descriptions:**
- `ids` (array of numbers, obrigatório): Array contendo os IDs das permissões de tela (id_permissao) a serem deletadas.

**Response Sucesso (200 OK):**
```json
{
  "grupos": [...],
  "telas": [...],
  "totalGrupos": 3,
  "totalTelas": 10,
  "permissoesPorGrupo": {...}
}
```

**Response Erro (400 Bad Request):**
```json
{
  "erro": "Tela nao encontrada: 999"
}
```

**Validações:**
- Campo `ids` é obrigatório
- Array `ids` não pode estar vazio
- Todos os IDs da lista devem existir no banco

---

## Fluxo de Exclusão de Grupos

1. Validação dos IDs dos grupos
2. Para cada grupo:
   - Buscar todos os IDs de permissões de tela vinculadas ao grupo
   - Remover os vículos em `ROLE_PERMISSAO` 
   - Remover os vículos em `USUARIO_PERMISSAO`
   - Deletar as permissões de tela
3. Deletar os grupos
4. Retornar o catálogo atualizado

---

## Fluxo de Exclusão de Telas

1. Validação dos IDs das permissões de tela
2. Remover os vículos em `ROLE_PERMISSAO`
3. Remover os vículos em `USUARIO_PERMISSAO`
4. Deletar as permissões de tela
5. Retornar o catálogo atualizado

---

## Camadas Implementadas

### DAO Layer
- **PermissaoGrupoDAO**: 
  - `excluir(Long idGrupo)` - Deleta um grupo individual
  - `excluirMultiplos(List<Long> idsGrupo)` - Deleta múltiplos grupos

- **PermissaoTelaDAO**:
  - `excluir(Long idPermissao)` - Deleta uma permissão individual
  - `excluirMultiplos(List<Long> idsPermissao)` - Deleta múltiplas permissões

- **RolePermissaoDAO**:
  - `excluirPermissoes(List<Long> idsPermissao)` - Remove vículos de permissões nas roles

- **UsuarioPermissaoDAO**:
  - `excluirPermissoes(List<Long> idsPermissao)` - Remove vículos de permissões nos usuários

### Business Layer
- **AdminAccessControlBusiness**:
  - `excluirGruposCatalogo(List<Long> idsGrupo)` - Lógica de negócio para exclusão de grupos
  - `excluirTelasCatalogo(List<Long> idsPermissao)` - Lógica de negócio para exclusão de telas

### Delegate Layer
- **AdministrationAccessControlDelegate**:
  - `excluirGruposCatalogo(Map<String, Object> body)` - Wrapper para requisição
  - `excluirTelasCatalogo(Map<String, Object> body)` - Wrapper para requisição

### Controller Layer
- **AdministrationAccessControlController**:
  - `POST /administration/access-control/catalog/groups/delete` - Endpoint para grupos
  - `POST /administration/access-control/catalog/screens/delete` - Endpoint para telas

---

## Exemplos de Uso

### cURL - Deletar grupos
```bash
curl -X POST 'http://localhost:8080/administration/access-control/catalog/groups/delete' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {token}' \
  -d '{
    "ids": [1, 2, 3]
  }'
```

### cURL - Deletar telas
```bash
curl -X POST 'http://localhost:8080/administration/access-control/catalog/screens/delete' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {token}' \
  -d '{
    "ids": [5, 7, 9]
  }'
```

### JavaScript/Fetch - Deletar grupos
```javascript
const response = await fetch(
  '/administration/access-control/catalog/groups/delete',
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      ids: [1, 2, 3]
    })
  }
);

const result = await response.json();
console.log(result);
```

---

## Notas Importantes

1. **Cascata de Exclusões**: Ao deletar um grupo, as permissões de tela vinculadas também são deletadas, seguido pelos vículos em role e usuário.

2. **Transações**: Os DAOs utilizam Pool de Conexões do DataSource, não implementam transações explícitas. Cada operação SQL é executada separadamente.

3. **Ordem de Eliminação**: É importante deletar os vículos (`ROLE_PERMISSAO` e `USUARIO_PERMISSAO`) antes de deletar as permissões de tela due às foreign keys.

4. **Validação**: Todos os IDs são validados antes de qualquer operação de exclusão.

5. **Response**: Os endpoints retornam o catálogo completo atualizado após a exclusão.

---

## Estrutura de Dados Afetada

### Tabelas Afetadas
- `PERMISSAO_GRUPO` - Deletado quando excluir grupo
- `PERMISSAO_TELA` - Deletado quando excluir tela
- `ROLE_PERMISSAO` - Foreign key `id_permissao` removida
- `USUARIO_PERMISSAO` - Foreign key `id_permissao` removida

### Relacionamentos (Foreign Keys)
```sql
ROLE_PERMISSAO:
  - id_permissao -> PERMISSAO_TELA(id_permissao)
  
USUARIO_PERMISSAO:
  - id_permissao -> PERMISSAO_TELA(id_permissao)

PERMISSAO_TELA:
  - id_grupo -> PERMISSAO_GRUPO(id_grupo) [NULLABLE]
```

---

## Autenticação e Autorização

Ambos os endpoints requerem:
- **Autenticação**: JWT Token válido
- **Autorização**: Usuário deve ter acesso ao `ADMIN_ACCESS_CONTROL` e permissão `EXCLUIR`

## Conclusão

Os novos endpoints implementados seguem o padrão arquitetural existente do projeto:
- Camadas separadas (DAO → Business → Delegate → Controller)
- Validação de entrada
- Tratamento de erros
- Respostas padronizadas
