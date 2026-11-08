package model;

import java.sql.Timestamp;

/**
 * Route Model - Represents a city-to-city travel route.
 */
public class Route {
    private int routeId;
    private String departureCity;
    private String destinationCity;
    private double distanceKm;
    private String estimatedDuration;
    private Timestamp createdAt;

    public Route() {}

    public Route(int routeId, String departureCity, String destinationCity, double distanceKm, String estimatedDuration) {
        this.routeId = routeId;
        this.departureCity = departureCity;
        this.destinationCity = destinationCity;
        this.distanceKm = distanceKm;
        this.estimatedDuration = estimatedDuration;
    }

    public Route(String departureCity, String destinationCity, double distanceKm, String estimatedDuration) {
        this.departureCity = departureCity;
        this.destinationCity = destinationCity;
        this.distanceKm = distanceKm;
        this.estimatedDuration = estimatedDuration;
    }

    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }

    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }

    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return departureCity + " ➔ " + destinationCity + " (" + distanceKm + " km, " + estimatedDuration + ")";
    }
}

