package eu.topine.parking;

import eu.topine.parking.service.PaymentService;
import eu.topine.parking.service.PaymentServiceFixedHoursImpl;
import eu.topine.parking.service.PaymentServiceHourImpl;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//@Controller
@EnableSwagger2
@Configuration
public class ParkingApiConfig {

    @Value("${pricingType}")
    private PricingType pricingType;

    @Bean
    public PaymentService paymentService() {

        PaymentService paymentService;

        switch (pricingType) {
            case HOURS:
                paymentService = new PaymentServiceHourImpl();
                break;
            case FIXED_AND_HOURS:
                paymentService = new PaymentServiceFixedHoursImpl();
                break;
            default:
                throw new ExceptionInInitializerError("Pricing type not available");
        }

        return paymentService;
    }


    public enum PricingType {
        HOURS,
        FIXED_AND_HOURS
    }

    //prometheus endpoint registration
    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        DefaultExports.initialize();
        return new ServletRegistrationBean(new MetricsServlet(), "/metrics");
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("eu.topine.parking.restservice.v1"))
                .paths(PathSelectors.any())
                .build();
    }
}
