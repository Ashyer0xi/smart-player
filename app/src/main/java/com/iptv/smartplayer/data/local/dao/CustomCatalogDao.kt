package com.iptv.smartplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.iptv.smartplayer.data.local.entity.CatalogItemEntity
import com.iptv.smartplayer.data.local.entity.CustomCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCatalogDao {
    @Query("SELECT * FROM custom_catalogs ORDER BY createdAtEpochMillis DESC")
    fun observeCatalogs(): Flow<List<CustomCatalogEntity>>

    @Insert
    suspend fun insertCatalog(catalog: CustomCatalogEntity): Long

    @Delete
    suspend fun deleteCatalog(catalog: CustomCatalogEntity)

    @Query("SELECT * FROM catalog_items WHERE catalogId = :catalogId")
    fun observeItems(catalogId: Long): Flow<List<CatalogItemEntity>>

    @Insert
    suspend fun addItem(item: CatalogItemEntity)

    @Query("DELETE FROM catalog_items WHERE id = :itemId")
    suspend fun removeItem(itemId: Long)
}
