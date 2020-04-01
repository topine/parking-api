package eu.topine.parking.restservice.v1;

import eu.topine.parking.exception.ParkingException;
import eu.topine.parking.restservice.v1.model.Car;
import eu.topine.parking.service.ParkingService;
import eu.topine.parking.service.ParkingSpot;
import io.prometheus.client.Summary;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/v1")
@Api(tags = {"Parking API"})
@SwaggerDefinition(tags = {
        @Tag(name = "Parking API", description = "Parking API Services")
})
public class ParkingAPIController {

    private final ParkingService parkingService;

    //Implicit constructor injection
    public ParkingAPIController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    //prometheus metrics
    static final Summary requestLatency = Summary.build()
            .name("requests_latency_seconds").help("Request latency in seconds.").labelNames("service").register();

    @ApiOperation(value = "Lock a spot for parking", response = ParkingSpot.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Car successfully parked"),
            @ApiResponse(code = 406, message = "Impossible to lock a spot.")
    }
    )
    @PostMapping("/parkcar")
    public ResponseEntity<Object> parkCar(@RequestBody Car car) throws ParkingException {
        //TODO: use an handler interceptor
        Summary.Timer requestTimer = requestLatency.labels("parkcar").startTimer();
        try {
            return ResponseEntity.ok(parkingService.parkCar(car));
        } finally {
            requestTimer.observeDuration();
        }
    }

    @ApiOperation(value = "Take out the car from the parking and receive amount to be paid.", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Car checkout done"),
            @ApiResponse(code = 500, message = "Internal error")
    }
    )
    @PostMapping("/takecar")
    public String takeCar(@RequestBody Car car) throws Exception {
        Summary.Timer requestTimer = requestLatency.labels("takecar").startTimer();
        try {
            return "Parking price : " + parkingService.releaseSlot(car.getRegPlate()).toString();
        } finally {
            requestTimer.observeDuration();
        }
    }

    @ExceptionHandler(ParkingException.class)
    public void handleContentNotAllowedException(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_ACCEPTABLE.value());
    }
}