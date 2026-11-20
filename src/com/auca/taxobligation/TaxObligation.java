package com.auca.taxobligation;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxtype.TaxType;
import java.time.LocalDate;

public class TaxObligation implements Identifiable, Validatable {
    private String obligationId;
    private RegisteredTaxpayer taxpayer;
    private TaxType taxType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;

    public TaxObligation(String obligationId, RegisteredTaxpayer taxpayer, TaxType taxType,
                         LocalDate periodStart, LocalDate periodEnd, LocalDate dueDate) {
        this.obligationId = obligationId;
        this.taxpayer = taxpayer;
        this.taxType = taxType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dueDate = dueDate;
    }

    public String getObligationId() { return obligationId; }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public TaxType getTaxType() { return taxType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public LocalDate getDueDate() { return dueDate; }

    @Override
    public String getId() { return obligationId; }

    @Override
    public String getDisplayName() { return taxType.getTaxCode() + " " + periodStart + ".." + periodEnd; }

    @Override
    public boolean isActive() { return taxpayer.isActive() && taxType.isActive(); }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(obligationId, 4, 30)) throw new ValidationException("Obligation ID must be 4-30 characters.");
        if (taxpayer == null || !taxpayer.isActive()) throw new ValidationException("Obligation needs a registered ACTIVE taxpayer.");
        if (taxType == null || !taxType.isActive()) throw new ValidationException("Obligation needs an ACTIVE tax type.");
        if (periodStart == null || periodEnd == null || dueDate == null) throw new InvalidTaxPeriodException("Period dates and due date are required.");
        if (periodStart.isAfter(periodEnd)) throw new InvalidTaxPeriodException("Period start must be on or before period end.");
        if (dueDate.isBefore(periodEnd)) throw new InvalidTaxPeriodException("Due date must not precede the period end.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX OBLIGATION");
        System.out.println("  Obligation ID   : " + obligationId);
        System.out.println("  Taxpayer        : " + taxpayer.getTin() + " " + taxpayer.getDisplayName());
        System.out.println("  Tax type        : " + taxType.getDisplayName());
        System.out.println("  Period start    : " + periodStart);
        System.out.println("  Period end      : " + periodEnd);
        System.out.println("  Due date        : " + dueDate);
    }

    @Override
    public String getSummary() {
        return "[OBLIGATION] " + obligationId + " | TIN " + taxpayer.getTin() + " | " + getDisplayName() + " | due " + dueDate;
    }
}
