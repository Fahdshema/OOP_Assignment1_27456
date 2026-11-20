package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import java.time.LocalDate;

// Abstract level 1: any person/organisation in the system (taxpayer or officer).
public abstract class TaxParty implements Identifiable, Validatable {
    private String partyId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    public TaxParty(String partyId, String firstName, String lastName, String email,
                    String phoneNumber, LocalDate dateOfBirth) {
        this.partyId = partyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getPartyId() { return partyId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public void setEmail(String email) throws ValidationException {
        if (!Validatable.emailOk(email)) throw new ValidationException("Invalid email address.");
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) throws ValidationException {
        if (!Validatable.phoneOk(phoneNumber)) throw new ValidationException("Phone must be 10-15 digits, optional leading +.");
        this.phoneNumber = phoneNumber;
    }

    // Domain extension (abstract-class level): every party describes its own role.
    public abstract String getPartyRole();

    @Override
    public String getId() { return partyId; }

    @Override
    public String getDisplayName() { return firstName + " " + lastName; }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(partyId, 4, 30)) throw new ValidationException("Party ID must be 4-30 characters.");
        if (!Validatable.textOk(firstName, 2, 60)) throw new ValidationException("First name must be 2-60 characters.");
        if (!Validatable.textOk(lastName, 2, 60)) throw new ValidationException("Last name must be 2-60 characters.");
        if (!Validatable.emailOk(email)) throw new ValidationException("Invalid email address.");
        if (!Validatable.phoneOk(phoneNumber)) throw new ValidationException("Phone must be 10-15 digits, optional leading +.");
        if (dateOfBirth == null || !dateOfBirth.isBefore(LocalDate.now()))
            throw new ValidationException("Date of birth must be a past date.");
    }

    @Override
    public void printDetails() {
        System.out.println("  Party ID        : " + partyId);
        System.out.println("  First name      : " + firstName);
        System.out.println("  Last name       : " + lastName);
        System.out.println("  Email           : " + email);
        System.out.println("  Phone           : " + phoneNumber);
        System.out.println("  Date of birth   : " + dateOfBirth);
    }

    @Override
    public String getSummary() {
        return partyId + " | " + getDisplayName() + " | " + getPartyRole();
    }
}
