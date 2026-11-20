package com.auca.corporatetaxpayer;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Validatable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CorporateTaxpayer extends RegisteredTaxpayer {
    private String companyRegistrationNo;
    private String legalName;
    private LocalDate incorporationDate;
    private String businessSector;
    private String authorizedRepresentative;
    private BigDecimal annualTurnover;

    public CorporateTaxpayer(String partyId, String firstName, String lastName, String email,
                             String phoneNumber, LocalDate dateOfBirth, String tin,
                             LocalDate registrationDate, String taxpayerStatus, String district,
                             String sector, String taxOffice, String companyRegistrationNo,
                             String legalName, LocalDate incorporationDate, String businessSector,
                             String authorizedRepresentative, BigDecimal annualTurnover) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin,
                registrationDate, taxpayerStatus, district, sector, taxOffice);
        this.companyRegistrationNo = companyRegistrationNo;
        this.legalName = legalName;
        this.incorporationDate = incorporationDate;
        this.businessSector = businessSector;
        this.authorizedRepresentative = authorizedRepresentative;
        this.annualTurnover = annualTurnover;
    }

    public String getCompanyRegistrationNo() { return companyRegistrationNo; }
    public String getLegalName() { return legalName; }
    public LocalDate getIncorporationDate() { return incorporationDate; }
    public String getBusinessSector() { return businessSector; }
    public String getAuthorizedRepresentative() { return authorizedRepresentative; }

    @Override
    public BigDecimal getAnnualTurnover() { return annualTurnover; }

    @Override
    public String getPartyRole() { return "Corporate taxpayer (" + businessSector + ")"; }

    @Override
    public String getDisplayName() { return legalName; }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (companyRegistrationNo == null || !companyRegistrationNo.matches("[A-Z0-9-]{5,30}"))
            throw new ValidationException("Company registration no. must be 5-30 uppercase letters/digits/hyphens.");
        if (!Validatable.textOk(legalName, 3, 120)) throw new ValidationException("Legal name must be 3-120 characters.");
        if (incorporationDate == null || !incorporationDate.isBefore(getRegistrationDate()))
            throw new ValidationException("Incorporation date must precede the registration date.");
        if (!Validatable.textOk(businessSector, 2, 60)) throw new ValidationException("Business sector must be 2-60 characters.");
        if (!Validatable.textOk(authorizedRepresentative, 2, 60)) throw new ValidationException("Authorized representative must be 2-60 characters.");
        if (!Validatable.moneyOk(annualTurnover)) throw new ValidationException("Annual turnover must be 0 - 999999999999.99 with max 2 decimals.");
    }

    @Override
    public void printDetails() {
        System.out.println("CORPORATE TAXPAYER");
        super.printDetails();
        System.out.println("  Company reg. no : " + companyRegistrationNo);
        System.out.println("  Legal name      : " + legalName);
        System.out.println("  Incorporated    : " + incorporationDate);
        System.out.println("  Business sector : " + businessSector);
        System.out.println("  Representative  : " + authorizedRepresentative);
        System.out.println("  Annual turnover : " + annualTurnover);
    }

    @Override
    public String getSummary() {
        return "[CORPORATE] " + super.getSummary() + " | turnover " + annualTurnover;
    }
}
