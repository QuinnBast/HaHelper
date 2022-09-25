package com.bast.quinn.hahelper.server

import com.bast.quinn.hahelper.grpc.data.*
import kotlinx.coroutines.flow.Flow

class DataServices() : DataStorageServicesGrpcKt.DataStorageServicesCoroutineImplBase() {

    override suspend fun put(request: PutRequest): PutResponse {
        return super.put(request)
    }

    override suspend fun get(request: GetRequest): GetResponse {
        return super.get(request)
    }

    override suspend fun delete(request: DeleteRequest): DeleteResponse {
        return super.delete(request)
    }

    override fun watch(request: WatchRequest): Flow<WatchEvent> {
        return super.watch(request)
    }
}