package com.auca.taxrefund;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxRefund implements Identifiable, Validatable {
    private String refundId;
    private RegisteredTaxpayer taxpayer;
    private TaxAssessment assessment;
    private BigDecimal requestedAmount;
    private LocalDate requestDate;
    private String status; // PENDING, APPROVED, REJECTED
    private RevenueOfficer decidedBy; // extra field: officer who decided

    public TaxRefund(String refundId, RegisteredTaxpayer taxpayer, TaxAssessment assessment,
                     BigDecimal requestedAmount, LocalDate requestDate, String status) {
        this.refundId = refundId;
        this.taxpayer = taxpayer;
        this.assessment = assessment;
        this.requestedAmount = requestedAmount;
        this.requestDate = requestDate;
        this.status = status;
        this.decidedBy = null;
    }

    public String getRefundId() { return refundId; }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public TaxAssessment getAssessment() { return assessment; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public LocalDate getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
    public RevenueOfficer getDecidedBy() { return decidedBy; }

    // PENDING -> APPROVED or REJECTED only
    public void decide(String newStatus, RevenueOfficer officer) throws InvalidStatusTransitionException {
        if (!status.equals("PENDING"))
            throw new InvalidStatusTransitionException("Refund is already " + status + ".");
        if (!newStatus.equals("APPROVED") && !newStatus.equals("REJECTED"))
            throw new InvalidStatusTransitionException("Invalid refund status " + newStatus + ".");
        status = newStatus;
        decidedBy = officer;
    }

    @Override
    public String getId() { return refundId; }

    @Override
    public String getDisplayName() { return "Refund " + refundId; }

    @Override
    public boolean isActive() { return !"REJECTED".equals(status); }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(refundId, 4, 30)) throw new ValidationException("Refund ID must be 4-30 characters.");
        if (taxpayer == null) throw new ValidationException("Refund needs a taxpayer.");
        if (assessment == null) throw new ValidationException("Refund needs an assessment.");
        if (!Validatable.moneyOk(requestedAmount) || requestedAmount.signum() <= 0)
            throw new ValidationException("Requested refund must be greater than zero (max 2 decimals).");
        if (requestDate == null || requestDate.isAfter(LocalDate.now())) throw new ValidationException("Request date cannot be empty or in the future.");
        if (!Validatable.inList(status, "PENDING", "APPROVED", "REJECTED")) throw new ValidationException("Refund status must be PENDING, APPROVED or REJECTED.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX REFUND");
        System.out.println("  Refund ID       : " + refundId);
        System.out.println("  Taxpayer        : " + taxpayer.getTin() + " " + taxpayer.getDisplayName());
        System.out.println("  Assessment      : " + assessment.getId());
        System.out.println("  Requested amount: " + requestedAmount);
        System.out.println("  Request date    : " + requestDate);
        System.out.println("  Status          : " + status);
        System.out.println("  Decided by      : " + (decidedBy == null ? "-" : decidedBy.getStaffNumber()));
    }

    @Override
    public String getSummary() {
        return "[REFUND] " + refundId + " | TIN " + taxpayer.getTin() + " | assess " + assessment.getId()
                + " | " + requestedAmount + " | " + status;
    }
}
