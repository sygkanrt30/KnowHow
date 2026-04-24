package ru.tbank.knowhow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "moderator_load")
@Getter
@Setter
@NoArgsConstructor
public class ModeratorLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "moderator_id", nullable = false, unique = true)
    private User moderator;

    @Column(name = "courses_in_moderation", nullable = false)
    private Integer coursesInModeration = 0;
}
