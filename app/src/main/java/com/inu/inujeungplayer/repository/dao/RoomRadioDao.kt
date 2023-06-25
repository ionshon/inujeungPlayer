package com.inu.inujeungplayer.repository.dao

import androidx.room.*
import com.inu.inujeungplayer.model.Radio
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomRadioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(radio: Radio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(radios: List<Radio>)

    @Query("SELECT * FROM radio_table WHERE id like :id")
    fun  getRadio(id : Int) : Radio

    @Update
    suspend fun update(radio: Radio)

    @Query("SELECT * FROM radio_table WHERE id like :id")
    fun  getRadio(id : Long) : Radio

    @Query("SELECT * FROM radio_table")// ORDER BY Radio ASC")
    fun getAllRadios() : MutableList<Radio>

//    @Query("SELECT * FROM radio_table WHERE artist like :query")
//    fun searchArtist(query: String): MutableList<Radio>

    @Query("DELETE FROM radio_table WHERE id like :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM radio_table")
    suspend fun deleteAllRadios()
}