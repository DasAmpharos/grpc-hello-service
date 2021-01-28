package io.github.dylmeadows.helloworld

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.github.dylmeadows.helloworld.GreeterGrpc.GreeterImplBase
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


class Greeter : CliktCommand() {
    val port: Int by option(help = "gRPC server port").int().default(50051)
    private val logger = Logger.getLogger(Greeter::class.simpleName)

    override fun run() {
        val server = ServerBuilder.forPort(port)
            .addService(GreeterService)
            .addService(ProtoReflectionService.newInstance())
            .build()
            .start()
        logger.info("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                try {
                    stop(server)
                } catch (e: InterruptedException) {
                    e.printStackTrace(System.err)
                }
                System.err.println("*** server shut down")
            }
        })
        server.awaitTermination()
    }

    private fun stop(server: Server?) {
        server?.shutdown()?.awaitTermination(30, TimeUnit.SECONDS)
    }
}

object GreeterService : GreeterImplBase() {
    override fun sayHello(
        request: HelloRequest,
        responseObserver: StreamObserver<HelloResponse>
    ) {
        responseObserver.onNext(
            HelloResponse.newBuilder()
                .setMessage("Hello, ${request.name}!")
                .build()
        )
        responseObserver.onCompleted()
    }
}

fun main(args: Array<String>) = Greeter().main(args)