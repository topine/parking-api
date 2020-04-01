package eu.topine.parking.service;

import eu.topine.parking.exception.CarAlreadyParked;
import eu.topine.parking.exception.NoParkingAvailable;
import eu.topine.parking.restservice.v1.model.Car;
import eu.topine.parking.restservice.v1.model.CarType;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.EnumMap;
import java.util.Stack;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@TestPropertySource("/testApplication.properties")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ParkingServiceTest {

    //NOTE : check when https://github.com/spring-projects/spring-framework/issues/18951 will be implemented to have
    // the TestPropertySource support at method level.


    @Autowired
    ParkingService parkingService;

    @Test
    public void testInitialSetup() {
        EnumMap<CarType, Stack<ParkingSpot>> freeSlotsMap = Whitebox.getInternalState(this.parkingService, "freeSlotsMap");

        assertNotNull(freeSlotsMap);
        assertEquals(1, freeSlotsMap.get(CarType.THERMAL).size());
        assertEquals(1, freeSlotsMap.get(CarType.EV20KW).size());
        assertEquals(2, freeSlotsMap.get(CarType.EV50KW).size());
    }

    @Test
    public void testParkCar() {

        var carThermal1 = new Car();
        carThermal1.setRegPlate("CY-078-BY");
        carThermal1.setType(CarType.THERMAL);

        var carThermal2 = new Car();
        carThermal2.setRegPlate("CY-078-BB");
        carThermal2.setType(CarType.THERMAL);

        try {
            var slot = parkingService.parkCar(carThermal1);

            assertNotNull(slot);
            assertEquals(CarType.THERMAL, slot.getAcceptedCarType());
        } catch (Exception e) {
            fail("Exception not expected at this point");
        }

        try {
            parkingService.parkCar(carThermal1);
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals(CarAlreadyParked.class, e.getClass());
        }


        try {
            parkingService.parkCar(carThermal2);
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals(NoParkingAvailable.class, e.getClass());
        }
    }

    @Test
    public void testTakeCar() throws Exception {


        var carThermal1 = new Car();
        carThermal1.setRegPlate("CY-078-BY");
        carThermal1.setType(CarType.THERMAL);


        try {
            parkingService.parkCar(carThermal1);
        } catch (Exception e) {
            fail("Exception not expected at this point");
        }

        Thread.sleep(1000);

        var bill = parkingService.releaseSlot(carThermal1.getRegPlate());

        assertEquals(Money.of(1, "EUR"), bill);
    }
}