package ru.practicum.ewm.commentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "text", nullable = false)
    @Size(min = 5, max = 255)
    String text;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "author_id", nullable = false)
    Long authorId;

    @JoinColumn(name = "event_id", nullable = false)
    Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    CommentStatus status = CommentStatus.PENDING;
}
