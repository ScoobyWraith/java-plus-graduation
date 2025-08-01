package ru.practicum.ewm.commentservice.dto.parameters;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.commentservice.dto.UpdateCommentDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCommentParameters {

    Long userId;
    Long commentId;
    UpdateCommentDto updateCommentDto;
}
