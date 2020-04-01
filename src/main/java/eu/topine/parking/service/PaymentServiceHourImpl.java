package eu.topine.parking.service;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Pricing for billing model hours.
 */
public class PaymentServiceHourImpl implements PaymentService {

    @Value("${hour.price}")
    private BigDecimal amount;
    @Value("${currency}")
    private String currency;

    private Money pricePerHour;

    @PostConstruct
    private void init() {
        pricePerHour = Money.of(amount, currency);
    }

    @Override
    public Money pricePeriod(LocalDateTime start, LocalDateTime end) {
        var duration = Duration.between(start, end);
        return pricePerHour.multiply(Math.ceil(duration.toMillis() / 3600000.0));
    }
}
