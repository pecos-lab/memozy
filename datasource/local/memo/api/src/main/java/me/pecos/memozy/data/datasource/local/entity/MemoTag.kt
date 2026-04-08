package me.pecos.memozy.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "memo_tag",
    primaryKeys = ["memoId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = Memo::class, parentColumns = ["id"], childColumns = ["memoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("memoId"), Index("tagId")]
)
data class MemoTag(
    val memoId: Int,
    val tagId: Int
)
