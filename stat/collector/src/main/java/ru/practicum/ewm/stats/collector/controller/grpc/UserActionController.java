package ru.practicum.ewm.stats.collector.controller.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.kafka.KafkaClient;
import ru.practicum.ewm.stats.proto.messages.UserActionProto;
import ru.practicum.ewm.stats.proto.services.UserActionControllerGrpc;

import java.time.Instant;


@GrpcService
@AllArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final KafkaClient kafkaClient;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                    .setUserId(request.getUserId())
                    .setEventId(request.getEventId())
                    .setTimestamp(Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos()))
                    .setActionType(ActionTypeAvro.valueOf(request.getActionType().name()))
                    .build();

            kafkaClient.sendData(String.valueOf(request.getUserId()), userActionAvro, kafkaClient.getUserActionsTopic());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
