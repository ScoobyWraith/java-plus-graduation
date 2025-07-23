package ru.practicum.ewm.commentservice.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.commentservice.model.AdminAction;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class UpdateCommentAdminDto {

    AdminAction action;
}
