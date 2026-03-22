package com.movie.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "theatre")
public class Screen {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int rows;

    @Column(name = "seats_per_row", nullable = false)
    private int seatsPerRow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    public Screen(String id, String name, int rows, int seatsPerRow) {
        this.id          = id;
        this.name        = name;
        this.rows        = rows;
        this.seatsPerRow = seatsPerRow;
    }
}
