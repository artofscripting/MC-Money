package com.artof.minecraftmoney.util;

/**
 * Utility class for formatting currency values with abbreviations.
 */
public class CurrencyFormatter {
    
    private static final long TRILLION = 1_000_000_000_000L;
    private static final long BILLION = 1_000_000_000L;
    private static final long MILLION = 1_000_000L;
    private static final long THOUSAND = 1_000L;
    
    /**
     * Formats a currency value with abbreviations (K, M, B, T).
     * Values under 1000 are shown as-is.
     * Values 1000+ are abbreviated to 1 decimal place.
     * 
     * @param value The currency value to format
     * @return Formatted string with appropriate suffix
     */
    public static String format(long value) {
        if (value < 0) {
            return "-" + format(-value);
        }
        
        if (value >= TRILLION) {
            return formatWithSuffix(value, TRILLION, "T");
        } else if (value >= BILLION) {
            return formatWithSuffix(value, BILLION, "B");
        } else if (value >= MILLION) {
            return formatWithSuffix(value, MILLION, "M");
        } else if (value >= THOUSAND) {
            return formatWithSuffix(value, THOUSAND, "K");
        } else {
            return String.valueOf(value);
        }
    }
    
    /**
     * Formats a currency value with abbreviations (K, M, B, T).
     * Convenience method for int values.
     * 
     * @param value The currency value to format
     * @return Formatted string with appropriate suffix
     */
    public static String format(int value) {
        return format((long) value);
    }
    
    private static String formatWithSuffix(long value, long divisor, String suffix) {
        double divided = (double) value / divisor;
        
        // If it's a clean number (like 1.0, 2.0), don't show decimal
        if (divided == Math.floor(divided) && divided < 1000) {
            return (int) divided + suffix;
        }
        
        // Show one decimal place, but trim .0
        String formatted = String.format("%.1f", divided);
        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }
        
        return formatted + suffix;
    }
}
