package com.dangbooking.tour.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tour_packages")
public class TourPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tour_id", nullable = false)
    private Integer tourId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer price;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTourId() { return tourId; }
    public void setTourId(Integer tourId) { this.tourId = tourId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
