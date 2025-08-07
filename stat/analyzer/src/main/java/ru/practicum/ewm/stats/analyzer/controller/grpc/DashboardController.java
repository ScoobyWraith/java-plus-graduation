package ru.practicum.ewm.stats.analyzer.controller.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.analyzer.service.InteractionsService;
import stats.message.InteractionsCountRequestProto;
import stats.message.RecommendedEventProto;
import stats.service.dashboard.RecommendationsControllerGrpc;

import java.util.Map;

@GrpcService
@RequiredArgsConstructor
public class DashboardController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final InteractionsService interactionsService;

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        Map<Long, Double> interactionsSums = interactionsService.getEventInteractionsSums(request.getEventIdList());

        for (long eventId : interactionsSums.keySet()) {
            try {
                RecommendedEventProto event = RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(interactionsSums.get(eventId))
                        .build();
                responseObserver.onNext(event);
            } catch (Exception e) {
                responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
            }
        }

        responseObserver.onCompleted();
    }
}
