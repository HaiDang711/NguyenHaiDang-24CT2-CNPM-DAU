package com.dangbooking.tour.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tours")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String place;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String rating;

    @Column(name = "price_from", nullable = false)
    private Integer priceFrom;

    @Column(name = "color_from", length = 20)
    private String colorFrom = "#14A098";

    @Column(name = "color_to", length = 20)
    private String colorTo = "#0E4749";

    // Danh sach diem noi bat, phan cach bang dau phay (giong PHP)
    @Column(columnDefinition = "TEXT")
    private String highlights;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public Integer getPriceFrom() { return priceFrom; }
    public void setPriceFrom(Integer priceFrom) { this.priceFrom = priceFrom; }

    public String getColorFrom() { return colorFrom; }
    public void setColorFrom(String colorFrom) { this.colorFrom = colorFrom; }

    public String getColorTo() { return colorTo; }
    public void setColorTo(String colorTo) { this.colorTo = colorTo; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String highlights) { this.highlights = highlights; }
}
