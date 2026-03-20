---
name: adding-feature
description: 새로운 기능(데이터 엔티티)을 추가할 때 사용. "새 기능 추가", "새 엔티티", "새 화면 만들어줘", 또는 Room DB에 새 테이블이 필요한 작업에 활성화됨. Entity → DAO → Repository → ViewModel → Screen 전체 보일러플레이트를 이 프로젝트 패턴에 맞게 생성.
version: 1.0.0
---

# Adding Feature 스킬

이 프로젝트(me.pecos.nota)에 새 기능을 추가할 때 따라야 할 패턴.

## 파일 생성 순서

1. `{Feature}.kt` — Room Entity
2. `{Feature}UiState.kt` — UI 전용 데이터 모델
3. `{Feature}Dao.kt` — Room DAO 인터페이스
4. `{Feature}Repository.kt` — Repository 인터페이스
5. `{Feature}RepositoryImpl.kt` — Repository 구현체
6. `{Feature}ViewModel.kt` — AndroidViewModel + 매핑 확장함수
7. `{Feature}Screen.kt` — Composable 화면

모든 파일은 `app/src/main/java/me/pecos/nota/` 에 생성.

---

## 1. Entity 패턴

```kotlin
package me.pecos.nota

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "{tableName}")
data class {Feature}(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // 필드 추가
    val createdAt: Long = System.currentTimeMillis()
)
```

## 2. UiState 패턴

Entity와 동일한 필드 구조. UI 레이어 전용으로 사용.

```kotlin
package me.pecos.nota

data class {Feature}UiState(
    val id: Int = 0,
    // Entity와 동일한 필드
    val createdAt: Long = 0L
)
```

## 3. DAO 패턴

```kotlin
package me.pecos.nota

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface {Feature}Dao {
    @Query("SELECT * FROM {tableName} ORDER BY id DESC")
    fun getAll{Feature}s(): Flow<List<{Feature}>>

    @Insert
    suspend fun insert{Feature}(item: {Feature})

    @Update
    suspend fun update{Feature}(item: {Feature})

    @Query("DELETE FROM {tableName} WHERE id = :id")
    suspend fun delete{Feature}ById(id: Int)

    @Query("DELETE FROM {tableName}")
    suspend fun clearAll{Feature}s()
}
```

## 4. Repository 인터페이스 패턴

```kotlin
package me.pecos.nota

import kotlinx.coroutines.flow.Flow

interface {Feature}Repository {
    fun get{Feature}s(): Flow<List<{Feature}>>
    suspend fun add{Feature}(item: {Feature})
    suspend fun delete{Feature}(id: Int)
    suspend fun update{Feature}(item: {Feature})
    suspend fun clearAll{Feature}s()
}
```

## 5. RepositoryImpl 패턴

```kotlin
package me.pecos.nota

import kotlinx.coroutines.flow.Flow

class {Feature}RepositoryImpl(private val dao: {Feature}Dao) : {Feature}Repository {
    override fun get{Feature}s(): Flow<List<{Feature}>> = dao.getAll{Feature}s()
    override suspend fun add{Feature}(item: {Feature}) { dao.insert{Feature}(item) }
    override suspend fun delete{Feature}(id: Int) { dao.delete{Feature}ById(id) }
    override suspend fun update{Feature}(item: {Feature}) { dao.update{Feature}(item) }
    override suspend fun clearAll{Feature}s() { dao.clearAll{Feature}s() }
}
```

## 6. ViewModel 패턴

```kotlin
package me.pecos.nota

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class {Feature}ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = {Feature}RepositoryImpl(
        MemoDatabase.getDatabase(application).{featureCamel}Dao()
    )

    val uiState = repository.get{Feature}s()
        .map { list -> list.map { it.toUiState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add{Feature}(/* 필드 파라미터 */) {
        viewModelScope.launch {
            repository.add{Feature}(
                {Feature}(/* 필드 매핑 */)
            )
        }
    }

    fun delete{Feature}(id: Int) {
        viewModelScope.launch { repository.delete{Feature}(id) }
    }

    fun update{Feature}(item: {Feature}UiState) {
        viewModelScope.launch { repository.update{Feature}(item.to{Feature}()) }
    }
}

// 매핑 확장함수 — ViewModel 파일 하단에 위치
fun {Feature}UiState.to{Feature}(): {Feature} = {Feature}(
    id = this.id,
    // 필드 매핑
    createdAt = this.createdAt
)

fun {Feature}.toUiState(): {Feature}UiState = {Feature}UiState(
    id = this.id,
    // 필드 매핑
    createdAt = this.createdAt
)
```

## 7. MemoDatabase에 추가

새 Entity와 DAO를 `MemoDatabase.kt`에 등록해야 함:

```kotlin
@Database(entities = [Memo::class, {Feature}::class], version = {버전+1})
abstract class MemoDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun {featureCamel}Dao(): {Feature}Dao
    // ...
}
```

> ⚠️ DB 버전 올릴 때 마이그레이션(Migration) 추가 또는 `fallbackToDestructiveMigration()` 사용 여부 확인.

## 8. Navigation에 화면 추가

`MainActivity.kt`의 NavHost에 새 route 추가:

```kotlin
composable("{featureLower}") {
    {Feature}Screen(viewModel = viewModel())
}
```
