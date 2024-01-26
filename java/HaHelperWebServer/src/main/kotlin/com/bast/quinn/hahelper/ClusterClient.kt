package com.bast.quinn.hahelper

import com.bast.quinn.hahelper.grpc.meta.GetStatusRequest
import com.bast.quinn.hahelper.grpc.meta.MetaServiceGrpcKt
import io.grpc.ManagedChannelBuilder

class ClusterClient(
    public val host: Host
) {

    val client = MetaServiceGrpcKt.MetaServiceCoroutineStub(
        ManagedChannelBuilder
            .forAddress(host.hostname, host.port)
            .usePlaintext()
            .build())

    suspend fun poll() = client.getStatus(GetStatusRequest.getDefaultInstance())

}