package eu.topine.parking.exception;

public class NoParkingAvailable extends ParkingException {

    public NoParkingAvailable() {
        super("No empty slot available");
    }
}
