package me.pecos.memozy.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.MemoTag
import me.pecos.memozy.data.datasource.local.entity.Tag

@Dao
interface TagDao {
    @Query("SELECT * FROM tag ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tag ORDER BY name ASC")
    suspend fun getAllTagsOnce(): List<Tag>

    @Insert
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Query("DELETE FROM tag WHERE id = :id")
    suspend fun deleteTagById(id: Int)

    @Query("SELECT * FROM tag WHERE id = :id")
    suspend fun getTagById(id: Int): Tag?

    // 메모-태그 관계
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemoTag(memoTag: MemoTag)

    @Query("DELETE FROM memo_tag WHERE memoId = :memoId AND tagId = :tagId")
    suspend fun removeMemoTag(memoId: Int, tagId: Int)

    @Query("DELETE FROM memo_tag WHERE memoId = :memoId")
    suspend fun removeAllTagsForMemo(memoId: Int)

    // 메모에 연결된 태그 조회
    @Query("SELECT t.* FROM tag t INNER JOIN memo_tag mt ON t.id = mt.tagId WHERE mt.memoId = :memoId ORDER BY t.name ASC")
    suspend fun getTagsForMemo(memoId: Int): List<Tag>

    @Query("SELECT t.* FROM tag t INNER JOIN memo_tag mt ON t.id = mt.tagId WHERE mt.memoId = :memoId ORDER BY t.name ASC")
    fun getTagsForMemoFlow(memoId: Int): Flow<List<Tag>>

    // 태그에 연결된 메모 ID 조회
    @Query("SELECT memoId FROM memo_tag WHERE tagId = :tagId")
    fun getMemoIdsForTag(tagId: Int): Flow<List<Int>>
}
