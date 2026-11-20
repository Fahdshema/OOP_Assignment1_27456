package com.auca.abstracts;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Validatable;
import java.math.BigDecimal;
import java.time.LocalDate;

// Abstract level 2: a taxpayer that has a TIN. Individual and Corporate extend it.
public abstract class RegisteredTaxpayer extends TaxParty {
    private String tin;
    private LocalDate registrationDate;
    private String taxpayerStatus;
    private String district;
    private String sector;
    private String taxOffice;

    public RegisteredTaxpayer(String partyId, String firstName, String lastName, String email,
                              String phoneNumber, LocalDate dateOfBirth, String tin,
                              LocalDate registrationDate, String taxpayerStatus,
                              String district, String sector, String taxOffice) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth); // constructor chaining
        this.tin = tin;
        this.registrationDate = registrationDate;
        this.taxpayerStatus = taxpayerStatus;
        this.district = district;
        this.sector = sector;
        this.taxOffice = taxOffice;
    }

    public String getTin() { return tin; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String getTaxpayerStatus() { return taxpayerStatus; }
    public String getDistrict() { return district; }
    public String getSector() { return sector; }
    public String getTaxOffice() { return taxOffice; }

    // Domain extension: each taxpayer type gives its own annual turnover.
    public abstract BigDecimal getAnnualTurnover();

    // Allowed: ACTIVE <-> SUSPENDED, anything -> CLOSED. CLOSED is final.
    public void setTaxpayerStatus(String newStatus) throws InvalidStatusTransitionException {
        if (!Validatable.inList(newStatus, "ACTIVE", "SUSPENDED", "CLOSED"))
            throw new InvalidStatusTransitionException("Unknown taxpayer status: " + newStatus);
        if (taxpayerStatus.equals("CLOSED"))
            throw new InvalidStatusTransitionException("A CLOSED taxpayer cannot change status.");
        if (taxpayerStatus.equals(newStatus))
            throw new InvalidStatusTransitionException("Taxpayer is already " + newStatus + ".");
        taxpayerStatus = newStatus;
    }

    @Override
    public boolean isActive() { return "ACTIVE".equals(taxpayerStatus); }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (!Validatable.digitsOk(tin, 9)) throw new ValidationException("TIN must be exactly 9 digits.");
        if (registrationDate == null || registrationDate.isAfter(LocalDate.now()))
            throw new ValidationException("Registration date cannot be empty or in the future.");
        if (!getDateOfBirth().isBefore(registrationDate))
            throw new ValidationException("Date of birth must precede the registration date.");
        if (!Validatable.inList(taxpayerStatus, "ACTIVE", "SUSPENDED", "CLOSED"))
            throw new ValidationException("Taxpayer status must be ACTIVE, SUSPENDED or CLOSED.");
        if (!Validatable.textOk(district, 2, 40)) throw new ValidationException("District must be 2-40 characters.");
        if (!Validatable.textOk(sector, 2, 40)) throw new ValidationException("Sector must be 2-40 characters.");
        if (!Validatable.textOk(taxOffice, 2, 60)) throw new ValidationException("Tax office must be 2-60 characters.");
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("  TIN             : " + tin);
        System.out.println("  Registered on   : " + registrationDate);
        System.out.println("  Status          : " + taxpayerStatus);
        System.out.println("  District        : " + district);
        System.out.println("  Sector          : " + sector);
        System.out.println("  Tax office      : " + taxOffice);
    }

    @Override
    public String getSummary() {
        return "TIN " + tin + " | " + getDisplayName() + " | " + taxpayerStatus;
    }
}
