package ru.practicum.ewm.stats.analyzer.controller.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.analyzer.service.InteractionsService;
import ru.practicum.ewm.stats.analyzer.service.RecommendationsService;
import ru.practicum.ewm.stats.analyzer.service.SimilarEventsService;
import ru.practicum.ewm.stats.proto.messages.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.messages.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.messages.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.messages.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.proto.services.RecommendationsControllerGrpc;

import java.util.Map;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationsService recommendationsService;
    private final InteractionsService interactionsService;
    private final SimilarEventsService similarEventsService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        Map<Long, Double> recommendations = recommendationsService
                .getRecommendations(request.getUserId(), request.getMaxResults());

        for (long eventId : recommendations.keySet()) {
            try {
                RecommendedEventProto event = RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(recommendations.get(eventId))
                        .build();
                responseObserver.onNext(event);
            } catch (Exception e) {
                responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        Map<Long, Double> similarEvents = similarEventsService
                .getSimilarEvents(request.getUserId(), request.getEventId(), request.getMaxResults());

        for (long eventId : similarEvents.keySet()) {
            try {
                RecommendedEventProto event = RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(similarEvents.get(eventId))
                        .build();
                responseObserver.onNext(event);
            } catch (Exception e) {
                responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
            }
        }

        responseObserver.onCompleted();
    }

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
