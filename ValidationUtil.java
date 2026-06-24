package util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class ValidationUtil {

    // Sri Lankan style NIC: old format 9 digits + V/X, or new format 12 digits
    private static final Pattern NIC_PATTERN =
            Pattern.compile("^([0-9]{9}[vVxX]|[0-9]{12})$");

    public static boolean isValidNic(String nic) {
        if (nic == null) return false;
        return NIC_PATTERN.matcher(nic.trim()).matches();
    }

    public static int calculateAge(LocalDate dob) {
        if (dob == null) return -1;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    public static boolean isEligibleAge(int age) {
        return age >= 18;
    }

    /**
     * Safely parses an integer, throwing NumberFormatException upward
     * so the calling UI can catch it and show a friendly dialog.
     */
    public static int parseIntSafe(String text) throws NumberFormatException {
        return Integer.parseInt(text.trim());
    }
}
