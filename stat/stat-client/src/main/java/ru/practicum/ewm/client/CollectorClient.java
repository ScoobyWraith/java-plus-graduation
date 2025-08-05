package ru.practicum.ewm.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import stats.message.ActionTypeProto;
import stats.message.UserActionProto;
import stats.service.collector.UserActionControllerGrpc;

import java.time.Instant;

@Slf4j
@Service
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void viewEvent(long userId, long eventId) {
        UserActionProto request = makeRequestProto(userId, eventId, ActionTypeProto.ACTION_VIEW);

        try {
            client.collectUserAction(request);
        } catch (Exception e) {
            log.error("Ошибка при отправке данных в коллектор {}", request, e);
        }
    }

    public void likeEvent(long userId, long eventId) {
        UserActionProto request = makeRequestProto(userId, eventId, ActionTypeProto.ACTION_LIKE);

        try {
            client.collectUserAction(request);
        } catch (Exception e) {
            log.error("Ошибка при отправке данных в коллектор {}", request, e);
        }
    }

    public void registerEvent(long userId, long eventId) {
        UserActionProto request = makeRequestProto(userId, eventId, ActionTypeProto.ACTION_REGISTER);

        try {
            client.collectUserAction(request);
        } catch (Exception e) {
            log.error("Ошибка при отправке данных в коллектор {}", request, e);
        }
    }

    private UserActionProto makeRequestProto(long userId, long eventId, ActionTypeProto type) {
        Timestamp now = Timestamp.newBuilder()
                .setSeconds(Instant.now().getEpochSecond())
                .build();

        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(type)
                .setTimestamp(now)
                .build();
    }
}
