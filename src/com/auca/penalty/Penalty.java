package com.auca.penalty;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxobligation.TaxObligation;
import java.math.BigDecimal;

public class Penalty implements Identifiable, Validatable {
    private String penaltyId;
    private TaxObligation obligation;
    private String penaltyType;
    private BigDecimal baseAmount;
    private BigDecimal penaltyRate;
    private BigDecimal penaltyAmount;

    public Penalty(String penaltyId, TaxObligation obligation, String penaltyType,
                   BigDecimal baseAmount, BigDecimal penaltyRate, BigDecimal penaltyAmount) {
        this.penaltyId = penaltyId;
        this.obligation = obligation;
        this.penaltyType = penaltyType;
        this.baseAmount = baseAmount;
        this.penaltyRate = penaltyRate;
        this.penaltyAmount = penaltyAmount;
    }

    public String getPenaltyId() { return penaltyId; }
    public TaxObligation getObligation() { return obligation; }
    public String getPenaltyType() { return penaltyType; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }

    @Override
    public String getId() { return penaltyId; }

    @Override
    public String getDisplayName() { return penaltyType + " penalty " + penaltyId; }

    @Override
    public boolean isActive() { return true; }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(penaltyId, 4, 30)) throw new ValidationException("Penalty ID must be 4-30 characters.");
        if (obligation == null) throw new ValidationException("Penalty needs an obligation.");
        if (!Validatable.inList(penaltyType, "LATE_FILING")) throw new ValidationException("Penalty type must be LATE_FILING.");
        if (!Validatable.moneyOk(baseAmount)) throw new ValidationException("Base amount must be 0 - 999999999999.99 with max 2 decimals.");
        if (penaltyRate == null || penaltyRate.signum() < 0 || penaltyRate.compareTo(new BigDecimal("100")) > 0)
            throw new ValidationException("Penalty rate must be between 0 and 100.");
        if (!Validatable.moneyOk(penaltyAmount)) throw new ValidationException("Penalty amount is invalid.");
    }

    @Override
    public void printDetails() {
        System.out.println("PENALTY");
        System.out.println("  Penalty ID      : " + penaltyId);
        System.out.println("  Obligation      : " + obligation.getId());
        System.out.println("  Penalty type    : " + penaltyType);
        System.out.println("  Base amount     : " + baseAmount);
        System.out.println("  Penalty rate %  : " + penaltyRate);
        System.out.println("  Penalty amount  : " + penaltyAmount);
    }

    @Override
    public String getSummary() {
        return "[PENALTY] " + penaltyId + " | obl " + obligation.getId() + " | " + penaltyType + " | " + penaltyAmount;
    }
}
