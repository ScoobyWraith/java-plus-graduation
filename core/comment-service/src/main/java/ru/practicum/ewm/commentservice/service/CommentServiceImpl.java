package ru.practicum.ewm.commentservice.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.commentservice.dto.CommentDto;
import ru.practicum.ewm.commentservice.dto.NewCommentDto;
import ru.practicum.ewm.commentservice.dto.UpdateCommentAdminDto;
import ru.practicum.ewm.commentservice.dto.UpdateCommentDto;
import ru.practicum.ewm.commentservice.dto.parameters.GetCommentsForAdminParameters;
import ru.practicum.ewm.commentservice.dto.parameters.GetCommentsParameters;
import ru.practicum.ewm.commentservice.dto.parameters.UpdateCommentParameters;
import ru.practicum.ewm.commentservice.mapper.CommentMapper;
import ru.practicum.ewm.commentservice.model.AdminAction;
import ru.practicum.ewm.commentservice.model.Comment;
import ru.practicum.ewm.commentservice.model.CommentStatus;
import ru.practicum.ewm.commentservice.model.QComment;
import ru.practicum.ewm.commentservice.storage.CommentRepository;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;
import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.common.dto.request.RequestStatus;
import ru.practicum.ewm.common.dto.user.GetUserShortRequest;
import ru.practicum.ewm.common.dto.user.UserShortDto;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.exception.ValidationException;
import ru.practicum.ewm.common.interaction.RequestClient;
import ru.practicum.ewm.common.interaction.UserClient;
import ru.practicum.ewm.common.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    private final UserClient userClient;
    private final RequestClient requestClient;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto newCommentDto) {
        Long eventId = newCommentDto.getEventId();
        RequestShortDto request = requestClient.findByRequesterIdAndEventId(userId, eventId);

        if (request == null || !request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new ValidationException("You cannot leave a comment because " +
                    "you did not leave a request to participate or your request was rejected.");
        }

        if (commentRepository.existsByAuthorIdAndEventId(userId, eventId)) {
            throw new ConflictException("You can leave a comment only once.");
        }

        Comment comment = CommentMapper.fromNewCommentDto(newCommentDto);
        comment.setAuthorId(userId);
        comment.setEventId(eventId);
        comment.setCreatedOn(Util.getNowTruncatedToSeconds());

        log.info("Created comment for userId={}, eventId={}", userId, newCommentDto.getEventId());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getComments(GetCommentsParameters parameters) {
        QComment comment = QComment.comment;
        List<BooleanExpression> conditions = new ArrayList<>();
        Pageable page = createPageableObject(parameters.getFrom(), parameters.getSize());

        conditions.add(comment.authorId.eq(parameters.getUserId()));

        if (parameters.getEventIds() != null && !parameters.getEventIds().isEmpty()) {
            conditions.add(comment.eventId.in(parameters.getEventIds()));
        }

        if (parameters.getStatus() != null) {
            conditions.add(comment.status.eq(parameters.getStatus()));
        }

        BooleanExpression condition = conditions.stream()
                .reduce(Expressions.asBoolean(true).isTrue(), BooleanExpression::and);

        return commentRepository.findAll(condition, page)
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public CommentDto getComment(Long commentId, Long userId) {
        Comment comment = getCommentWithCheck(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Only author can see comment.");
        }

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(UpdateCommentParameters parameters) {
        Comment comment = getCommentWithCheck(parameters.getCommentId());

        if (!comment.getAuthorId().equals(parameters.getUserId())) {
            throw new ValidationException("Only author can update comment.");
        }

        if (comment.getStatus() == CommentStatus.PENDING) {
            throw new ValidationException("Cannot edit comment while it is pending moderation.");
        }

        UpdateCommentDto updateDto = parameters.getUpdateCommentDto();
        comment.setText(updateDto.getText());
        comment.setStatus(CommentStatus.PENDING);

        log.info("Updated comment id={} for userId={}", parameters.getCommentId(), parameters.getUserId());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getCommentWithCheck(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Only author can delete his comment.");
        }

        commentRepository.delete(comment);
        log.info("Deleted comment id={} for userId={}", commentId, userId);
    }

    @Override
    public List<CommentDto> getCommentsForAdmin(GetCommentsForAdminParameters parameters) {
        CommentStatus status = parameters.getStatus();
        Pageable pageable = createPageableObject(parameters.getFrom(), parameters.getSize());
        return commentRepository.findPageableCommentsForAdmin(status, pageable).stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public CommentDto updateCommentByAdmin(long commentId, UpdateCommentAdminDto updateCommentAdminDto) {
        Comment comment = getCommentWithCheck(commentId);
        AdminAction action = updateCommentAdminDto.getAction();

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Comment must has status PENDING.");
        }

        comment.setStatus(action == AdminAction.APPROVE ? CommentStatus.APPROVE : CommentStatus.REJECT);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentShortDto> findPageableCommentsForEvent(long eventId, int from, int size) {
        List<Comment> comments = commentRepository.findPageableCommentsForEvent(eventId, from, size);
        List<Long> authorsId = comments.stream()
                .map(Comment::getAuthorId)
                .toList();
        Map<Long, UserShortDto> authorsShortMap = userClient
                .getUsersShort(new GetUserShortRequest(authorsId));

        return comments
                .stream()
                .map(comment -> CommentMapper.toCommentShortDto(comment, authorsShortMap.get(comment.getAuthorId()).getName()))
                .toList();
    }

    @Override
    public List<CommentShortDto> findFirstCommentsForEvent(long eventId, long size) {
        List<Comment> comments = commentRepository.findFirstCommentsForEvent(eventId, size);
        List<Long> authorsId = comments.stream()
                .map(Comment::getAuthorId)
                .toList();
        Map<Long, UserShortDto> authorsShortMap = userClient
                .getUsersShort(new GetUserShortRequest(authorsId));

        return comments.stream()
                .map(comment -> CommentMapper.toCommentShortDto(comment, authorsShortMap.get(comment.getAuthorId()).getName()))
                .toList();
    }

    @Override
    public Map<Long, Long> getCommentsNumberForEvents(List<Long> eventIds) {
        Map<Long, Long> commentsNumberMap = commentRepository.getCommentsNumberForEvents(eventIds).stream()
                .collect(Collectors.toMap(List::getFirst, List::getLast));

        return eventIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> commentsNumberMap.getOrDefault(id, 0L)));
    }

    private Comment getCommentWithCheck(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment id=%d not found.", commentId)));
    }

    private Pageable createPageableObject(Integer from, Integer size) {
        return PageRequest.of(from / size, size);
    }
}
