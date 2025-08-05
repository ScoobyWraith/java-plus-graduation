package stats.service.collector;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import stats.message.ActionTypeProto;
import stats.message.UserActionProto;
import stats.service.collector.kafka.KafkaClient;

import java.time.Instant;

@Slf4j
@GrpcService
@AllArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final KafkaClient kafkaClient;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получены данные от события {}", request);
            UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                    .setUserId(request.getUserId())
                    .setEventId(request.getEventId())
                    .setTimestamp(Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos()))
                    .setActionType(fromActionTypeProto(request.getActionType()))
                    .build();

            kafkaClient.sendData(request.getUserId(), userActionAvro, kafkaClient.getUserActionsTopic());
            log.info(
                    "Отправлено в кафка-топик {} под ключом {} сообщение {}.",
                    kafkaClient.getUserActionsTopic(), request.getUserId(), userActionAvro
            );
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage());
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }

    private ActionTypeAvro fromActionTypeProto(ActionTypeProto type) {
        return switch (type) {
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            default -> null;
        };
    }
}
