package com.movie.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theatres")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "screens")
public class Theatre {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;       // stored lower-case

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Screen> screens = new ArrayList<>();

    public Theatre(String id, String name, String city, String address) {
        this.id      = id;
        this.name    = name;
        this.city    = city.toLowerCase();
        this.address = address;
    }

    public void addScreen(Screen screen) {
        screen.setTheatre(this);
        screens.add(screen);
    }
}
