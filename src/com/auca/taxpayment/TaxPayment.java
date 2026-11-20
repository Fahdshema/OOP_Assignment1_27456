package com.auca.taxpayment;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxassessment.TaxAssessment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxPayment implements Identifiable, Validatable {
    private String paymentId;
    private TaxAssessment assessment;
    private String paymentReference;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;

    public TaxPayment(String paymentId, TaxAssessment assessment, String paymentReference,
                      BigDecimal amount, LocalDate paymentDate, String paymentMethod) {
        this.paymentId = paymentId;
        this.assessment = assessment;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentId() { return paymentId; }
    public TaxAssessment getAssessment() { return assessment; }
    public String getPaymentReference() { return paymentReference; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }

    @Override
    public String getId() { return paymentId; }

    @Override
    public String getDisplayName() { return "Payment " + paymentReference; }

    @Override
    public boolean isActive() { return true; }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(paymentId, 4, 30)) throw new ValidationException("Payment ID must be 4-30 characters.");
        if (assessment == null) throw new ValidationException("Payment needs an assessment.");
        if (!assessment.getStatus().equals("CONFIRMED")) throw new ValidationException("Payments require a CONFIRMED assessment.");
        if (!Validatable.textOk(paymentReference, 4, 40)) throw new ValidationException("Payment reference must be 4-40 characters.");
        if (!Validatable.moneyOk(amount) || amount.signum() <= 0) throw new ValidationException("Payment amount must be greater than zero (max 2 decimals).");
        if (paymentDate == null || paymentDate.isAfter(LocalDate.now())) throw new ValidationException("Payment date cannot be empty or in the future.");
        if (!Validatable.inList(paymentMethod, "CASH", "BANK", "MOBILE")) throw new ValidationException("Method must be CASH, BANK or MOBILE.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX PAYMENT");
        System.out.println("  Payment ID      : " + paymentId);
        System.out.println("  Assessment      : " + assessment.getId());
        System.out.println("  Reference       : " + paymentReference);
        System.out.println("  Amount          : " + amount);
        System.out.println("  Payment date    : " + paymentDate);
        System.out.println("  Method          : " + paymentMethod);
    }

    @Override
    public String getSummary() {
        return "[PAYMENT] " + paymentId + " | ref " + paymentReference + " | assess " + assessment.getId()
                + " | " + amount + " | " + paymentDate;
    }
}
