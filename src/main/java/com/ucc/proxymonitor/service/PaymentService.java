package com.ucc.proxymonitor.service;

import com.ucc.proxymonitor.exception.BusinessRuleException;
import com.ucc.proxymonitor.exception.PaymentGatewayException;
import com.ucc.proxymonitor.exception.ResourceNotFoundException;
import com.ucc.proxymonitor.service.model.Payment;
import com.ucc.proxymonitor.service.model.PaymentStatus;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Real subject: payments microservice.
 * Operations: processPayment, getPayment, refundPayment.
 * A FailureInjector makes a percentage of the calls fail on purpose (10% by default).
 */
public class PaymentService extends AbstractMicroservice {

    public static final String ID = "payments";

    private final Map<String, Payment> payments = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();
    private final FailureInjector failureInjector;

    public PaymentService(LatencySimulator latency, FailureInjector failureInjector) {
        super(ID, latency);
        this.failureInjector = failureInjector;
        registerOperation("processPayment", args -> processPayment(
                args.getString("orderId"), args.getPositiveDouble("amount"), args.getString("method", "CARD")));
        registerOperation("getPayment", args -> findPayment(args.getString("paymentId")));
        registerOperation("refundPayment", args -> refundPayment(args.getString("paymentId")));
    }

    @Override
    protected void beforeOperation(String operation) {
        if (failureInjector.shouldFail()) {
            throw new PaymentGatewayException("Simulated payment gateway failure while executing '" + operation + "'");
        }
    }

    private Payment processPayment(String orderId, double amount, String method) {
        String id = "PAY-" + sequence.incrementAndGet();
        Payment payment = new Payment(id, orderId, amount, method, PaymentStatus.APPROVED, Instant.now());
        payments.put(id, payment);
        return payment;
    }

    private Payment findPayment(String paymentId) {
        Payment payment = payments.get(paymentId);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment", paymentId);
        }
        return payment;
    }

    private Payment refundPayment(String paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.status() == PaymentStatus.REFUNDED) {
            throw new BusinessRuleException("Payment " + paymentId + " is already refunded");
        }
        Payment refunded = payment.withStatus(PaymentStatus.REFUNDED);
        payments.put(paymentId, refunded);
        return refunded;
    }
}
