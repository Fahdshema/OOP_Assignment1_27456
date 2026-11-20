package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.abstracts.TaxParty;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.OverpaymentException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.penalty.Penalty;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/*
 * TAX ADMINISTRATION MANAGEMENT SYSTEM (teaching simulation - all rates are fictional).
 * Storage = fixed arrays + counters only. One shared Scanner, nextLine() only.
 */
public class Main {

    static final Scanner sc = new Scanner(System.in);
    static final BigDecimal HUNDRED = new BigDecimal("100");
    static final BigDecimal ZERO = new BigDecimal("0.00");

    // ---------------- storage (arrays only) ----------------
    static IndividualTaxpayer[] individuals = new IndividualTaxpayer[100];
    static CorporateTaxpayer[] corporates = new CorporateTaxpayer[100];
    static RevenueOfficer[] officers = new RevenueOfficer[50];
    static TaxType[] taxTypes = new TaxType[30];
    static TaxObligation[] obligations = new TaxObligation[200];
    static TaxDeclaration[] declarations = new TaxDeclaration[200];
    static TaxAssessment[] assessments = new TaxAssessment[200];
    static TaxPayment[] payments = new TaxPayment[300];
    static Penalty[] penalties = new Penalty[100];
    static TaxRefund[] refunds = new TaxRefund[100];
    static TaxClearance[] clearances = new TaxClearance[100];
    static int nInd = 0, nCorp = 0, nOff = 0, nType = 0, nObl = 0, nDecl = 0,
            nAss = 0, nPay = 0, nPen = 0, nRef = 0, nClr = 0;

    // ---------------- form definitions: "label|java type, range, format|kind" ----------------
    static final String[] PARTY = {
        "Party ID|String, unique, 4-30 letters/digits/_-|ID 4 30",
        "First name|String, 2-60|TEXT 2 60",
        "Last name|String, 2-60|TEXT 2 60",
        "Email|String, valid email, max 120|EMAIL",
        "Phone|String, 10-15 digits, optional +|PHONE",
        "Date of birth|LocalDate, YYYY-MM-DD, past|PASTDATE"};
    static final String[] REG = {
        "TIN|String, exactly 9 digits, unique|DIGITS 9",
        "Registration date|LocalDate, YYYY-MM-DD, not future|PASTDATE",
        "Taxpayer status|String, ACTIVE/SUSPENDED|ENUM ACTIVE SUSPENDED",
        "District|String, 2-40|TEXT 2 40",
        "Sector|String, 2-40|TEXT 2 40",
        "Tax office|String, 2-60|TEXT 2 60"};
    static final String[] IND = {
        "National ID|String, exactly 16 digits, unique|DIGITS 16",
        "Occupation|String, 2-60|TEXT 2 60",
        "Business name|String, 2-80|TEXT 2 80",
        "Business start date|LocalDate, YYYY-MM-DD, past|PASTDATE",
        "Annual turnover|BigDecimal, 0-999999999999.99|MONEY",
        "Resident|boolean, YES/NO|YESNO"};
    static final String[] CORP = {
        "Company reg. no|String, 5-30 A-Z 0-9 -, unique|UPPER 5 30",
        "Legal name|String, 3-120|TEXT 3 120",
        "Incorporation date|LocalDate, YYYY-MM-DD, past|PASTDATE",
        "Business sector|String, 2-60|TEXT 2 60",
        "Authorized rep.|String, 2-60|TEXT 2 60",
        "Annual turnover|BigDecimal, 0-999999999999.99|MONEY"};
    static final String[] OFF = {
        "Staff number|String, unique, 3-20|ID 3 20",
        "Department|String, 2-60|TEXT 2 60",
        "Role|String, ASSESSOR/SUPERVISOR/MANAGER|ENUM ASSESSOR SUPERVISOR MANAGER",
        "Hire date|LocalDate, YYYY-MM-DD, not future|PASTDATE",
        "Approval limit|BigDecimal, 0-999999999999.99|MONEY",
        "Active|boolean, YES/NO|YESNO"};
    static final String[] TYPE = {
        "Tax type ID|String, unique, 3-20|ID 3 20",
        "Tax code|String, unique, 2-10 A-Z0-9|CODE 2 10",
        "Tax name|String, 3-60|TEXT 3 60",
        "Rate (%)|BigDecimal, 0-100 (fictional)|RATE",
        "Filing frequency|String, MONTHLY/QUARTERLY/ANNUAL|ENUM MONTHLY QUARTERLY ANNUAL",
        "Active|boolean, YES/NO|YESNO"};
    static final String[] OBL = {
        "Obligation ID|String, unique, 4-30|ID 4 30",
        "Taxpayer TIN|RegisteredTaxpayer, TIN of ACTIVE taxpayer|DIGITS 9",
        "Tax type code|TaxType, code of ACTIVE tax type|CODE 2 10",
        "Period start|LocalDate, YYYY-MM-DD|DATE",
        "Period end|LocalDate, YYYY-MM-DD, >= start|DATE",
        "Due date|LocalDate, YYYY-MM-DD, >= period end|DATE"};
    static final String[] DECL = {
        "Declaration ID|String, unique, 4-30|ID 4 30",
        "Obligation ID|TaxObligation, existing ID|ID 4 30",
        "Filing date|LocalDate, YYYY-MM-DD, not future|PASTDATE",
        "Taxable amount|BigDecimal, 0-999999999999.99|MONEY",
        "Declared tax|BigDecimal, = amount x rate / 100 (+-0.01)|MONEY",
        "Status|String, calculated FILED/LATE|AUTO"};
    static final String[] ASSESS = {
        "Assessment ID|String, unique, 4-30|ID 4 30",
        "Declaration ID|TaxDeclaration, existing, not rejected|ID 4 30",
        "Assessed tax|BigDecimal, 0-999999999999.99|MONEY",
        "Adjustment|BigDecimal, may be negative, total >= 0|SMONEY",
        "Assessment date|LocalDate, YYYY-MM-DD, not future|PASTDATE",
        "Status|String, calculated DRAFT|AUTO"};
    static final String[] PAY = {
        "Payment ID|String, unique, 4-30|ID 4 30",
        "Assessment ID|TaxAssessment, must be CONFIRMED|ID 4 30",
        "Payment reference|String, globally unique, 4-40|ID 4 40",
        "Amount|BigDecimal, > 0 and <= outstanding|MONEY",
        "Payment date|LocalDate, YYYY-MM-DD, not future|PASTDATE",
        "Payment method|String, CASH/BANK/MOBILE|ENUM CASH BANK MOBILE"};
    static final String[] PEN = {
        "Penalty ID|String, unique, 4-30|ID 4 30",
        "Obligation ID|TaxObligation, with a LATE declaration|ID 4 30",
        "Penalty type|String, LATE_FILING|ENUM LATE_FILING",
        "Base amount|BigDecimal, 0-999999999999.99|MONEY",
        "Penalty rate (%)|BigDecimal, 0-100 (illustrative 5)|RATE",
        "Penalty amount|BigDecimal, calculated HALF_UP 2 dp|AUTO"};
    static final String[] REF = {
        "Refund ID|String, unique, 4-30|ID 4 30",
        "Taxpayer TIN|RegisteredTaxpayer, existing TIN|DIGITS 9",
        "Assessment ID|TaxAssessment of that taxpayer|ID 4 30",
        "Requested amount|BigDecimal, > 0 and <= overpayment|MONEY",
        "Request date|LocalDate, YYYY-MM-DD, not future|PASTDATE",
        "Status|String, calculated PENDING|AUTO"};
    static final String[] CLR = {
        "Clearance ID|String, unique, 4-30|ID 4 30",
        "Taxpayer TIN|RegisteredTaxpayer, existing TIN|DIGITS 9",
        "Request date|LocalDate, YYYY-MM-DD, not future|PASTDATE",
        "Expiry date|LocalDate, YYYY-MM-DD, > request date|DATE",
        "Status|String, calculated APPROVED|AUTO",
        "Approved by (staff no.)|RevenueOfficer, ACTIVE staff number|ID 3 20"};

    static final String[][] FIELDS = {
        join(PARTY, REG, IND), join(PARTY, REG, CORP), join(PARTY, OFF),
        TYPE, OBL, DECL, ASSESS, PAY, PEN, REF, CLR};

    // end index (exclusive) of step 1, step 2 and step 3 for every form
    static final int[][] STEP_END = {
        {6, 12, 18}, {6, 12, 18}, {6, 12, 12}, {2, 6, 6}, {3, 6, 6}, {2, 5, 6},
        {2, 5, 6}, {2, 5, 6}, {2, 5, 6}, {3, 5, 6}, {2, 5, 6}};

