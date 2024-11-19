package com.scalable.shoppify.model;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetail {
    private String cardNumber;
    private LocalDate cardExpiry;
    private Integer cvv;
    private Integer upiNumber;

}
