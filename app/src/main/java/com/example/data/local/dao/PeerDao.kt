package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY isOnline DESC, lastSeenTimestamp DESC")
    fun getAllPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isSaved = 1 ORDER BY lastSeenTimestamp DESC")
    fun getSavedContacts(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isSaved = 1 ORDER BY lastSeenTimestamp DESC")
    suspend fun getSavedContactsList(): List<PeerEntity>

    @Query("SELECT * FROM peers WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getPeerByMac(macAddress: String): PeerEntity?

    @Query("SELECT * FROM peers WHERE macAddress = :macAddress LIMIT 1")
    fun observePeerByMac(macAddress: String): Flow<PeerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeer(peer: PeerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeers(peers: List<PeerEntity>)

    @Update
    suspend fun updatePeer(peer: PeerEntity)

    @Query("UPDATE peers SET customAlias = :newAlias WHERE macAddress = :macAddress")
    suspend fun updateCustomAlias(macAddress: String, newAlias: String?)

    @Query("UPDATE peers SET isSaved = :isSaved WHERE macAddress = :macAddress")
    suspend fun updateSavedStatus(macAddress: String, isSaved: Boolean)

    @Query("DELETE FROM peers WHERE macAddress = :macAddress")
    suspend fun deletePeer(macAddress: String)

    @Query("DELETE FROM peers")
    suspend fun deleteAllPeers()
}
