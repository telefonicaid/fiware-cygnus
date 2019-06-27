/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
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

    /**
     * It is an array of regular expressions where they are executed until one
     * is correct. The first two expressions are to know if the string is a date
     * (yyyy-MM-dd'T'HH:mm:ss.SSS'Z', yyyy/MM/dd'T'HH:mm:ss.SSS'Z'). The third
     * to know if it is an Integer,and the last two to know if it is a double.
     */
    private static final String[] PATTERNS = {
            // they're regular expressions to check that the string is a Date.
            "^[0-9]{4}(-)[0-9]{2}(-)[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.{0,1}[0-9]{3}Z$",
            "^[0-9]{4}(\\/)[0-9]{2}(\\/)[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.{0,1}[0-9]{3}Z$",
            // It's a regular expression to check that the string is a Integer.
            "^(-)?[0-9]+$",
            // they're regular expressions to check that the string is a Double.
            "^(-)?[0-9]+(\\.)?([0-9])+$", "^(-)?[0-9]+((\\,)([0-9])+)?$" };
    private static final String[] FORMAT_DATE = { "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy/MM/dd'T'HH:mm:ss.SSS'Z'" };

    /**
     * Constructor. It is private since utility classes should not have a public
     * or default constructor.
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
