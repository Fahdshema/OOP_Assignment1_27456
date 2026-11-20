package com.auca.taxtype;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import java.math.BigDecimal;

public class TaxType implements Identifiable, Validatable {
    private String taxTypeId;
    private String taxCode;
    private String taxName;
    private BigDecimal rate;
    private String filingFrequency;
    private boolean active;

    public TaxType(String taxTypeId, String taxCode, String taxName, BigDecimal rate,
                   String filingFrequency, boolean active) {
        this.taxTypeId = taxTypeId;
        this.taxCode = taxCode;
        this.taxName = taxName;
        this.rate = rate;
        this.filingFrequency = filingFrequency;
        this.active = active;
    }

    public String getTaxTypeId() { return taxTypeId; }
    public String getTaxCode() { return taxCode; }
    public String getTaxName() { return taxName; }
    public BigDecimal getRate() { return rate; }
    public String getFilingFrequency() { return filingFrequency; }

    public void setActive(boolean active) { this.active = active; }

    public void setRate(BigDecimal rate) throws ValidationException {
        if (rate == null || rate.signum() < 0 || rate.compareTo(new BigDecimal("100")) > 0)
            throw new ValidationException("Rate must be between 0 and 100.");
        this.rate = rate;
    }

    @Override
    public String getId() { return taxTypeId; }

    @Override
    public String getDisplayName() { return taxName + " (" + taxCode + ")"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(taxTypeId, 3, 20)) throw new ValidationException("Tax type ID must be 3-20 characters.");
        if (taxCode == null || !taxCode.matches("[A-Z0-9]{2,10}")) throw new ValidationException("Tax code must be 2-10 uppercase letters/digits.");
        if (!Validatable.textOk(taxName, 3, 60)) throw new ValidationException("Tax name must be 3-60 characters.");
        if (rate == null || rate.signum() < 0 || rate.compareTo(new BigDecimal("100")) > 0)
            throw new ValidationException("Rate must be between 0 and 100.");
        if (!Validatable.inList(filingFrequency, "MONTHLY", "QUARTERLY", "ANNUAL"))
            throw new ValidationException("Filing frequency must be MONTHLY, QUARTERLY or ANNUAL.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX TYPE");
        System.out.println("  Tax type ID     : " + taxTypeId);
        System.out.println("  Tax code        : " + taxCode);
        System.out.println("  Tax name        : " + taxName);
        System.out.println("  Rate (%)        : " + rate);
        System.out.println("  Filing frequency: " + filingFrequency);
        System.out.println("  Active          : " + (active ? "YES" : "NO"));
    }

    @Override
    public String getSummary() {
        return "[TAX TYPE] " + taxTypeId + " | " + getDisplayName() + " | " + rate + "% | "
                + filingFrequency + " | " + (active ? "ACTIVE" : "INACTIVE");
    }
}
