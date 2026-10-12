package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryItem
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.DefaultCategories
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        CategoryItem::class,
        Conversation::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prime_infinity_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            // Seed built-in categories
            dao.insertDefaultCategories(DefaultCategories.ALL)

            // Seed default primary user profile
            val defaultUser = UserProfile(
                id = "default_user_1",
                name = "Prime Alpha",
                avatarEmoji = "⚡",
                bio = "Infinity Explorer & Creator",
                activeCategoryId = "schools",
                temperature = 0.7f,
                tone = "Helpful & Balanced",
                responseLength = "Balanced",
                customInstructions = "Provide insightful, accurate, and structured responses.",
                ttsEnabled = false,
                isPrivateByDefault = false
            )
            dao.insertProfile(defaultUser)

            // Seed a secondary profile for demonstration
            val creativeUser = UserProfile(
                id = "profile_creative_2",
                name = "Aero Nova",
                avatarEmoji = "🚀",
                bio = "Anime, Culture & Fitness fanatic",
                activeCategoryId = "anime",
                temperature = 0.85f,
                tone = "Creative & Playful",
                responseLength = "Detailed",
                customInstructions = "Embrace creative analogies and vibrant personality.",
                ttsEnabled = true,
                isPrivateByDefault = false
            )
            dao.insertProfile(creativeUser)
        }
    }
}
