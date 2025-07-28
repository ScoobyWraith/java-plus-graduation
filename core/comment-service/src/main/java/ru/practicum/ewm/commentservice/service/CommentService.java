package ru.practicum.ewm.commentservice.service;

import ru.practicum.ewm.commentservice.dto.CommentDto;
import ru.practicum.ewm.commentservice.dto.NewCommentDto;
import ru.practicum.ewm.commentservice.dto.UpdateCommentAdminDto;
import ru.practicum.ewm.commentservice.dto.parameters.GetCommentsForAdminParameters;
import ru.practicum.ewm.commentservice.dto.parameters.GetCommentsParameters;
import ru.practicum.ewm.commentservice.dto.parameters.UpdateCommentParameters;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;

import java.util.List;
import java.util.Map;

public interface CommentService {
    CommentDto createComment(Long userId, NewCommentDto newCommentDto);

    List<CommentDto> getComments(GetCommentsParameters parameters);

    CommentDto getComment(Long commentId, Long userId);

    CommentDto updateComment(UpdateCommentParameters parameters);

    void deleteComment(Long commentId, Long userId);

    List<CommentDto> getCommentsForAdmin(GetCommentsForAdminParameters parameters);

    CommentDto updateCommentByAdmin(long commentId, UpdateCommentAdminDto updateCommentAdminDto);

    List<CommentShortDto> findPageableCommentsForEvent(long eventId, int from, int size);

    List<CommentShortDto> findFirstCommentsForEvent(long eventId, long size);

    Map<Long, Long> getCommentsNumberForEvents(List<Long> eventIds);
}
