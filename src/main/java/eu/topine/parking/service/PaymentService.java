package eu.topine.parking.service;

import org.javamoney.moneta.Money;

import java.time.LocalDateTime;

/**
 * Payment Service : Provides the billing for the requested period based on the application configuration.
 */
public interface PaymentService {

    public Money pricePeriod(LocalDateTime start, LocalDateTime end);
}
