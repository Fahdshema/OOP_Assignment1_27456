package com.auca.taxdeclaration;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxobligation.TaxObligation;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxDeclaration implements Identifiable, Validatable {
    private String declarationId;
    private TaxObligation obligation;
    private LocalDate filingDate;
    private BigDecimal taxableAmount;
    private BigDecimal declaredTax;
    private String status; // FILED, LATE, ASSESSED, REJECTED

    public TaxDeclaration(String declarationId, TaxObligation obligation, LocalDate filingDate,
                          BigDecimal taxableAmount, BigDecimal declaredTax, String status) {
        this.declarationId = declarationId;
        this.obligation = obligation;
        this.filingDate = filingDate;
        this.taxableAmount = taxableAmount;
        this.declaredTax = declaredTax;
        this.status = status;
    }

    public String getDeclarationId() { return declarationId; }
    public TaxObligation getObligation() { return obligation; }
    public LocalDate getFilingDate() { return filingDate; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public BigDecimal getDeclaredTax() { return declaredTax; }
    public String getStatus() { return status; }

    // true when the declaration was filed after the due date (filing date is never changed)
    public boolean isLate() { return filingDate.isAfter(obligation.getDueDate()); }

    // FILED/LATE -> ASSESSED or REJECTED. ASSESSED and REJECTED are final.
    public void setStatus(String newStatus) throws InvalidStatusTransitionException {
        boolean open = status.equals("FILED") || status.equals("LATE");
        if (!open) throw new InvalidStatusTransitionException("Declaration is " + status + " and cannot change to " + newStatus + ".");
        if (!newStatus.equals("ASSESSED") && !newStatus.equals("REJECTED"))
            throw new InvalidStatusTransitionException("Invalid declaration transition to " + newStatus + ".");
        status = newStatus;
    }

    @Override
    public String getId() { return declarationId; }

    @Override
    public String getDisplayName() { return "Declaration " + declarationId; }

    @Override
    public boolean isActive() { return !"REJECTED".equals(status); }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(declarationId, 4, 30)) throw new ValidationException("Declaration ID must be 4-30 characters.");
        if (obligation == null) throw new ValidationException("Declaration needs an obligation.");
        if (filingDate == null || filingDate.isAfter(LocalDate.now())) throw new ValidationException("Filing date cannot be empty or in the future.");
        if (!Validatable.moneyOk(taxableAmount)) throw new ValidationException("Taxable amount must be 0 - 999999999999.99 with max 2 decimals.");
        if (!Validatable.moneyOk(declaredTax)) throw new ValidationException("Declared tax must be 0 - 999999999999.99 with max 2 decimals.");
        if (!Validatable.inList(status, "FILED", "LATE", "ASSESSED", "REJECTED")) throw new ValidationException("Invalid declaration status.");
        if (isLate() && status.equals("FILED")) throw new ValidationException("A declaration filed after the due date must be marked LATE.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX DECLARATION");
        System.out.println("  Declaration ID  : " + declarationId);
        System.out.println("  Obligation      : " + obligation.getId() + " (" + obligation.getDisplayName() + ")");
        System.out.println("  Filing date     : " + filingDate + (isLate() ? "  [LATE - due " + obligation.getDueDate() + "]" : ""));
        System.out.println("  Taxable amount  : " + taxableAmount);
        System.out.println("  Declared tax    : " + declaredTax);
        System.out.println("  Status          : " + status);
    }

    @Override
    public String getSummary() {
        return "[DECLARATION] " + declarationId + " | obl " + obligation.getId() + " | filed " + filingDate
                + " | tax " + declaredTax + " | " + status;
    }
}
