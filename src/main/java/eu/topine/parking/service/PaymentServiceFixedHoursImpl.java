package eu.topine.parking.service;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Pricing for model Fixed + hours.
 */
public class PaymentServiceFixedHoursImpl implements PaymentService {


    @Value("${fixed.price}")
    private BigDecimal fixedAmount;
    @Value("${hour.price}")
    private BigDecimal hourAmount;
    @Value("${currency}")
    private String currency;

    private Money pricePerHour;
    private Money fixedPrice;

    @PostConstruct
    private void init() {
        pricePerHour = Money.of(hourAmount, currency);
        fixedPrice = Money.of(fixedAmount, currency);
    }


    @Override
    public Money pricePeriod(LocalDateTime start, LocalDateTime end) {
        var duration = Duration.between(start, end);
        return fixedPrice.add(pricePerHour.multiply(Math.ceil(duration.toMinutes() / 60.0)));
    }

}
