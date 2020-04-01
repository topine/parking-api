package eu.topine.parking.exception;

public class CarAlreadyParked extends ParkingException {

    public CarAlreadyParked() {
        super("Car Already parked");
    }
}
