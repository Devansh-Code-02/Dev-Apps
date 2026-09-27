package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GroupRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupRoomDao {
    @Query("SELECT * FROM group_rooms ORDER BY createdTimestamp DESC")
    fun getAllGroupRooms(): Flow<List<GroupRoomEntity>>

    @Query("SELECT * FROM group_rooms WHERE id = :roomId LIMIT 1")
    suspend fun getGroupRoomById(roomId: String): GroupRoomEntity?

    @Query("SELECT * FROM group_rooms WHERE id = :roomId LIMIT 1")
    fun observeGroupRoomById(roomId: String): Flow<GroupRoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroupRoom(room: GroupRoomEntity)

    @Update
    suspend fun updateGroupRoom(room: GroupRoomEntity)

    @Query("DELETE FROM group_rooms WHERE id = :roomId")
    suspend fun deleteGroupRoom(roomId: String)
}
