package eu.topine.parking.restservice.v1.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
public class Car implements Serializable {

    private CarType type;
    private String regPlate;
}
