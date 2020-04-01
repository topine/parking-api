package eu.topine.parking.service;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest(PaymentServiceFixedHoursImpl.class)
public class PaymentServiceFixedHoursImplTest {

    PaymentService paymentService;


    @BeforeEach
    public void setUp() {
        this.paymentService = PowerMockito.spy(new PaymentServiceFixedHoursImpl());
        Whitebox.setInternalState(this.paymentService, "pricePerHour", Money.of(2, "USD"));
        Whitebox.setInternalState(this.paymentService, "fixedPrice", Money.of(10, "USD"));
    }

    @Test
    public void testHourCalculation2hrs() {
        var localTime = LocalDateTime.now();
        var localTimeFuture = localTime.plusHours(1).plusMinutes(30);

        assertEquals(Money.of(10 + 4, "USD"), paymentService.pricePeriod(localTime, localTimeFuture));
    }


    @Test
    public void testHourCalculation1week() {
        var localTime = LocalDateTime.now();
        var localTimeFuture = localTime.plusDays(7);

        assertEquals(Money.of(10 + (2 * 24 * 7), "USD"), paymentService.pricePeriod(localTime, localTimeFuture));
    }

}