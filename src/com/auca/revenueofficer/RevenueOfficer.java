package com.auca.revenueofficer;

import com.auca.abstracts.TaxParty;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Validatable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueOfficer extends TaxParty {
    private String staffNumber;
    private String department;
    private String role;
    private LocalDate hireDate;
    private BigDecimal approvalLimit;
    private boolean active;

    public RevenueOfficer(String partyId, String firstName, String lastName, String email,
                          String phoneNumber, LocalDate dateOfBirth, String staffNumber,
                          String department, String role, LocalDate hireDate,
                          BigDecimal approvalLimit, boolean active) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = staffNumber;
        this.department = department;
        this.role = role;
        this.hireDate = hireDate;
        this.approvalLimit = approvalLimit;
        this.active = active;
    }

    public String getStaffNumber() { return staffNumber; }
    public String getDepartment() { return department; }
    public String getRole() { return role; }
    public LocalDate getHireDate() { return hireDate; }
    public BigDecimal getApprovalLimit() { return approvalLimit; }

    public void setActive(boolean active) { this.active = active; }

    public void setApprovalLimit(BigDecimal limit) throws ValidationException {
        if (!Validatable.moneyOk(limit)) throw new ValidationException("Approval limit must be 0 - 999999999999.99 with max 2 decimals.");
        this.approvalLimit = limit;
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public String getPartyRole() { return "Revenue officer - " + role + " (" + department + ")"; }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (!Validatable.textOk(staffNumber, 3, 20)) throw new ValidationException("Staff number must be 3-20 characters.");
        if (!Validatable.textOk(department, 2, 60)) throw new ValidationException("Department must be 2-60 characters.");
        if (!Validatable.inList(role, "ASSESSOR", "SUPERVISOR", "MANAGER"))
            throw new ValidationException("Role must be ASSESSOR, SUPERVISOR or MANAGER.");
        if (hireDate == null || hireDate.isAfter(LocalDate.now()) || !hireDate.isAfter(getDateOfBirth()))
            throw new ValidationException("Hire date must be after birth date and not in the future.");
        if (!Validatable.moneyOk(approvalLimit)) throw new ValidationException("Approval limit must be 0 - 999999999999.99 with max 2 decimals.");
    }

    @Override
    public void printDetails() {
        System.out.println("REVENUE OFFICER");
        super.printDetails();
        System.out.println("  Staff number    : " + staffNumber);
        System.out.println("  Department      : " + department);
        System.out.println("  Role            : " + role);
        System.out.println("  Hire date       : " + hireDate);
        System.out.println("  Approval limit  : " + approvalLimit);
        System.out.println("  Active          : " + (active ? "YES" : "NO"));
    }

    @Override
    public String getSummary() {
        return "[OFFICER] Staff " + staffNumber + " | " + getDisplayName() + " | " + role
                + " | limit " + approvalLimit + " | " + (active ? "ACTIVE" : "INACTIVE");
    }
}
