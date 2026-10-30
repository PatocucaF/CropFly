package com.example.cropfly.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cropfly.model.dao.InteresDao
import com.example.cropfly.model.dao.NecesidadDao
import com.example.cropfly.model.dao.NotificacionDao
import com.example.cropfly.model.dao.PrestadorDao
import com.example.cropfly.model.dao.UsuarioDao

/** Base de datos local (Room). Todo lo de la app vive aquí, en el teléfono. */
@Database(
    entities = [Usuario::class, Prestador::class, Necesidad::class, Interes::class, Notificacion::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CropFlyDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun prestadorDao(): PrestadorDao
    abstract fun necesidadDao(): NecesidadDao
    abstract fun interesDao(): InteresDao
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        fun crear(contexto: Context): CropFlyDatabase =
            Room.databaseBuilder(contexto.applicationContext, CropFlyDatabase::class.java, "cropfly.db")
                // Para un MVP académico: si cambias las tablas, la base se recrea en vez de cerrarse la app.
                .fallbackToDestructiveMigration()
                .build()
    }
}
