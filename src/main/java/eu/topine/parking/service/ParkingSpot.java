package eu.topine.parking.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.topine.parking.restservice.v1.model.CarType;
import lombok.Data;

import java.io.Serializable;

@Data
public class ParkingSpot implements Serializable {
    private String id;
    private CarType acceptedCarType;
    @JsonIgnore
    //TODO: why transient is not working?
    private transient Boolean free;

    protected ParkingSpot(String id, CarType acceptedCarType) {
        this.id = id;
        this.acceptedCarType = acceptedCarType;
    }

    protected void setFree(Boolean free) {
        this.free = free;
    }
}
