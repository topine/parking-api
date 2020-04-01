package eu.topine.parking.service;

import eu.topine.parking.exception.CarAlreadyParked;
import eu.topine.parking.exception.NoParkingAvailable;
import eu.topine.parking.exception.ParkingException;
import eu.topine.parking.restservice.v1.model.Car;
import eu.topine.parking.restservice.v1.model.CarType;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Scope("singleton")
public class ParkingService implements InitializingBean {

    // will inject the implementation based on the property file selection
    // see: eu/topine/parking/ParkingApiConfig.java:17
    @Autowired
    private PaymentService paymentService;

    //NOSONAR : In this case I really want to use a synchronized structure
    @SuppressWarnings("java:S1149")
    private EnumMap<CarType, Stack<ParkingSpot>> freeSlotsMap = new EnumMap<>(CarType.class);
    private EnumMap<CarType, List<ParkingSpot>> slotsListMap = new EnumMap<>(CarType.class);
    private Map<String, UsedSlot> usedSlots = new HashMap<>();

    @Value("${thermalSpots}")
    private Integer numSpotsThermal;
    @Value("${ev20KWSpots}")
    private Integer ev20KWThermal;
    @Value("${ev50KWSpots}")
    private Integer ev50KWThermal;

    @Data
    @AllArgsConstructor
    private class UsedSlot {
        private Car car;
        private ParkingSpot slot;
        private LocalDateTime parkingTime;
    }

    //prometheus metrics

    public static final String SPOT_TYPE = "spotType";
    //prometheus metrics
    static final Summary parkingBillTotal = Summary.build()
            .name("parking_billing_amount").labelNames(SPOT_TYPE).help("Parking billing").register();

    static final Gauge usedSpotsMonitoring = Gauge.build().name("used_spots").labelNames(SPOT_TYPE)
            .help("Actual number of used spots").register();

    static final Gauge totalSpotsMonitoring = Gauge.build().name("spot_total").labelNames(SPOT_TYPE)
            .help("Total number of spots").register();

    //PostConstruct no longer available in java 11
    @Override
    public void afterPropertiesSet() throws Exception {
        this.initParking(CarType.THERMAL, numSpotsThermal);
        this.initParking(CarType.EV20KW, ev20KWThermal);
        this.initParking(CarType.EV50KW, ev50KWThermal);
    }

    private void initParking(CarType acceptedType, int numSpots) {
        List<ParkingSpot> slots = new ArrayList<>();
        Stack<ParkingSpot> freeStack = new Stack<>();

        for (var i = 1; i <= numSpots; i++) {
            var parkingSlot = new ParkingSpot(String.valueOf(i), acceptedType);
            slots.add(parkingSlot);
            freeStack.add(parkingSlot);
        }

        freeSlotsMap.put(acceptedType, freeStack);
        slotsListMap.put(acceptedType, slots);

        totalSpotsMonitoring.labels(acceptedType.toString()).set(slots.size());
    }


    /**
     * Return the parking slot to park if available.
     *
     * @param car
     * @return
     */
    public ParkingSpot parkCar(Car car) throws ParkingException {

        var freeSlots = freeSlotsMap.get(car.getType());

        // is this true?
        if (null != usedSlots.get(car.getRegPlate())) {
            throw new CarAlreadyParked();
        }

        if (freeSlots == null || freeSlots.isEmpty()) {
            throw new NoParkingAvailable();
        }

        // stacks are syncronized. Pop the first free spot
        var freeSlot = freeSlots.pop();
        freeSlot.setFree(false);
        usedSlots.put(car.getRegPlate(), new UsedSlot(car, freeSlot, LocalDateTime.now()));

        usedSpotsMonitoring.labels(car.getType().toString()).inc();

        return freeSlot;
    }

    /**
     * Release the parking slot and bill the client
     *
     * @param carPlate
     * @return
     */
    public Money releaseSlot(String carPlate) throws Exception {
        var usedSlot = usedSlots.remove(carPlate);

        if (usedSlot == null) {
            throw new Exception("Car not found. Plate : " + carPlate);
        }
        usedSlot.getSlot().setFree(true);
        freeSlotsMap.get(usedSlot.car.getType()).add(usedSlot.slot);

        var bill = paymentService.pricePeriod(usedSlot.getParkingTime(), LocalDateTime.now());
        parkingBillTotal.labels(usedSlot.slot.getAcceptedCarType().toString())
                .observe(bill.getNumberStripped().doubleValue());
        usedSpotsMonitoring.labels(usedSlot.slot.getAcceptedCarType().toString()).dec();

        return bill;
    }


}
