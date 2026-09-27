# Manual de Desenvolvimento Android para Agentes de IA (.ai/ANDROID_GUIDE.md)

Este documento estabelece o guia prático, as regras de código e o passo a passo exato para qualquer **Agente de IA** que precise implementar ou alterar telas, repositórios e regras no aplicativo Android **Platform**.

---

## 1. Princípios Inegociáveis

1. **Clean Architecture Estrita**:
   - `domain`: Proibido importar qualquer classe com prefixo `android.*` ou bibliotecas de terceiros (exceto Coroutines/Flow).
   - `presentation`: Telas Compose nunca chamam banco ou Retrofit diretamente. Toda interação passa pelo ViewModel.
2. **Imutabilidade e Tipagem**:
   - Estados de tela devem ser `data class` imutáveis.
   - NUNCA exponha `MutableStateFlow` público no ViewModel.
3. **Material 3 & Dark Theme**:
   - Todo componente visual deve utilizar tokens do `MaterialTheme.colorScheme` e `MaterialTheme.typography`.
4. **Gerenciamento de Lifecycle e Coroutines**:
   - Use `viewModelScope` em ViewModels e `LaunchedEffect` controlado em Composables quando necessário.
   - Nunca use `GlobalScope`.

---

## 2. Passo a Passo: Como Criar uma Nova Tela e Recurso Completo

Exemplo prático: Criação do recurso de **Notas / Tarefas**:

### Passo 1: Modelo e Contrato de Domínio
Crie em `domain/model/Note.kt`:
```kotlin
package com.platform.app.domain.model

data class Note(
    val id: String,
    val text: String,
    val timestamp: Long
)
```
Crie a interface do repositório em `domain/repository/NoteRepository.kt`:
```kotlin
package com.platform.app.domain.repository

import com.platform.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
}
```
Crie o UseCase em `domain/usecase/GetNotesUseCase.kt`:
```kotlin
package com.platform.app.domain.usecase

import com.platform.app.domain.model.Note
import com.platform.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getNotes()
}
```

---

### Passo 2: Camada de Dados (Room)
Crie a entidade em `data/local/entity/NoteEntity.kt`:
```kotlin
package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val text: String,
    val timestamp: Long
) {
    fun toDomain() = Note(id = id, text = text, timestamp = timestamp)
    companion object {
        fun fromDomain(note: Note) = NoteEntity(id = note.id, text = note.text, timestamp = note.timestamp)
    }
}
```
Crie o DAO em `data/local/dao/NoteDao.kt`:
```kotlin
package com.platform.app.data.local.dao

import androidx.room.*
import com.platform.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)
}
```
Registre o `NoteEntity` e o `noteDao` em `data/local/PlatformDatabase.kt`.

Implemente o repositório em `data/repository/NoteRepositoryImpl.kt`:
```kotlin
package com.platform.app.data.repository

import com.platform.app.data.local.dao.NoteDao
import com.platform.app.data.local.entity.NoteEntity
import com.platform.app.domain.model.Note
import com.platform.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {
    override fun getNotes(): Flow<List<Note>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveNote(note: Note) =
        dao.insert(NoteEntity.fromDomain(note))

    override suspend fun deleteNote(id: String) =
        dao.deleteById(id)
}
```

---

### Passo 3: Injeção com Hilt
1. Em `di/AppModule.kt`:
```kotlin
@Provides
@Singleton
fun provideNoteDao(db: PlatformDatabase): NoteDao = db.noteDao
```
2. Em `di/RepositoryModule.kt`:
```kotlin
@Binds
@Singleton
abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
```

---

### Passo 4: Camada de Apresentação (Compose + ViewModel)
1. Estado de UI (`presentation/notes/NotesUiState.kt`):
```kotlin
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```
2. ViewModel (`presentation/notes/NotesViewModel.kt`):
```kotlin
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val repository: NoteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getNotesUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { notes -> _uiState.update { it.copy(notes = notes, isLoading = false) } }
        }
    }
}
```
3. Composable Screen (`presentation/notes/NotesScreen.kt`):
Use Material 3, `Scaffold`, `LazyColumn` e renderize os estados adequadamente.
4. Registre no `NavGraph.kt`.

---

## 3. Comandos Úteis de Validação Local

- Executar testes unitários:
  ```bash
  ./gradlew test
  ```
- Executar verificação de lint:
  ```bash
  ./gradlew lint
  ```
- Compilar APK de debug:
  ```bash
  ./gradlew assembleDebug
  ```
