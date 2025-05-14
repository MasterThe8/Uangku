package pember.latihan.uangku.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.latihan.uangku.data.AppDatabase

object DataResetHelper {
    fun clearLocalDataWithSync(context: Context, uid: String, onCleared: () -> Unit) {
        FirebaseSyncHelper.syncToFirebase(context, uid) {
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getInstance(context).clearAllTables()
                withContext(Dispatchers.Main) {
                    onCleared()
                }
            }
        }
    }
}