    static final String[] TITLES = {
        "REGISTER INDIVIDUAL TAXPAYER", "REGISTER CORPORATE TAXPAYER", "REGISTER REVENUE OFFICER",
        "REGISTER TAX TYPE", "CREATE TAX OBLIGATION", "FILE TAX DECLARATION", "ASSESS DECLARATION",
        "RECORD TAX PAYMENT", "CALCULATE / RECORD PENALTY", "REQUEST REFUND", "REQUEST TAX CLEARANCE"};

    static final String[] DEFAULT_STEPS = {"IDENTIFICATION / PREREQUISITE", "ENTITY DETAILS", "VALIDATION / CALCULATIONS"};
    static final String[][] STEP_NAMES = {
        {"PERSONAL DETAILS", "TIN, REGISTRATION & OFFICE", "BUSINESS DETAILS + VALIDATION"},
        {"CONTACT PERSON DETAILS", "TIN, REGISTRATION & OFFICE", "COMPANY DETAILS + VALIDATION"},
        {"PERSONAL DETAILS", "STAFF DETAILS", "VALIDATION (no input)"},
        DEFAULT_STEPS, DEFAULT_STEPS, DEFAULT_STEPS, DEFAULT_STEPS, DEFAULT_STEPS,
        DEFAULT_STEPS, DEFAULT_STEPS, DEFAULT_STEPS};

    static String[] join(String[]... parts) {
        int total = 0;
        for (int i = 0; i < parts.length; i++) total += parts[i].length;
        String[] all = new String[total];
        int k = 0;
        for (int i = 0; i < parts.length; i++) {
            for (int j = 0; j < parts[i].length; j++) all[k++] = parts[i][j];
        }
        return all;
    }

