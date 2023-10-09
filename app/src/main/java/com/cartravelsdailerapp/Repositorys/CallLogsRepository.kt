package com.cartravelsdailerapp.Repositorys

import android.os.Build
import androidx.annotation.RequiresApi
import com.cartravelsdailerapp.Repositorys.DAO.CallLogsDataSource
import com.cartravelsdailerapp.models.CallHistory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CallLogsRepository(
    private val source: CallLogsDataSource,
    private val myDispatcher: CoroutineDispatcher
) {
   fun fetchCallLogs(): List<CallHistory> {
        return source.fetchCallLogsList()

    }

   fun fetchCallLogSignle(): CallHistory {
        return source.fetchCallLogSingle()

    }
}