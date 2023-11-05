package com.cartravelsdailerapp.Repositorys

import com.cartravelsdailerapp.Repositorys.DAO.CallLogsDataSource
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import kotlinx.coroutines.CoroutineDispatcher
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

    /*suspend fun fetchContacts(): List<Contact> {
        return withContext(myDispatcher) {
            source.readContacts()
        }

    }*/

    suspend fun fetchCallLogSignle(): CallHistory {
        return withContext(myDispatcher) { source.fetchCallLogSingle() }

    }
}