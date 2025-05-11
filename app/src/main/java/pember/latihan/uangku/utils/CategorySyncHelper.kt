package pember.latihan.uangku.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.data.CategorySeed
import pember.latihan.uangku.model.Category

object CategorySyncHelper {

    suspend fun syncCategories(context: Context): List<Category> {
        val db = AppDatabase.getInstance(context)
        val dao = db.categoryDao()

        val existing = withContext(Dispatchers.IO) { dao.getAll() }

        val toInsert = CategorySeed.defaultCategories.filterNot { seed ->
            existing.any { it.name == seed.name && it.type == seed.type }
        }

        if (toInsert.isNotEmpty()) {
            dao.insertAll(toInsert)
        }

        // Return full list after sync
        return withContext(Dispatchers.IO) { dao.getAll() }
    }

}
