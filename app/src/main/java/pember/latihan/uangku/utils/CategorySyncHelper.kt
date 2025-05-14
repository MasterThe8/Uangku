package pember.latihan.uangku.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.seed.CategorySeed
import pember.latihan.uangku.model.Category

object CategorySyncHelper {

    suspend fun syncCategories(context: Context): List<Category> {
        val db = AppDatabase.getInstance(context)
        val dao = db.categoryDao()

        return withContext(Dispatchers.IO) {
            val existing = dao.getAll()
            val toInsert = CategorySeed.defaultCategories.filter { seed ->
                existing.none { it.name == seed.name && it.type == seed.type }
            }
            if (toInsert.isNotEmpty()) {
                dao.insertAll(toInsert)
            }
            dao.getAll()
        }
    }
}

