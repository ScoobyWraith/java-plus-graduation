package ru.practicum.ewm.commentservice.mapper;

import ru.practicum.ewm.commentservice.dto.CommentDto;
import ru.practicum.ewm.commentservice.dto.NewCommentDto;
import ru.practicum.ewm.commentservice.model.Comment;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;

public class CommentMapper {
    public static Comment fromNewCommentDto(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .eventId(comment.getEventId())
                .authorId(comment.getAuthorId())
                .text(comment.getText())
                .status(comment.getStatus())
                .createdOn(comment.getCreatedOn())
                .build();
    }

    public static CommentShortDto toCommentShortDto(Comment comment, String author) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(author)
                .build();
    }
}
