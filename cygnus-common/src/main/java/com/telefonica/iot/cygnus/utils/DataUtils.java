/**
 * 
 */
package com.telefonica.iot.cygnus.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author PMO Santander Smart City - Ayuntamiento de Santander
 *
 */
public final class DataUtils {

    private static final String[] PATTERNS = {
        "^[0-9]{4}(-)[0-9]{2}(-)[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.{0,1}[0-9]{3}Z$",
        "^[0-9]{4}(\\/)[0-9]{2}(\\/)[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.{0,1}[0-9]{3}Z$", "^(-)?[0-9]+$",
        "^(-)?[0-9]+(\\.)?([0-9])+$", "^(-)?[0-9]+((\\,)([0-9])+)?$"};
    private static final String[] FORMAT_DATE = {"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy/MM/dd'T'HH:mm:ss.SSS'Z'"};

    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private DataUtils() {
    } // DataUtils
    
    /**
     * Transform String to Date, Integer, Double.
     * 
     * @param value
     * @return
     */
    public static Object getStringToObject(String value) {
        int i = 0;

        for (String pattern : PATTERNS) {
            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(value);

            if (m.find()) {
                switch (i) {
                    case 0:
                        SimpleDateFormat dtformatter = new SimpleDateFormat(FORMAT_DATE[i]);
    
                        try {
                            Date dateD = dtformatter.parse(value);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dateD);
                            return calendar;
                            //return dateD.getTime();
                        } catch (ParseException e1) {
                            return value;
                        }
    
                    case 1:
                        try {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new SimpleDateFormat(FORMAT_DATE[i]).parse(value));
                            return calendar;
                        } catch (ParseException e) {
                            return value;
                        }
                    case 2:
                        return new Integer(value);
                    case 3:
                        return new Double(value);
                    case 4:
                        return new Double(value.replace(",", "."));
    
                    default:
                        return value;
                }

            }
            i++;

        }
        return value;

    }
}