    // =====================================================================
    //  INPUT HELPERS
    // =====================================================================
    static String readLine(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) {
            System.out.println("\nInput closed. Goodbye.");
            System.exit(0);
        }
        return sc.nextLine();
    }

    static int readMenu(String prompt) {
        String s = readLine(prompt).trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("  !! '" + s + "' is not a number. Enter one of the menu numbers.");
            return Integer.MIN_VALUE;
        }
    }

    static void printError(ValidationException e) {
        System.out.println("  !! " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    static LocalDate date(String s) { return LocalDate.parse(s); }

    static BigDecimal money(String s) { return new BigDecimal(s); }

    // checks the FORMAT of one typed value and returns the cleaned value
    static String normalize(String kind, String raw) throws ValidationException {
        String[] k = kind.split(" ");
        String t = raw.trim();
        String type = k[0];
        if (t.isEmpty()) throw new ValidationException("This field cannot be blank.");
        if (type.equals("TEXT")) {
            if (!Validatable.textOk(t, Integer.parseInt(k[1]), Integer.parseInt(k[2])))
                throw new ValidationException("Length must be " + k[1] + "-" + k[2] + " characters.");
            return t;
        }
        if (type.equals("ID")) {
            if (!t.matches("[A-Za-z0-9_-]{" + k[1] + "," + k[2] + "}"))
                throw new ValidationException("Use " + k[1] + "-" + k[2] + " letters, digits, _ or - (no spaces).");
            return t;
        }
        if (type.equals("UPPER")) {
            if (!t.matches("[A-Z0-9-]{" + k[1] + "," + k[2] + "}"))
                throw new ValidationException("Use " + k[1] + "-" + k[2] + " UPPERCASE letters/digits/hyphens.");
            return t;
        }
        if (type.equals("CODE")) {
            if (!t.matches("[A-Z0-9]{" + k[1] + "," + k[2] + "}"))
                throw new ValidationException("Use " + k[1] + "-" + k[2] + " UPPERCASE letters/digits only.");
            return t;
        }
        if (type.equals("DIGITS")) {
            if (!Validatable.digitsOk(t, Integer.parseInt(k[1])))
                throw new ValidationException("Must be exactly " + k[1] + " digits (you typed " + t.length() + " characters).");
            return t;
        }
        if (type.equals("EMAIL")) {
            if (!Validatable.emailOk(t)) throw new ValidationException("Invalid email (example: name@mail.com, max 120).");
            return t;
        }
        if (type.equals("PHONE")) {
            if (!Validatable.phoneOk(t)) throw new ValidationException("Phone must be 10-15 digits, optional leading +.");
            return t;
        }
        if (type.equals("DATE") || type.equals("PASTDATE")) {
            if (!t.matches("\\d{4}-\\d{2}-\\d{2}")) throw new ValidationException("Date must use the format YYYY-MM-DD.");
            try {
                LocalDate d = LocalDate.parse(t);
                if (type.equals("PASTDATE") && d.isAfter(LocalDate.now()))
                    throw new ValidationException("This date cannot be in the future.");
                return t;
            } catch (DateTimeParseException e) {
                throw new ValidationException("Impossible date '" + t + "' (check the month and day).");
            }
        }
        if (type.equals("MONEY") || type.equals("SMONEY")) {
            String re = type.equals("MONEY") ? "\\d{1,12}(\\.\\d{1,2})?" : "-?\\d{1,12}(\\.\\d{1,2})?";
            if (!t.matches(re))
                throw new ValidationException("Enter a number, max 12 digits and max 2 decimals"
                        + (type.equals("MONEY") ? ", not negative." : "."));
            try {
                return new BigDecimal(t).setScale(2).toPlainString();
            } catch (NumberFormatException e) {
                throw new ValidationException("Not a valid number.");
            }
        }
        if (type.equals("RATE")) {
            if (!t.matches("\\d{1,3}(\\.\\d{1,2})?")) throw new ValidationException("Rate must be a number like 18 or 5.50.");
            try {
                BigDecimal r = new BigDecimal(t);
                if (r.compareTo(HUNDRED) > 0) throw new ValidationException("Rate must be between 0 and 100.");
                return r.setScale(2).toPlainString();
            } catch (NumberFormatException e) {
                throw new ValidationException("Not a valid number.");
            }
        }
        if (type.equals("ENUM")) {
            String up = t.toUpperCase();
            for (int i = 1; i < k.length; i++) {
                if (k[i].equals(up)) return up;
            }
            throw new ValidationException("Choose one of: " + kind.substring(5).replace(" ", " / "));
        }
        if (type.equals("YESNO")) {
            String up = t.toUpperCase();
            if (up.equals("YES") || up.equals("NO")) return up;
            throw new ValidationException("Type YES or NO.");
        }
        throw new ValidationException("Unknown field kind.");
    }

    // asks one value (used by the update workflows); returns null when the user types CANCEL
    static String askKind(String label, String kind) {
        while (true) {
            String in = readLine(label + " : ");
            if (in.trim().equalsIgnoreCase("CANCEL")) return null;
            try {
                return normalize(kind, in);
            } catch (ValidationException e) {
                printError(e);
            }
        }
    }

    static String askId(String label) {
        String s = readLine(label + " (CANCEL to stop) : ").trim();
        if (s.isEmpty() || s.equalsIgnoreCase("CANCEL")) return null;
        return s;
    }

    static boolean confirm(String message) {
        String c = readLine(message + "  [1] Yes  [0] Cancel : ").trim();
        return c.equals("1");
    }

    // =====================================================================
    //  LOOKUPS (loops over occupied array entries only)
    // =====================================================================
    static boolean hasId(Identifiable[] arr, int n, String id) {
        for (int i = 0; i < n; i++) {
            if (arr[i].getId().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    static Identifiable getById(Identifiable[] arr, int n, String id) {
        for (int i = 0; i < n; i++) {
            if (arr[i].getId().equalsIgnoreCase(id)) return arr[i];
        }
        return null;
    }

    static RegisteredTaxpayer findTaxpayer(String tin) {
        for (int i = 0; i < nInd; i++) {
            if (individuals[i].getTin().equals(tin)) return individuals[i];
        }
        for (int i = 0; i < nCorp; i++) {
            if (corporates[i].getTin().equals(tin)) return corporates[i];
        }
        return null;
    }

    static RevenueOfficer findOfficer(String staff) {
        for (int i = 0; i < nOff; i++) {
            if (officers[i].getStaffNumber().equalsIgnoreCase(staff)) return officers[i];
        }
        return null;
    }

    static TaxType findTaxTypeByCode(String code) {
        for (int i = 0; i < nType; i++) {
            if (taxTypes[i].getTaxCode().equals(code)) return taxTypes[i];
        }
        return null;
    }

    static TaxObligation findObligation(String id) { return (TaxObligation) getById(obligations, nObl, id); }
    static TaxDeclaration findDeclaration(String id) { return (TaxDeclaration) getById(declarations, nDecl, id); }
    static TaxAssessment findAssessment(String id) { return (TaxAssessment) getById(assessments, nAss, id); }

    static boolean nationalIdExists(String id) {
        for (int i = 0; i < nInd; i++) {
            if (individuals[i].getNationalId().equals(id)) return true;
        }
        return false;
    }

    static boolean companyNoExists(String no) {
        for (int i = 0; i < nCorp; i++) {
            if (corporates[i].getCompanyRegistrationNo().equals(no)) return true;
        }
        return false;
    }

    static boolean referenceExists(String ref) {
        for (int i = 0; i < nPay; i++) {
            if (payments[i].getPaymentReference().equalsIgnoreCase(ref)) return true;
        }
        return false;
    }

    static RegisteredTaxpayer readTaxpayer() {
        while (true) {
            String s = readLine("Taxpayer TIN [9 digits] (CANCEL to stop) : ").trim();
            if (s.equalsIgnoreCase("CANCEL")) return null;
            RegisteredTaxpayer t = Validatable.digitsOk(s, 9) ? findTaxpayer(s) : null;
            if (t != null) return t;
            System.out.println("  !! RecordNotFoundException: no taxpayer with TIN '" + s + "'.");
        }
    }

    // =====================================================================
    //  FINANCIAL CALCULATIONS
    // =====================================================================
    static BigDecimal expectedTax(BigDecimal taxable, BigDecimal rate) {
        return taxable.multiply(rate).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    static BigDecimal sumPayments(TaxAssessment a) {
        BigDecimal sum = ZERO;
        for (int i = 0; i < nPay; i++) {
            if (payments[i].getAssessment() == a) sum = sum.add(payments[i].getAmount());
        }
        return sum;
    }

    static BigDecimal sumPenalties(TaxObligation o) {
        BigDecimal sum = ZERO;
        for (int i = 0; i < nPen; i++) {
            if (penalties[i].getObligation() == o) sum = sum.add(penalties[i].getPenaltyAmount());
        }
        return sum;
    }

    // what the taxpayer owes for this assessment (a cancelled assessment owes nothing)
    static BigDecimal dueFor(TaxAssessment a) {
        if (a.getStatus().equals("CANCELLED")) return ZERO;
        return a.getTotal().add(sumPenalties(a.getDeclaration().getObligation()));
    }

    // balance = total + penalties - confirmed payments (negative means overpaid)
    static BigDecimal balance(TaxAssessment a) { return dueFor(a).subtract(sumPayments(a)); }

    static BigDecimal overpayment(TaxAssessment a) {
        BigDecimal b = balance(a);
        return b.signum() < 0 ? b.negate() : ZERO;
    }

    static BigDecimal refundsHeld(TaxAssessment a, TaxRefund exclude) {
        BigDecimal sum = ZERO;
        for (int i = 0; i < nRef; i++) {
            TaxRefund r = refunds[i];
            if (r != exclude && r.getAssessment() == a && (r.getStatus().equals("PENDING") || r.getStatus().equals("APPROVED")))
                sum = sum.add(r.getRequestedAmount());
        }
        return sum;
    }

    static BigDecimal refundable(TaxAssessment a) { return overpayment(a).subtract(refundsHeld(a, null)); }

    static TaxDeclaration activeDeclaration(TaxObligation o) {
        for (int i = 0; i < nDecl; i++) {
            if (declarations[i].getObligation() == o && declarations[i].isActive()) return declarations[i];
        }
        return null;
    }

    static TaxAssessment confirmedAssessment(TaxDeclaration d) {
        for (int i = 0; i < nAss; i++) {
            if (assessments[i].getDeclaration() == d && assessments[i].getStatus().equals("CONFIRMED")) return assessments[i];
        }
        return null;
    }

    // reasons why a clearance cannot be approved (empty text = eligible)
    static String clearanceBlockers(RegisteredTaxpayer tp) {
        String r = "";
        LocalDate today = LocalDate.now();
        if (!tp.isActive()) r += "   - Taxpayer is not ACTIVE.\n";
        for (int i = 0; i < nObl; i++) {
            TaxObligation o = obligations[i];
            if (o.getTaxpayer() != tp) continue;
            TaxDeclaration d = activeDeclaration(o);
            if (d == null) {
                if (today.isAfter(o.getDueDate())) r += "   - Obligation " + o.getId() + " is overdue and has no declaration.\n";
                continue;
            }
            TaxAssessment a = confirmedAssessment(d);
            if (a == null) {
                r += "   - Declaration " + d.getId() + " is unresolved (no CONFIRMED assessment).\n";
                continue;
            }
            BigDecimal bal = balance(a);
            if (bal.signum() > 0 && today.isAfter(o.getDueDate()))
                r += "   - Obligation " + o.getId() + " is overdue with unpaid balance " + bal + ".\n";
        }
        return r;
    }

    // =====================================================================
    //  FIELD CHECKS, CALCULATIONS, BUILD AND SAVE  (business rules)
    // =====================================================================
    static boolean isAuto(int f, int i) { return FIELDS[f][i].endsWith("|AUTO"); }

    // checks one field against the stored records (uniqueness / existence / eligibility)
    static void checkField(int f, int i, String v) throws ValidationException {
        if (f <= 2 && i == 0 && (hasId(individuals, nInd, v) || hasId(corporates, nCorp, v) || hasId(officers, nOff, v)))
            throw new DuplicateRecordException("Party ID '" + v + "' already exists.");
        if (f <= 1 && i == 6 && findTaxpayer(v) != null)
            throw new DuplicateRecordException("TIN " + v + " is already registered to " + findTaxpayer(v).getDisplayName() + ".");
        if (f == 0 && i == 12 && nationalIdExists(v)) throw new DuplicateRecordException("National ID " + v + " already exists.");
        if (f == 1 && i == 12 && companyNoExists(v)) throw new DuplicateRecordException("Company number " + v + " already exists.");
        if (f == 2 && i == 6 && findOfficer(v) != null) throw new DuplicateRecordException("Staff number " + v + " already exists.");
        if (f == 3 && i == 0 && hasId(taxTypes, nType, v)) throw new DuplicateRecordException("Tax type ID " + v + " already exists.");
        if (f == 3 && i == 1 && findTaxTypeByCode(v) != null) throw new DuplicateRecordException("Tax code " + v + " already exists.");
        if (f == 4) {
            if (i == 0 && hasId(obligations, nObl, v)) throw new DuplicateRecordException("Obligation ID " + v + " already exists.");
            if (i == 1) {
                RegisteredTaxpayer t = findTaxpayer(v);
                if (t == null) throw new PrerequisiteMissingException("No taxpayer with TIN " + v + " is registered.", "TAXPAYER");
                if (!t.isActive()) throw new ValidationException("Taxpayer " + t.getDisplayName() + " is not ACTIVE.");
            }
            if (i == 2) {
                TaxType tt = findTaxTypeByCode(v);
                if (tt == null) throw new PrerequisiteMissingException("No tax type with code " + v + " exists.", "TAXTYPE");
                if (!tt.isActive()) throw new ValidationException("Tax type " + v + " is not ACTIVE.");
            }
        }
        if (f == 5) {
            if (i == 0 && hasId(declarations, nDecl, v)) throw new DuplicateRecordException("Declaration ID " + v + " already exists.");
            if (i == 1 && findObligation(v) == null)
                throw new PrerequisiteMissingException("Obligation " + v + " does not exist.", "OBLIGATION");
        }
        if (f == 6) {
            if (i == 0 && hasId(assessments, nAss, v)) throw new DuplicateRecordException("Assessment ID " + v + " already exists.");
            if (i == 1) {
                TaxDeclaration d = findDeclaration(v);
                if (d == null) throw new PrerequisiteMissingException("Declaration " + v + " does not exist.", "DECLARATION");
                if (d.getStatus().equals("REJECTED")) throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed.");
            }
        }
        if (f == 7) {
            if (i == 0 && hasId(payments, nPay, v)) throw new DuplicateRecordException("Payment ID " + v + " already exists.");
            if (i == 1) {
                TaxAssessment a = findAssessment(v);
                if (a == null) throw new PrerequisiteMissingException("Assessment " + v + " does not exist.", "ASSESSMENT");
                if (!a.getStatus().equals("CONFIRMED"))
                    throw new PrerequisiteMissingException("Assessment " + v + " is " + a.getStatus() + ". Payments require CONFIRMED (menu option 8).");
            }
            if (i == 2 && referenceExists(v)) throw new DuplicateRecordException("Payment reference " + v + " was already used.");
        }
        if (f == 8) {
            if (i == 0 && hasId(penalties, nPen, v)) throw new DuplicateRecordException("Penalty ID " + v + " already exists.");
            if (i == 1 && findObligation(v) == null)
                throw new PrerequisiteMissingException("Obligation " + v + " does not exist.", "OBLIGATION");
        }
        if (f == 9) {
            if (i == 0 && hasId(refunds, nRef, v)) throw new DuplicateRecordException("Refund ID " + v + " already exists.");
            if (i == 1 && findTaxpayer(v) == null)
                throw new PrerequisiteMissingException("No taxpayer with TIN " + v + " is registered.", "TAXPAYER");
            if (i == 2 && findAssessment(v) == null)
                throw new PrerequisiteMissingException("Assessment " + v + " does not exist.", "ASSESSMENT");
        }
        if (f == 10) {
            if (i == 0 && hasId(clearances, nClr, v)) throw new DuplicateRecordException("Clearance ID " + v + " already exists.");
            if (i == 1 && findTaxpayer(v) == null)
                throw new PrerequisiteMissingException("No taxpayer with TIN " + v + " is registered.", "TAXPAYER");
            if (i == 5) {
                RevenueOfficer o = findOfficer(v);
                if (o == null) throw new PrerequisiteMissingException("No officer with staff number " + v + ".", "OFFICER");
                if (!o.isActive()) throw new UnauthorizedApprovalException("Officer " + v + " is inactive and cannot approve.");
            }
        }
    }

    // fills the calculated (AUTO) fields; prints the calculation when show = true
    static void autoFill(int f, String[] v, boolean show) throws ValidationException {
        if (f == 5 && v[1] != null && v[2] != null && v[3] != null) {
            TaxObligation o = findObligation(v[1]);
            if (o == null) throw new RecordNotFoundException("Obligation not found: " + v[1]);
            v[5] = date(v[2]).isAfter(o.getDueDate()) ? "LATE" : "FILED";
            if (show) {
                BigDecimal rate = o.getTaxType().getRate();
                System.out.println("  Calculation: " + v[3] + " x " + rate + " / 100 = " + expectedTax(money(v[3]), rate) + " (expected tax)");
                System.out.println("  Due date " + o.getDueDate() + ", filed " + v[2] + " -> status " + v[5]);
            }
        }
        if (f == 6) {
            v[5] = "DRAFT";
            if (show && v[2] != null && v[3] != null)
                System.out.println("  Calculation: total = " + v[2] + " + (" + v[3] + ") = " + money(v[2]).add(money(v[3])));
        }
        if (f == 7 && show && v[1] != null && v[3] != null) {
            TaxAssessment a = findAssessment(v[1]);
            if (a != null) {
                BigDecimal bal = balance(a);
                System.out.println("  Total " + a.getTotal() + " + penalties " + sumPenalties(a.getDeclaration().getObligation())
                        + " - paid " + sumPayments(a) + " = outstanding " + bal);
                System.out.println("  After this payment: " + bal.subtract(money(v[3])));
            }
        }
        if (f == 8 && v[3] != null && v[4] != null) {
            BigDecimal amount = money(v[3]).multiply(money(v[4])).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            v[5] = amount.toPlainString();
            if (show) System.out.println("  Calculation: " + v[3] + " x " + v[4] + " / 100 = " + v[5] + " (HALF_UP, 2 decimals)");
        }
        if (f == 9) {
            v[5] = "PENDING";
            if (show && v[2] != null) {
                TaxAssessment a = findAssessment(v[2]);
                if (a != null) System.out.println("  Overpayment " + overpayment(a) + ", already requested " + refundsHeld(a, null)
                        + " -> available " + refundable(a));
            }
        }
        if (f == 10) v[4] = "APPROVED";
    }

    // creates the object from the typed values AFTER checking every rule (nothing is stored here)
    static Object build(int f, String[] v) throws ValidationException {
        for (int i = 0; i < v.length; i++) {
            if (isAuto(f, i)) continue;
            if (v[i] == null) throw new ValidationException("Field " + (i + 1) + " has no value yet.");
            checkField(f, i, v[i]);
        }
        if (f == 0) {
            IndividualTaxpayer t = new IndividualTaxpayer(v[0], v[1], v[2], v[3], v[4], date(v[5]), v[6], date(v[7]), v[8],
                    v[9], v[10], v[11], v[12], v[13], v[14], date(v[15]), money(v[16]), v[17].equals("YES"));
            t.validate();
            return t;
        }
        if (f == 1) {
            CorporateTaxpayer t = new CorporateTaxpayer(v[0], v[1], v[2], v[3], v[4], date(v[5]), v[6], date(v[7]), v[8],
                    v[9], v[10], v[11], v[12], v[13], date(v[14]), v[15], v[16], money(v[17]));
            t.validate();
            return t;
        }
        if (f == 2) {
            RevenueOfficer o = new RevenueOfficer(v[0], v[1], v[2], v[3], v[4], date(v[5]), v[6], v[7], v[8],
                    date(v[9]), money(v[10]), v[11].equals("YES"));
            o.validate();
            return o;
        }
        if (f == 3) {
            TaxType t = new TaxType(v[0], v[1], v[2], money(v[3]), v[4], v[5].equals("YES"));
            t.validate();
            return t;
        }
        if (f == 4) {
            RegisteredTaxpayer tp = findTaxpayer(v[1]);
            TaxType tt = findTaxTypeByCode(v[2]);
            TaxObligation o = new TaxObligation(v[0], tp, tt, date(v[3]), date(v[4]), date(v[5]));
            o.validate();
            for (int k = 0; k < nObl; k++) {
                TaxObligation x = obligations[k];
                if (x.getTaxpayer() == tp && x.getTaxType() == tt && !o.getPeriodStart().isAfter(x.getPeriodEnd())
                        && !o.getPeriodEnd().isBefore(x.getPeriodStart()))
                    throw new InvalidTaxPeriodException("Period overlaps obligation " + x.getId() + " (" + x.getPeriodStart()
                            + ".." + x.getPeriodEnd() + ") for the same taxpayer and tax type.");
            }
            return o;
        }
        if (f == 5) {
            TaxObligation o = findObligation(v[1]);
            if (!o.isActive()) throw new ValidationException("The taxpayer or tax type of this obligation is no longer ACTIVE.");
            LocalDate fd = date(v[2]);
            if (fd.isBefore(o.getPeriodStart())) throw new InvalidTaxPeriodException("Filing date is before the period start " + o.getPeriodStart() + ".");
            for (int k = 0; k < nDecl; k++) {
                if (declarations[k].getObligation() == o && declarations[k].isActive())
                    throw new DuplicateRecordException("Obligation " + o.getId() + " already has an active declaration (" + declarations[k].getId() + ").");
            }
            BigDecimal taxable = money(v[3]);
            BigDecimal declared = money(v[4]);
            BigDecimal expected = expectedTax(taxable, o.getTaxType().getRate());
            if (declared.subtract(expected).abs().compareTo(new BigDecimal("0.01")) > 0)
                throw new InvalidTaxCalculationException("Declared tax " + declared + " is wrong. Expected " + expected + " ("
                        + taxable + " x " + o.getTaxType().getRate() + " / 100).");
            TaxDeclaration d = new TaxDeclaration(v[0], o, fd, taxable, declared, v[5]);
            d.validate();
            return d;
        }
        if (f == 6) {
            TaxDeclaration d = findDeclaration(v[1]);
            if (d.getStatus().equals("REJECTED")) throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed.");
            for (int k = 0; k < nAss; k++) {
                if (assessments[k].getDeclaration() == d && assessments[k].isActive())
                    throw new DuplicateRecordException("Declaration " + d.getId() + " already has an active assessment (" + assessments[k].getId() + ").");
            }
            TaxAssessment a = new TaxAssessment(v[0], d, money(v[2]), new BigDecimal(v[3]), date(v[4]), v[5]);
            a.validate();
            return a;
        }
        if (f == 7) {
            TaxAssessment a = findAssessment(v[1]);
            BigDecimal amount = money(v[3]);
            if (amount.signum() <= 0) throw new ValidationException("Payment amount must be greater than zero.");
            BigDecimal bal = balance(a);
            if (amount.compareTo(bal) > 0)
                throw new OverpaymentException("Amount " + amount + " exceeds the outstanding balance " + (bal.signum() < 0 ? ZERO : bal) + ".");
            TaxPayment p = new TaxPayment(v[0], a, v[2], amount, date(v[4]), v[5]);
            p.validate();
            return p;
        }
        if (f == 8) {
            TaxObligation o = findObligation(v[1]);
            for (int k = 0; k < nPen; k++) {
                if (penalties[k].getObligation() == o && penalties[k].getPenaltyType().equals(v[2]))
                    throw new DuplicateRecordException("Obligation " + o.getId() + " already has a " + v[2] + " penalty.");
            }
            TaxDeclaration d = activeDeclaration(o);
            if (d == null || !d.isLate())
                throw new PrerequisiteMissingException("Obligation " + o.getId() + " has no late declaration, so no late-filing penalty applies.");
            BigDecimal base = money(v[3]);
            BigDecimal rate = money(v[4]);
            BigDecimal amount = base.multiply(rate).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            Penalty p = new Penalty(v[0], o, v[2], base, rate, amount);
            p.validate();
            return p;
        }
        if (f == 9) {
            RegisteredTaxpayer tp = findTaxpayer(v[1]);
            TaxAssessment a = findAssessment(v[2]);
            if (a.getDeclaration().getObligation().getTaxpayer() != tp)
                throw new ValidationException("Assessment " + a.getId() + " does not belong to taxpayer " + tp.getTin() + ".");
            for (int k = 0; k < nRef; k++) {
                if (refunds[k].getAssessment() == a && refunds[k].getStatus().equals("PENDING"))
                    throw new DuplicateRecordException("A pending refund (" + refunds[k].getId() + ") already exists for this assessment.");
            }
            if (overpayment(a).signum() <= 0) throw new OverpaymentException("No verified overpayment exists for assessment " + a.getId() + ".");
            BigDecimal requested = money(v[3]);
            if (requested.signum() <= 0 || requested.compareTo(refundable(a)) > 0)
                throw new OverpaymentException("Requested " + requested + " must be > 0 and not exceed the available overpayment " + refundable(a) + ".");
            TaxRefund r = new TaxRefund(v[0], tp, a, requested, date(v[4]), v[5]);
            r.validate();
            return r;
        }
        RegisteredTaxpayer tp = findTaxpayer(v[1]);
        RevenueOfficer officer = findOfficer(v[5]);
        LocalDate req = date(v[2]);
        LocalDate exp = date(v[3]);
        if (!exp.isAfter(req)) throw new ValidationException("Expiry date must be after the request date.");
        String reasons = clearanceBlockers(tp);
        if (!reasons.isEmpty()) throw new PrerequisiteMissingException("Clearance cannot be APPROVED:\n" + reasons.stripTrailing());
        TaxClearance c = new TaxClearance(v[0], tp, req, exp, "APPROVED", officer);
        c.validate();
        return c;
    }

    static ValidationException full(String what, int size) {
        return new ValidationException("Storage for " + what + " is full (" + size + "). Nothing was saved; existing data is unchanged.");
    }

    // re-validates everything at Save time, then stores. Nothing is changed unless every step passes.
    static Object saveRecord(int f, String[] v) throws ValidationException {
        autoFill(f, v, false);
        Object o = build(f, v);
        if (f == 0) {
            if (nInd >= individuals.length) throw full("individual taxpayers", individuals.length);
            individuals[nInd++] = (IndividualTaxpayer) o;
        } else if (f == 1) {
            if (nCorp >= corporates.length) throw full("corporate taxpayers", corporates.length);
            corporates[nCorp++] = (CorporateTaxpayer) o;
        } else if (f == 2) {
            if (nOff >= officers.length) throw full("officers", officers.length);
            officers[nOff++] = (RevenueOfficer) o;
        } else if (f == 3) {
            if (nType >= taxTypes.length) throw full("tax types", taxTypes.length);
            taxTypes[nType++] = (TaxType) o;
        } else if (f == 4) {
            if (nObl >= obligations.length) throw full("obligations", obligations.length);
            obligations[nObl++] = (TaxObligation) o;
        } else if (f == 5) {
            if (nDecl >= declarations.length) throw full("declarations", declarations.length);
            declarations[nDecl++] = (TaxDeclaration) o;
        } else if (f == 6) {
            if (nAss >= assessments.length) throw full("assessments", assessments.length);
            TaxAssessment a = (TaxAssessment) o;
            assessments[nAss++] = a;
            String st = a.getDeclaration().getStatus();
            if (st.equals("FILED") || st.equals("LATE")) a.getDeclaration().setStatus("ASSESSED");
        } else if (f == 7) {
            if (nPay >= payments.length) throw full("payments", payments.length);
            payments[nPay++] = (TaxPayment) o;
        } else if (f == 8) {
            if (nPen >= penalties.length) throw full("penalties", penalties.length);
            penalties[nPen++] = (Penalty) o;
        } else if (f == 9) {
            if (nRef >= refunds.length) throw full("refunds", refunds.length);
            refunds[nRef++] = (TaxRefund) o;
        } else {
            if (nClr >= clearances.length) throw full("clearances", clearances.length);
            clearances[nClr++] = (TaxClearance) o;
        }
        return o;
    }

    // =====================================================================
    //  FOUR-STEP SMART FORM ENGINE  (shared by all 11 forms)
    //  Returns true if the record was saved.
    // =====================================================================
    static boolean runForm(int f) {
        String[] v = new String[FIELDS[f].length];
        System.out.println("\nTip: type BACK to go to the previous step or CANCEL to discard this form.");
        int step = 1;
        while (true) {
            int r;
            if (step == 4) r = reviewStep(f, v);
            else r = entryStep(f, v, step);
            if (r == 2) return true;
            if (r == 0) {
                System.out.println("  -- Form cancelled. Nothing was saved, no balance or status was changed.");
                return false;
            }
            if (r == -1) {
                if (step > 1) step--;
                else System.out.println("  You are already at step 1.");
            } else {
                step++;
            }
        }
    }

    // result: 1 = next, -1 = back, 0 = cancel
    static int entryStep(int f, String[] v, int step) {
        int from = step == 1 ? 0 : STEP_END[f][step - 2];
        int to = STEP_END[f][step - 1];
        while (true) {
            for (int i = from; i < to; i++) {
                if (isAuto(f, i) || v[i] != null) continue;
                int r = askField(f, i, v);
                if (r != 1) return r;
            }
            boolean ok = true;
            if (step == 3) ok = runCalc(f, v);
            box(f, v, step);
            String cmd = readLine("NEXT / BACK / CANCEL / or a field number to edit : ").trim();
            if (cmd.equalsIgnoreCase("CANCEL")) return 0;
            if (cmd.equalsIgnoreCase("BACK")) return -1;
            if (cmd.equalsIgnoreCase("NEXT") || cmd.isEmpty()) {
                if (ok) return 1;
                System.out.println("  Fix the problem above first (edit a field number, BACK or CANCEL).");
                continue;
            }
            int r = editField(f, v, cmd);
            if (r == 0 || r == -1) return r;
        }
    }

    // edits one field by its number; returns 1 normally, 0 cancel, -1 back
    static int editField(int f, String[] v, String cmd) {
        int n;
        try {
            n = Integer.parseInt(cmd) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Type NEXT, BACK, CANCEL or a field number.");
            return 1;
        }
        if (n < 0 || n >= v.length) {
            System.out.println("  There is no field number " + cmd + ".");
            return 1;
        }
        if (isAuto(f, n)) {
            System.out.println("  Field " + cmd + " is calculated by the system and cannot be typed.");
            return 1;
        }
        String old = v[n];
        v[n] = null;
        int r = askField(f, n, v);
        if (r != 1) v[n] = old; // keep the previous valid value
        return r;
    }

    // asks ONE field until it is valid. 1 = ok, 0 = cancel, -1 = back
    static int askField(int f, int i, String[] v) {
        String[] spec = FIELDS[f][i].split("\\|");
        while (true) {
            String in = readLine(String.format("%02d %s [%s] : ", i + 1, spec[0], spec[1]));
            if (in.trim().equalsIgnoreCase("CANCEL")) return 0;
            if (in.trim().equalsIgnoreCase("BACK")) return -1;
            try {
                String val = normalize(spec[2], in);
                checkField(f, i, val);
                v[i] = val;
                return 1;
            } catch (PrerequisiteMissingException e) {
                printError(e);
                if (e.getPrerequisite() != null) {
                    String c = readLine("  [1] Register prerequisite now   [0] Cancel : ").trim();
                    if (c.equals("1")) registerPrerequisite(e.getPrerequisite());
                    else return 0;
                    System.out.println("  Back to your form - re-enter the field:");
                }
            } catch (ValidationException e) {
                printError(e);
            }
        }
    }

    static boolean runCalc(int f, String[] v) {
        try {
            autoFill(f, v, true);
            build(f, v);
            System.out.println("  OK: all checks passed.");
            return true;
        } catch (ValidationException e) {
            printError(e);
            return false;
        }
    }

    static int reviewStep(int f, String[] v) {
        while (true) {
            boolean ok = runCalc(f, v);
            System.out.println("\n+------------------ " + TITLES[f] + " ------------------+");
            System.out.println("| STEP 4 OF 4 - REVIEW ALL " + v.length + " FIELDS");
            for (int i = 0; i < v.length; i++) printField(f, i, v);
            String c = readLine("[1] SAVE   [2] EDIT FIELD   [3] BACK   [0] CANCEL : ").trim();
            if (c.equals("0") || c.equalsIgnoreCase("CANCEL")) return 0;
            if (c.equals("3") || c.equalsIgnoreCase("BACK")) return -1;
            if (c.equals("2")) {
                String n = readLine("Field number to edit : ").trim();
                int r = editField(f, v, n);
                if (r == 0 || r == -1) return r;
            } else if (c.equals("1")) {
                if (!ok) {
                    System.out.println("  Cannot save yet - fix the error above (use [2] EDIT FIELD).");
                    continue;
                }
                try {
                    Object saved = saveRecord(f, v);
                    System.out.println("  ** SAVED: " + ((Validatable) saved).getSummary());
                    return 2;
                } catch (ValidationException e) {
                    printError(e);
                }
            } else {
                System.out.println("  Choose 1, 2, 3 or 0.");
            }
        }
    }

    static void printField(int f, int i, String[] v) {
        String[] s = FIELDS[f][i].split("\\|");
        String val = v[i] != null ? v[i] : (isAuto(f, i) ? "(calculated)" : "-");
        System.out.printf("| %02d %-24s [%s] : %s%n", i + 1, s[0], s[1], val);
    }

    static void box(int f, String[] v, int step) {
        int from = step == 1 ? 0 : STEP_END[f][step - 2];
        int to = STEP_END[f][step - 1];
        System.out.println("\n+------------------ " + TITLES[f] + " ------------------+");
        System.out.println("| STEP " + step + " OF 4 - " + STEP_NAMES[f][step - 1]);
        for (int i = from; i < to; i++) printField(f, i, v);
        if (from == to) System.out.println("| (no typing in this step - the system checks your data)");
    }

    static void registerPrerequisite(String kind) {
        if (kind.equals("TAXPAYER")) {
            String c = readLine("  [1] Individual  [2] Corporate  [0] Cancel : ").trim();
            if (c.equals("1")) runForm(0);
            else if (c.equals("2")) runForm(1);
        } else if (kind.equals("TAXTYPE")) runForm(3);
        else if (kind.equals("OBLIGATION")) runForm(4);
        else if (kind.equals("DECLARATION")) runForm(5);
        else if (kind.equals("OFFICER")) runForm(2);
        else if (kind.equals("ASSESSMENT")) {
            if (runForm(6)) {
                try {
                    confirmAssessment();
                } catch (ValidationException e) {
                    printError(e);
                }
            }
        }
    }

    // =====================================================================
    //  MENU WORKFLOWS
    // =====================================================================
    // option 8
    static void confirmAssessment() throws ValidationException {
        String id = askId("Assessment ID to confirm");
        if (id == null) return;
        TaxAssessment a = findAssessment(id);
        if (a == null) throw new RecordNotFoundException("Assessment " + id + " does not exist.");
        a.printDetails();
        if (a.getDeclaration().getStatus().equals("REJECTED"))
            throw new InvalidStatusTransitionException("The declaration was rejected; this assessment cannot be confirmed.");
        if (!a.getStatus().equals("DRAFT"))
            throw new InvalidStatusTransitionException("Only a DRAFT assessment can be confirmed (this one is " + a.getStatus() + ").");
        if (!confirm("Confirm this assessment?")) {
            System.out.println("  -- Cancelled. Status unchanged (" + a.getStatus() + ").");
            return;
        }
        a.setStatus("CONFIRMED");
        System.out.println("  ** CONFIRMED: " + a.getSummary());
    }

    static void printList(String title, Validatable[] arr, int n, boolean details) {
        System.out.println("\n--- " + title + " (" + n + ") ---");
        if (n == 0) System.out.println("  (no records)");
        for (int i = 0; i < n; i++) {
            if (details) {
                arr[i].printDetails();
                System.out.println();
            } else {
                System.out.println("  " + arr[i].getSummary());
            }
        }
    }

    // option 9
    static void viewAll() {
        System.out.println("\n1 Individuals  2 Corporates  3 Officers  4 Tax types  5 Obligations  6 Declarations");
        System.out.println("7 Assessments  8 Payments  9 Penalties  10 Refunds  11 Clearances  12 ALL (summary)  0 Back");
        int c = readMenu("View which records? : ");
        if (c == 1 || c == 12) printList("INDIVIDUAL TAXPAYERS", individuals, nInd, c == 1);
        if (c == 2 || c == 12) printList("CORPORATE TAXPAYERS", corporates, nCorp, c == 2);
        if (c == 3 || c == 12) printList("REVENUE OFFICERS", officers, nOff, c == 3);
        if (c == 4 || c == 12) printList("TAX TYPES", taxTypes, nType, c == 4);
        if (c == 5 || c == 12) printList("TAX OBLIGATIONS", obligations, nObl, c == 5);
        if (c == 6 || c == 12) printList("TAX DECLARATIONS", declarations, nDecl, c == 6);
        if (c == 7 || c == 12) printList("TAX ASSESSMENTS", assessments, nAss, c == 7);
        if (c == 8 || c == 12) printList("TAX PAYMENTS", payments, nPay, c == 8);
        if (c == 9 || c == 12) printList("PENALTIES", penalties, nPen, c == 9);
        if (c == 10 || c == 12) printList("TAX REFUNDS", refunds, nRef, c == 10);
        if (c == 11 || c == 12) printList("TAX CLEARANCES", clearances, nClr, c == 11);
        if (c < 0 || c > 12) System.out.println("  Unknown option.");
    }

    // option 10
    static void declarationHistory() {
        RegisteredTaxpayer tp = readTaxpayer();
        if (tp == null) return;
        System.out.println("\nDECLARATION HISTORY for " + tp.getSummary());
        int count = 0;
        for (int i = 0; i < nDecl; i++) {
            TaxDeclaration d = declarations[i];
            if (d.getObligation().getTaxpayer() == tp) {
                System.out.println("  " + d.getSummary() + (d.isLate() ? "  [LATE]" : ""));
                count++;
            }
        }
        if (count == 0) System.out.println("  (no declarations)");
    }

    // option 11
    static void assessmentHistory() {
        RegisteredTaxpayer tp = readTaxpayer();
        if (tp == null) return;
        System.out.println("\nASSESSMENT HISTORY for " + tp.getSummary());
        int count = 0;
        for (int i = 0; i < nAss; i++) {
            TaxAssessment a = assessments[i];
            if (a.getDeclaration().getObligation().getTaxpayer() == tp) {
                System.out.println("  " + a.getSummary() + " | balance " + balance(a));
                count++;
            }
        }
        if (count == 0) System.out.println("  (no assessments)");
    }

    // option 14
    static void refundMenu() throws ValidationException {
        System.out.println("\n1 Request a refund   2 Approve / reject a pending refund   0 Back");
        int c = readMenu("Choose : ");
        if (c == 1) runForm(9);
        else if (c == 2) decideRefund();
        else if (c != 0) System.out.println("  Unknown option.");
    }

    static void decideRefund() throws ValidationException {
        String id = askId("Refund ID to decide");
        if (id == null) return;
        TaxRefund r = (TaxRefund) getById(refunds, nRef, id);
        if (r == null) throw new RecordNotFoundException("Refund " + id + " does not exist.");
        r.printDetails();
        if (!r.getStatus().equals("PENDING"))
            throw new InvalidStatusTransitionException("Refund is already " + r.getStatus() + ".");
        String staff = askId("Deciding officer staff number");
        if (staff == null) return;
        RevenueOfficer off = findOfficer(staff);
        if (off == null) throw new RecordNotFoundException("Officer " + staff + " does not exist.");
        if (!off.isActive()) throw new UnauthorizedApprovalException("Officer " + staff + " is inactive and cannot decide refunds.");
        String c = readLine("[1] APPROVE   [2] REJECT   [0] CANCEL : ").trim();
        if (c.equals("1")) {
            if (r.getRequestedAmount().compareTo(off.getApprovalLimit()) > 0)
                throw new UnauthorizedApprovalException("Refund " + r.getRequestedAmount() + " is above the officer approval limit " + off.getApprovalLimit() + ".");
            BigDecimal supported = overpayment(r.getAssessment()).subtract(refundsHeld(r.getAssessment(), r));
            if (supported.compareTo(r.getRequestedAmount()) < 0)
                throw new OverpaymentException("The supporting overpayment (" + supported + ") no longer covers this refund.");
            r.decide("APPROVED", off);
            System.out.println("  ** APPROVED: " + r.getSummary());
        } else if (c.equals("2")) {
            r.decide("REJECTED", off);
            System.out.println("  ** REJECTED: " + r.getSummary());
        } else {
            System.out.println("  -- Cancelled. Refund status unchanged (" + r.getStatus() + ").");
        }
    }

    // option 16
    static void search() {
        String key = readLine("Enter ID / TIN / national ID / company no. / staff no. / tax code / payment ref : ").trim();
        if (key.isEmpty()) {
            System.out.println("  Nothing typed.");
            return;
        }
        int found = searchIn(individuals, nInd, key) + searchIn(corporates, nCorp, key) + searchIn(officers, nOff, key)
                + searchIn(taxTypes, nType, key) + searchIn(obligations, nObl, key) + searchIn(declarations, nDecl, key)
                + searchIn(assessments, nAss, key) + searchIn(payments, nPay, key) + searchIn(penalties, nPen, key)
                + searchIn(refunds, nRef, key) + searchIn(clearances, nClr, key);
        if (found == 0) System.out.println("  !! RecordNotFoundException: nothing matches '" + key + "'.");
    }

    static int searchIn(Identifiable[] arr, int n, String key) {
        int count = 0;
        for (int i = 0; i < n; i++) {
            Identifiable x = arr[i];
            boolean match = x.getId().equalsIgnoreCase(key);
            if (x instanceof RegisteredTaxpayer && ((RegisteredTaxpayer) x).getTin().equals(key)) match = true;
            if (x instanceof IndividualTaxpayer && ((IndividualTaxpayer) x).getNationalId().equals(key)) match = true;
            if (x instanceof CorporateTaxpayer && ((CorporateTaxpayer) x).getCompanyRegistrationNo().equalsIgnoreCase(key)) match = true;
            if (x instanceof RevenueOfficer && ((RevenueOfficer) x).getStaffNumber().equalsIgnoreCase(key)) match = true;
            if (x instanceof TaxType && ((TaxType) x).getTaxCode().equalsIgnoreCase(key)) match = true;
            if (x instanceof TaxPayment && ((TaxPayment) x).getPaymentReference().equalsIgnoreCase(key)) match = true;
            if (match) {
                System.out.println();
                ((Validatable) x).printDetails();
                count++;
            }
        }
        return count;
    }

    // option 17
    static void updateRecord() throws ValidationException {
        System.out.println("\nUPDATE: 1 Taxpayer (email/phone/status)  2 Officer (active/limit)  3 Tax type (rate/active)");
        System.out.println("        4 Reject a declaration  5 Assessment (adjustment/cancel)  0 Back");
        int c = readMenu("Choose : ");
        if (c == 1) {
            RegisteredTaxpayer tp = readTaxpayer();
            if (tp == null) return;
            tp.printDetails();
            int w = readMenu("1 Email  2 Phone  3 Status : ");
            if (w == 1) {
                String val = askKind("New email [valid email]", "EMAIL");
                if (val != null && confirm("Apply?")) { tp.setEmail(val); System.out.println("  ** UPDATED."); }
            } else if (w == 2) {
                String val = askKind("New phone [10-15 digits]", "PHONE");
                if (val != null && confirm("Apply?")) { tp.setPhoneNumber(val); System.out.println("  ** UPDATED."); }
            } else if (w == 3) {
                String val = askKind("New status [ACTIVE/SUSPENDED/CLOSED]", "ENUM ACTIVE SUSPENDED CLOSED");
                if (val != null && confirm("Change status to " + val + "?")) { tp.setTaxpayerStatus(val); System.out.println("  ** UPDATED: " + tp.getSummary()); }
            }
        } else if (c == 2) {
            String id = askId("Officer staff number");
            if (id == null) return;
            RevenueOfficer o = findOfficer(id);
            if (o == null) throw new RecordNotFoundException("Officer " + id + " does not exist.");
            o.printDetails();
            int w = readMenu("1 Toggle active  2 New approval limit : ");
            if (w == 1 && confirm("Set active = " + !o.isActive() + "?")) { o.setActive(!o.isActive()); System.out.println("  ** UPDATED: " + o.getSummary()); }
            else if (w == 2) {
                String val = askKind("New approval limit [BigDecimal]", "MONEY");
                if (val != null && confirm("Apply?")) { o.setApprovalLimit(money(val)); System.out.println("  ** UPDATED: " + o.getSummary()); }
            }
        } else if (c == 3) {
            String code = askId("Tax code");
            if (code == null) return;
            TaxType t = findTaxTypeByCode(code);
            if (t == null) throw new RecordNotFoundException("Tax code " + code + " does not exist.");
            t.printDetails();
            int w = readMenu("1 New rate  2 Toggle active : ");
            if (w == 1) {
                String val = askKind("New rate % [0-100]", "RATE");
                if (val != null && confirm("Apply?")) { t.setRate(money(val)); System.out.println("  ** UPDATED: " + t.getSummary()); }
            } else if (w == 2 && confirm("Set active = " + !t.isActive() + "?")) {
                t.setActive(!t.isActive());
                System.out.println("  ** UPDATED: " + t.getSummary());
            }
        } else if (c == 4) {
            String id = askId("Declaration ID to reject");
            if (id == null) return;
            TaxDeclaration d = findDeclaration(id);
            if (d == null) throw new RecordNotFoundException("Declaration " + id + " does not exist.");
            d.printDetails();
            if (confirm("Reject this declaration?")) { d.setStatus("REJECTED"); System.out.println("  ** REJECTED: " + d.getSummary()); }
        } else if (c == 5) {
            String id = askId("Assessment ID");
            if (id == null) return;
            TaxAssessment a = findAssessment(id);
            if (a == null) throw new RecordNotFoundException("Assessment " + id + " does not exist.");
            a.printDetails();
            int w = readMenu("1 New adjustment  2 Cancel assessment : ");
            if (w == 1) {
                String val = askKind("New adjustment [BigDecimal, may be negative]", "SMONEY");
                if (val != null && confirm("Apply?")) {
                    a.setAdjustmentAmount(new BigDecimal(val));
                    System.out.println("  ** UPDATED: " + a.getSummary() + " | balance now " + balance(a));
                }
            } else if (w == 2 && confirm("Cancel this assessment?")) {
                a.setStatus("CANCELLED");
                System.out.println("  ** CANCELLED: " + a.getSummary() + " | overpayment now " + overpayment(a));
            }
        } else if (c != 0) {
            System.out.println("  Unknown option.");
        }
    }

    // option 18
    static void statement() {
        RegisteredTaxpayer tp = readTaxpayer();
        if (tp == null) return;
        System.out.println("\n================ TAXPAYER STATEMENT ================");
        tp.printDetails();
        BigDecimal due = ZERO, paid = ZERO;
        System.out.println("\nOBLIGATIONS / DECLARATIONS");
        for (int i = 0; i < nObl; i++) {
            if (obligations[i].getTaxpayer() != tp) continue;
            System.out.println("  " + obligations[i].getSummary());
            TaxDeclaration d = activeDeclaration(obligations[i]);
            System.out.println("      " + (d == null ? "No active declaration" : d.getSummary() + (d.isLate() ? " [LATE]" : "")));
        }
        System.out.println("\nASSESSMENTS");
        for (int i = 0; i < nAss; i++) {
            TaxAssessment a = assessments[i];
            if (a.getDeclaration().getObligation().getTaxpayer() != tp) continue;
            BigDecimal pen = a.getStatus().equals("CANCELLED") ? ZERO : sumPenalties(a.getDeclaration().getObligation());
            System.out.println("  " + a.getSummary());
            System.out.println("      penalties " + pen + " | paid " + sumPayments(a) + " | balance " + balance(a));
            due = due.add(dueFor(a));
            paid = paid.add(sumPayments(a));
        }
        System.out.println("\nPAYMENTS");
        for (int i = 0; i < nPay; i++) {
            if (payments[i].getAssessment().getDeclaration().getObligation().getTaxpayer() == tp) System.out.println("  " + payments[i].getSummary());
        }
        System.out.println("\nPENALTIES");
        for (int i = 0; i < nPen; i++) {
            if (penalties[i].getObligation().getTaxpayer() == tp) System.out.println("  " + penalties[i].getSummary());
        }
        System.out.println("\nREFUNDS");
        for (int i = 0; i < nRef; i++) {
            if (refunds[i].getTaxpayer() == tp) System.out.println("  " + refunds[i].getSummary());
        }
        System.out.println("\nCLEARANCES");
        for (int i = 0; i < nClr; i++) {
            if (clearances[i].getTaxpayer() == tp) System.out.println("  " + clearances[i].getSummary());
        }
        System.out.println("\nTOTAL DUE " + due + " | TOTAL PAID " + paid + " | NET BALANCE " + due.subtract(paid));
    }

    // option 19
    static void outstanding() {
        System.out.println("\nOUTSTANDING BALANCES (CONFIRMED assessments)");
        BigDecimal total = ZERO;
        int count = 0;
        for (int i = 0; i < nAss; i++) {
            TaxAssessment a = assessments[i];
            if (!a.getStatus().equals("CONFIRMED")) continue;
            BigDecimal bal = balance(a);
            if (bal.signum() > 0) {
                RegisteredTaxpayer tp = a.getDeclaration().getObligation().getTaxpayer();
                System.out.println("  " + a.getId() + " | TIN " + tp.getTin() + " " + tp.getDisplayName() + " | total " + a.getTotal()
                        + " | penalties " + sumPenalties(a.getDeclaration().getObligation()) + " | paid " + sumPayments(a) + " | OUTSTANDING " + bal);
                total = total.add(bal);
                count++;
            }
        }
        if (count == 0) System.out.println("  (nothing outstanding)");
        System.out.println("  TOTAL OUTSTANDING: " + total);
    }

    // option 20
    static void reports() {
        System.out.println("\n================ TAX REPORTS (fictional data) ================");
        System.out.println("Taxpayers : " + (nInd + nCorp) + " (individual " + nInd + ", corporate " + nCorp + ") | Officers " + nOff
                + " | Tax types " + nType + " | Obligations " + nObl);
        int late = 0, rejected = 0;
        for (int i = 0; i < nDecl; i++) {
            if (declarations[i].isLate()) late++;
            if (declarations[i].getStatus().equals("REJECTED")) rejected++;
        }
        System.out.println("Declarations : " + nDecl + " (late " + late + ", rejected " + rejected + ")");
        int draft = 0, conf = 0, canc = 0;
        BigDecimal confirmedTotal = ZERO, outstandingTotal = ZERO;
        for (int i = 0; i < nAss; i++) {
            TaxAssessment a = assessments[i];
            if (a.getStatus().equals("DRAFT")) draft++;
            if (a.getStatus().equals("CANCELLED")) canc++;
            if (a.getStatus().equals("CONFIRMED")) {
                conf++;
                confirmedTotal = confirmedTotal.add(a.getTotal());
                if (balance(a).signum() > 0) outstandingTotal = outstandingTotal.add(balance(a));
            }
        }
        System.out.println("Assessments : DRAFT " + draft + ", CONFIRMED " + conf + ", CANCELLED " + canc + " | confirmed total " + confirmedTotal);
        BigDecimal collected = ZERO, penTotal = ZERO;
        for (int i = 0; i < nPay; i++) collected = collected.add(payments[i].getAmount());
        for (int i = 0; i < nPen; i++) penTotal = penTotal.add(penalties[i].getPenaltyAmount());
        System.out.println("Payments : " + nPay + " | collected " + collected + " | penalties " + penTotal + " | outstanding " + outstandingTotal);
        BigDecimal pend = ZERO, appr = ZERO;
        for (int i = 0; i < nRef; i++) {
            if (refunds[i].getStatus().equals("PENDING")) pend = pend.add(refunds[i].getRequestedAmount());
            if (refunds[i].getStatus().equals("APPROVED")) appr = appr.add(refunds[i].getRequestedAmount());
        }
        System.out.println("Refunds : pending " + pend + " | approved " + appr);
        int ok = 0;
        for (int i = 0; i < nClr; i++) {
            if (clearances[i].isActive()) ok++;
        }
        System.out.println("Clearances : " + nClr + " (currently valid " + ok + ")");
        System.out.println("\nCollections by tax type:");
        for (int t = 0; t < nType; t++) {
            BigDecimal sum = ZERO;
            for (int p = 0; p < nPay; p++) {
                if (payments[p].getAssessment().getDeclaration().getObligation().getTaxType() == taxTypes[t])
                    sum = sum.add(payments[p].getAmount());
            }
            System.out.println("  " + taxTypes[t].getDisplayName() + " : " + sum);
        }
        if (nType == 0) System.out.println("  (no tax types)");
    }

    // option 21
    static void polymorphism() {
        System.out.println("\n=== POLYMORPHISM 1: TaxParty[] (different concrete types, same calls) ===");
        TaxParty[] parties = new TaxParty[3];
        boolean[] sample = new boolean[3];
        if (nInd > 0) parties[0] = individuals[0];
        else {
            parties[0] = new IndividualTaxpayer("SAMPLE-IND", "Demo", "Person", "demo@mail.com", "0788000001", LocalDate.of(1990, 1, 1),
                    "000000001", LocalDate.of(2020, 1, 1), "ACTIVE", "Gasabo", "Remera", "Kigali", "1199000000000001", "Trader", "Demo Shop",
                    LocalDate.of(2018, 1, 1), new BigDecimal("1000.00"), true);
            sample[0] = true;
        }
        if (nCorp > 0) parties[1] = corporates[0];
        else {
            parties[1] = new CorporateTaxpayer("SAMPLE-COR", "Demo", "Contact", "corp@mail.com", "0788000002", LocalDate.of(1985, 5, 5),
                    "000000002", LocalDate.of(2020, 1, 1), "ACTIVE", "Nyarugenge", "Nyamirambo", "Kigali", "SAMPLE-CO-1", "Demo Company Ltd",
                    LocalDate.of(2015, 1, 1), "Retail", "Demo Rep", new BigDecimal("5000000.00"));
            sample[1] = true;
        }
        if (nOff > 0) parties[2] = officers[0];
        else {
            parties[2] = new RevenueOfficer("SAMPLE-OFF", "Demo", "Officer", "off@mail.com", "0788000003", LocalDate.of(1988, 3, 3),
                    "STAFF-0", "Assessment", "MANAGER", LocalDate.of(2015, 1, 1), new BigDecimal("1000000.00"), true);
            sample[2] = true;
        }
        for (int i = 0; i < parties.length; i++) {
            TaxParty p = parties[i];
            System.out.println("\n[" + (i + 1) + "] runtime class : " + p.getClass().getSimpleName() + (sample[i] ? "  (sample object, not saved)" : ""));
            System.out.println("    getDisplayName() : " + p.getDisplayName());
            System.out.println("    getPartyRole()   : " + p.getPartyRole());
            System.out.println("    isActive()       : " + p.isActive());
            System.out.println("    getSummary()     : " + p.getSummary());
            if (p instanceof RegisteredTaxpayer) System.out.println("    getAnnualTurnover() : " + ((RegisteredTaxpayer) p).getAnnualTurnover());
            if (p instanceof RevenueOfficer) System.out.println("    approval limit   : " + ((RevenueOfficer) p).getApprovalLimit());
        }
        System.out.println("\n=== POLYMORPHISM 2: Identifiable[] (parties + business records) ===");
        Identifiable[] items = new Identifiable[11];
        int n = 0;
        items[n++] = parties[0];
        items[n++] = parties[1];
        items[n++] = parties[2];
        if (nType > 0) items[n++] = taxTypes[0];
        if (nObl > 0) items[n++] = obligations[0];
        if (nDecl > 0) items[n++] = declarations[0];
        if (nAss > 0) items[n++] = assessments[0];
        if (nPay > 0) items[n++] = payments[0];
        if (nPen > 0) items[n++] = penalties[0];
        if (nRef > 0) items[n++] = refunds[0];
        if (nClr > 0) items[n++] = clearances[0];
        for (int i = 0; i < n; i++) {
            System.out.println("  " + items[i].getClass().getSimpleName() + " -> id=" + items[i].getId() + " | name=" + items[i].getDisplayName()
                    + " | active=" + items[i].isActive());
            System.out.println("      " + ((Validatable) items[i]).getSummary());
        }
    }

    // =====================================================================
    //  MENU + MAIN
    // =====================================================================
    static final String[] MENU = {
        "Register Individual Taxpayer", "Register Corporate Taxpayer", "Register Revenue Officer", "Register Tax Type",
        "Create Tax Obligation", "File Tax Declaration", "Assess Declaration", "Confirm Assessment", "View All Records",
        "View Declaration History", "View Assessment History", "Record Tax Payment", "Calculate / Record Penalty",
        "Request / Approve Refund", "Request / Issue Tax Clearance", "Search Record by ID / TIN", "Update Eligible Record",
        "View Taxpayer Statement", "View Outstanding Balances", "View Tax Reports", "Demonstrate Polymorphism"};

    static void printMenu() {
        System.out.println("\n================ TAX ADMINISTRATION MANAGEMENT ================");
        for (int i = 0; i < 11; i++) {
            String right = (i + 11 < MENU.length) ? String.format("%2d %s", i + 12, MENU[i + 11]) : "";
            System.out.printf("%2d %-32s %s%n", i + 1, MENU[i], right);
        }
        System.out.println(" 0 Exit");
    }

    static void runOption(int c) throws ValidationException {
        switch (c) {
            case 1: runForm(0); break;
            case 2: runForm(1); break;
            case 3: runForm(2); break;
            case 4: runForm(3); break;
            case 5: runForm(4); break;
            case 6: runForm(5); break;
            case 7: runForm(6); break;
            case 8: confirmAssessment(); break;
            case 9: viewAll(); break;
            case 10: declarationHistory(); break;
            case 11: assessmentHistory(); break;
            case 12: runForm(7); break;
            case 13: runForm(8); break;
            case 14: refundMenu(); break;
            case 15: runForm(10); break;
            case 16: search(); break;
            case 17: updateRecord(); break;
            case 18: statement(); break;
            case 19: outstanding(); break;
            case 20: reports(); break;
            case 21: polymorphism(); break;
            default:
                if (c != Integer.MIN_VALUE) System.out.println("  !! Unknown menu option " + c + ". Choose 0-21.");
        }
    }

    public static void main(String[] args) {
        System.out.println("TAMS - teaching simulation. All rates and rules are FICTIONAL, not official tax law.");
        while (true) {
            printMenu();
            int c = readMenu("Select option: ");
            if (c == 0) {
                System.out.println("Goodbye.");
                break;
            }
            try {
                runOption(c);
            } catch (ValidationException e) {
                printError(e);
            } catch (RuntimeException e) {
                System.out.println("  !! Unexpected problem (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            }
        }
    }
}
