package com.inu.inujeungplayer.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.inu.inujeungplayer.model.Music
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomMusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(music: Music) : Long

    @Update
    suspend fun update(music: Music)

    @Query("SELECT id FROM music_table")
    fun getAllIds() : MutableList<Int>

    @Query("SELECT * FROM music_table")
    suspend fun getCustomSortOrder() : List<Music>

    @Query("SELECT * FROM music_table WHERE id like :id")
    fun  getMusic(id : Int) : Music

    //***************썬플라워liavedata******************************* */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(musics: List<Music>)

    @Query("SELECT * FROM music_table ORDER BY genre")
    fun getMusicsWithGenreSorted(): LiveData<List<Music>>

    @Query("SELECT * FROM music_table ORDER BY RANDOM()")// ORDER BY music ASC")
    fun getAllMusics() : Flow<List<Music>>

    @Query("SELECT * FROM music_table WHERE path LIKE :query") //artist LIKE :query OR title LIKE :query OR album LIKE :query
    fun searchMusic(query: String): Flow<List<Music>>

    @Query("SELECT * FROM music_table WHERE title LIKE :query " +
            "OR artist LIKE :query OR album LIKE :query")
    fun search(query: String): List<Music>

//    @Query("SELECT * FROM music_table")// ORDER BY music ASC")
//    fun getAllMusicsList() : Flow<List<Music>>
//**************************************************************


    @Query("SELECT * FROM music_table ORDER BY RANDOM()")// ORDER BY music ASC")
    suspend  fun getAllMusicsRandom() : MutableList<Music>


    @Query("SELECT * FROM music_table WHERE bookmark LIKE :b")
    suspend fun getBook(b: Boolean) : MutableList<Music>

    @Query("DELETE FROM music_table WHERE id like :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM music_table")
    suspend fun deleteAlMusics()


}