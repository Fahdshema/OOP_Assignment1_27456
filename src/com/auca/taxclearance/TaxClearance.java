package com.auca.taxclearance;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import java.time.LocalDate;

public class TaxClearance implements Identifiable, Validatable {
    private String clearanceId;
    private RegisteredTaxpayer taxpayer;
    private LocalDate requestDate;
    private LocalDate expiryDate;
    private String status; // APPROVED, DENIED
    private RevenueOfficer approvedBy;

    public TaxClearance(String clearanceId, RegisteredTaxpayer taxpayer, LocalDate requestDate,
                        LocalDate expiryDate, String status, RevenueOfficer approvedBy) {
        this.clearanceId = clearanceId;
        this.taxpayer = taxpayer;
        this.requestDate = requestDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public String getClearanceId() { return clearanceId; }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public RevenueOfficer getApprovedBy() { return approvedBy; }

    @Override
    public String getId() { return clearanceId; }

    @Override
    public String getDisplayName() { return "Clearance " + clearanceId; }

    @Override
    public boolean isActive() { return "APPROVED".equals(status) && !expiryDate.isBefore(LocalDate.now()); }

    @Override
    public void validate() throws ValidationException {
        if (!Validatable.textOk(clearanceId, 4, 30)) throw new ValidationException("Clearance ID must be 4-30 characters.");
        if (taxpayer == null || !taxpayer.isActive()) throw new ValidationException("Clearance needs an ACTIVE taxpayer.");
        if (requestDate == null || requestDate.isAfter(LocalDate.now())) throw new ValidationException("Request date cannot be empty or in the future.");
        if (expiryDate == null || !expiryDate.isAfter(requestDate)) throw new ValidationException("Expiry date must be after the request date.");
        if (!Validatable.inList(status, "APPROVED", "DENIED")) throw new ValidationException("Clearance status must be APPROVED or DENIED.");
        if (approvedBy == null) throw new ValidationException("Clearance must store the approving officer.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX CLEARANCE");
        System.out.println("  Clearance ID    : " + clearanceId);
        System.out.println("  Taxpayer        : " + taxpayer.getTin() + " " + taxpayer.getDisplayName());
        System.out.println("  Request date    : " + requestDate);
        System.out.println("  Expiry date     : " + expiryDate);
        System.out.println("  Status          : " + status);
        System.out.println("  Approved by     : " + approvedBy.getStaffNumber() + " " + approvedBy.getDisplayName());
    }

    @Override
    public String getSummary() {
        return "[CLEARANCE] " + clearanceId + " | TIN " + taxpayer.getTin() + " | " + status + " | expires " + expiryDate;
    }
}
