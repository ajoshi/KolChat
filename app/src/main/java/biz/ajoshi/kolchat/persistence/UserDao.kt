package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import biz.ajoshi.kolchat.model.User

/**
 * Created by ajoshi on 7/8/2017.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getUsers () : List <User>

    @Insert
    fun insert(user: User)

    @Delete
    fun delete(user: User)
}