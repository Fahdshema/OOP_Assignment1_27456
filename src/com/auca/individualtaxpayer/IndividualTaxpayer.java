package com.auca.individualtaxpayer;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Validatable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class IndividualTaxpayer extends RegisteredTaxpayer {
    private String nationalId;
    private String occupation;
    private String businessName;
    private LocalDate businessStartDate;
    private BigDecimal annualTurnover;
    private boolean resident;

    public IndividualTaxpayer(String partyId, String firstName, String lastName, String email,
                              String phoneNumber, LocalDate dateOfBirth, String tin,
                              LocalDate registrationDate, String taxpayerStatus, String district,
                              String sector, String taxOffice, String nationalId, String occupation,
                              String businessName, LocalDate businessStartDate,
                              BigDecimal annualTurnover, boolean resident) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin,
                registrationDate, taxpayerStatus, district, sector, taxOffice);
        this.nationalId = nationalId;
        this.occupation = occupation;
        this.businessName = businessName;
        this.businessStartDate = businessStartDate;
        this.annualTurnover = annualTurnover;
        this.resident = resident;
    }

    public String getNationalId() { return nationalId; }
    public String getOccupation() { return occupation; }
    public String getBusinessName() { return businessName; }
    public LocalDate getBusinessStartDate() { return businessStartDate; }
    public boolean isResident() { return resident; }

    @Override
    public BigDecimal getAnnualTurnover() { return annualTurnover; }

    @Override
    public String getPartyRole() {
        return "Individual taxpayer (" + (resident ? "resident" : "non-resident") + ")";
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (" + businessName + ")";
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (!Validatable.digitsOk(nationalId, 16)) throw new ValidationException("National ID must be exactly 16 digits.");
        if (!Validatable.textOk(occupation, 2, 60)) throw new ValidationException("Occupation must be 2-60 characters.");
        if (!Validatable.textOk(businessName, 2, 80)) throw new ValidationException("Business name must be 2-80 characters.");
        if (businessStartDate == null || !businessStartDate.isBefore(getRegistrationDate()))
            throw new ValidationException("Business start date must precede the registration date.");
        if (!Validatable.moneyOk(annualTurnover)) throw new ValidationException("Annual turnover must be 0 - 999999999999.99 with max 2 decimals.");
    }

    @Override
    public void printDetails() {
        System.out.println("INDIVIDUAL TAXPAYER");
        super.printDetails();
        System.out.println("  National ID     : " + nationalId);
        System.out.println("  Occupation      : " + occupation);
        System.out.println("  Business name   : " + businessName);
        System.out.println("  Business start  : " + businessStartDate);
        System.out.println("  Annual turnover : " + annualTurnover);
        System.out.println("  Resident        : " + (resident ? "YES" : "NO"));
    }

    @Override
    public String getSummary() {
        return "[INDIVIDUAL] " + super.getSummary() + " | turnover " + annualTurnover;
    }
}
