package com.akhara.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.akhara.data.db.dao.BodyWeightDao
import com.akhara.data.db.dao.ExerciseDao
import com.akhara.data.db.dao.PlannedExerciseDao
import com.akhara.data.db.dao.PlanDao
import com.akhara.data.db.dao.SettingsDao
import com.akhara.data.db.dao.WorkoutDao
import com.akhara.data.db.entity.BodyWeight
import com.akhara.data.db.entity.Exercise
import com.akhara.data.db.entity.PlannedExercise
import com.akhara.data.db.entity.UserSettings
import com.akhara.data.db.entity.WeeklyPlan
import com.akhara.data.db.entity.WorkoutSession
import com.akhara.data.db.entity.WorkoutSet
import com.akhara.data.seed.ExerciseSeedData
import com.akhara.security.SecurePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        Exercise::class,
        WorkoutSession::class,
        WorkoutSet::class,
        WeeklyPlan::class,
        UserSettings::class,
        PlannedExercise::class,
        BodyWeight::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AkharaDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun planDao(): PlanDao
    abstract fun settingsDao(): SettingsDao
    abstract fun plannedExerciseDao(): PlannedExerciseDao
    abstract fun bodyWeightDao(): BodyWeightDao

    companion object {
        @Volatile
        private var INSTANCE: AkharaDatabase? = null

        private const val DB_NAME = "akhara_database"

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_workout_sets_exerciseId_sessionId " +
                    "ON workout_sets(exerciseId, sessionId)"
                )
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sets ADD COLUMN plannedReps INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE workout_sets ADD COLUMN plannedWeight REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE workout_sets ADD COLUMN completedAt INTEGER DEFAULT NULL")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS planned_exercises (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "dayOfWeek INTEGER NOT NULL, " +
                        "exerciseId INTEGER NOT NULL, " +
                        "targetSets INTEGER NOT NULL, " +
                        "targetReps INTEGER NOT NULL, " +
                        "targetWeight REAL NOT NULL, " +
                        "orderIndex INTEGER NOT NULL, " +
                        "FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_planned_exercises_exerciseId ON planned_exercises(exerciseId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_planned_exercises_dayOfWeek ON planned_exercises(dayOfWeek)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS body_weight (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "weightKg REAL NOT NULL, " +
                        "date INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): AkharaDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                migrateUnencryptedToEncrypted(appContext)
                val passphrase = SecurePreferences.getOrCreateDbPassphrase(appContext)
                val factory = SupportFactory(passphrase)

                Room.databaseBuilder(
                    appContext,
                    AkharaDatabase::class.java,
                    DB_NAME
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(SeedCallback(appContext))
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * One-time migration: encrypts an existing plain-text database file in-place
         * using SQLCipher's sqlcipher_export. Safe to call multiple times — it's a no-op
         * if the DB is already encrypted or doesn't exist. If migration fails, deletes
         * the old DB so a fresh encrypted one is created (data loss, but crash-safe).
         */
        private fun migrateUnencryptedToEncrypted(context: Context) {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return

            val isUnencrypted = try {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )
                db.rawQuery("SELECT count(*) FROM sqlite_master", null).use { it.moveToFirst() }
                db.close()
                true
            } catch (_: Exception) {
                false
            }
            if (!isUnencrypted) return

            val passphrase = SecurePreferences.getOrCreateDbPassphrase(context)
            val passphraseStr = String(passphrase, Charsets.UTF_8)
            val tempFile = context.getDatabasePath("${DB_NAME}_encrypted")

            try {
                net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
                val db = net.sqlcipher.database.SQLiteDatabase.openDatabase(
                    dbFile.absolutePath, "", null,
                    net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE
                )
                db.rawExecSQL("ATTACH DATABASE '${tempFile.absolutePath}' AS encrypted KEY '$passphraseStr'")
                db.rawExecSQL("SELECT sqlcipher_export('encrypted')")
                db.rawExecSQL("DETACH DATABASE encrypted")
                db.close()

                dbFile.delete()
                tempFile.renameTo(dbFile)
            } catch (_: Exception) {
                tempFile.delete()
                dbFile.delete()
            }
        }
    }

    private class SeedCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                database.exerciseDao().insertAll(ExerciseSeedData.exercises)
                database.settingsDao().insertSettings(UserSettings())
            }
        }
    }
}
