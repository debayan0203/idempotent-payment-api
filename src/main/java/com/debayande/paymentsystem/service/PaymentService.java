package com.debayande.paymentsystem.service;

import com.debayande.paymentsystem.entity.Payment;
import com.debayande.paymentsystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Payment processPayment(Payment payment) {
        String lockKey = "payment:lock:" + payment.getIdempotencyKey();

        Boolean isNewRequest = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "PROCESSING", Duration.ofHours(24));

        if (Boolean.FALSE.equals(isNewRequest)) {
            throw new RuntimeException("Duplicate");
        }

        payment.setStatus("COMPLETED");

        try {
            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Duplicate");
        }
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
}