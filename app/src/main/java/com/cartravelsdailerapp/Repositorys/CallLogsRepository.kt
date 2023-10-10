package com.cartravelsdailerapp.Repositorys

import android.os.Build
import androidx.annotation.RequiresApi
import com.cartravelsdailerapp.Repositorys.DAO.CallLogsDataSource
import com.cartravelsdailerapp.models.CallHistory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CallLogsRepository(
    private val source: CallLogsDataSource,
    private val myDispatcher: CoroutineDispatcher
) {
    suspend fun fetchCallLogs(): List<CallHistory> {
        return withContext(myDispatcher) {
            source.fetchCallLogsList()
        }

    }

    suspend fun fetchCallLogSignle(): CallHistory {
        return withContext(myDispatcher) { source.fetchCallLogSingle() }

    }
}