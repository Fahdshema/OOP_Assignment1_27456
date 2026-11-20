package com.auca.taxassessment;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxdeclaration.TaxDeclaration;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxAssessment implements Identifiable, Validatable {
    private String assessmentId;
    private TaxDeclaration declaration;
    private BigDecimal assessedTax;
    private BigDecimal adjustmentAmount;
    private LocalDate assessmentDate;
    private String status; // DRAFT, CONFIRMED, CANCELLED

    public TaxAssessment(String assessmentId, TaxDeclaration declaration, BigDecimal assessedTax,
                         BigDecimal adjustmentAmount, LocalDate assessmentDate, String status) {
        this.assessmentId = assessmentId;
        this.declaration = declaration;
        this.assessedTax = assessedTax;
        this.adjustmentAmount = adjustmentAmount;
        this.assessmentDate = assessmentDate;
        this.status = status;
    }

    public String getAssessmentId() { return assessmentId; }
    public TaxDeclaration getDeclaration() { return declaration; }
    public BigDecimal getAssessedTax() { return assessedTax; }
    public BigDecimal getAdjustmentAmount() { return adjustmentAmount; }
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public String getStatus() { return status; }

    // total = assessedTax + adjustmentAmount
    public BigDecimal getTotal() { return assessedTax.add(adjustmentAmount); }

    public void setAdjustmentAmount(BigDecimal newAdjustment) throws ValidationException {
        if (newAdjustment == null || newAdjustment.scale() > 2 || newAdjustment.abs().compareTo(Validatable.MAX_MONEY) > 0)
            throw new ValidationException("Adjustment must have max 2 decimals and be within +/-999999999999.99.");
        if (assessedTax.add(newAdjustment).signum() < 0)
            throw new InvalidTaxCalculationException("Assessment total cannot be negative.");
        if (status.equals("CANCELLED")) throw new InvalidStatusTransitionException("A CANCELLED assessment cannot be adjusted.");
        adjustmentAmount = newAdjustment;
    }

    // DRAFT -> CONFIRMED / CANCELLED ; CONFIRMED -> CANCELLED ; CANCELLED is final.
    public void setStatus(String newStatus) throws InvalidStatusTransitionException {
        boolean ok = (status.equals("DRAFT") && (newStatus.equals("CONFIRMED") || newStatus.equals("CANCELLED")))
                || (status.equals("CONFIRMED") && newStatus.equals("CANCELLED"));
        if (!ok) throw new InvalidStatusTransitionException("Cannot change assessment from " + status + " to " + newStatus + ".");
        status = newStatus;
    }

    @Override
    public String getId() { return assessmentId; }

    @Override
    public String getDisplayName() { return "Assessment " + assessmentId; }

    @Override
    public boolean isActive() { return !"CANCELLED".equals(status); }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(assessmentId, 4, 30)) throw new ValidationException("Assessment ID must be 4-30 characters.");
        if (declaration == null) throw new ValidationException("Assessment needs a declaration.");
        if (declaration.getStatus().equals("REJECTED")) throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed.");
        if (!Validatable.moneyOk(assessedTax)) throw new ValidationException("Assessed tax must be 0 - 999999999999.99 with max 2 decimals.");
        if (adjustmentAmount == null || adjustmentAmount.scale() > 2) throw new ValidationException("Adjustment must have max 2 decimals.");
        if (getTotal().signum() < 0) throw new InvalidTaxCalculationException("Assessment total cannot be negative.");
        if (assessmentDate == null || assessmentDate.isAfter(LocalDate.now())) throw new ValidationException("Assessment date cannot be empty or in the future.");
        if (assessmentDate.isBefore(declaration.getFilingDate())) throw new ValidationException("Assessment date cannot precede the filing date.");
        if (!Validatable.inList(status, "DRAFT", "CONFIRMED", "CANCELLED")) throw new ValidationException("Status must be DRAFT, CONFIRMED or CANCELLED.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX ASSESSMENT");
        System.out.println("  Assessment ID   : " + assessmentId);
        System.out.println("  Declaration     : " + declaration.getId());
        System.out.println("  Assessed tax    : " + assessedTax);
        System.out.println("  Adjustment      : " + adjustmentAmount);
        System.out.println("  Total           : " + getTotal());
        System.out.println("  Assessment date : " + assessmentDate);
        System.out.println("  Status          : " + status);
    }

    @Override
    public String getSummary() {
        return "[ASSESSMENT] " + assessmentId + " | decl " + declaration.getId() + " | total " + getTotal() + " | " + status;
    }
}
