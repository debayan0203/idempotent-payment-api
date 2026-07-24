package com.debayande.paymentsystem.controller;

import com.debayande.paymentsystem.entity.Payment;
import com.debayande.paymentsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/postPayment")
    public ResponseEntity<?> postPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Payment paymentRequest) {

        paymentRequest.setIdempotencyKey(idempotencyKey);

        try {
            Payment savedPayment = paymentService.processPayment(paymentRequest);
            return ResponseEntity.ok(savedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Payment already processed for this key.");
        }
    }

    @GetMapping("/getPayment/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);

        if (payment.isPresent()) {
            return ResponseEntity.ok(payment.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Payment not found.");
        }
    }
}