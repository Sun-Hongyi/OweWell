package owewell.api;

import owewell.domain.Payment;

public record PaymentDto(String from, String to, long amountCents, String currency) {

    public static PaymentDto from(Payment payment) {
        return new PaymentDto(payment.from().value(), payment.to().value(),
            payment.amount().cents(), payment.amount().currency());
    }
}
