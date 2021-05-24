package com.acutus.atk.util;

import java.util.Arrays;
import java.util.regex.Pattern;

public class MailHelper {
    private static String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static Pattern pattern = Pattern.compile(regex);

    public static boolean validate(String address) {
        return address != null && !address.trim().isEmpty() && !Arrays.stream(address.split(","))
                .filter(email -> !pattern.matcher(email).matches()).findAny().isPresent();
    }

    public static void main(String[] args) {

        System.out.println(validate("transport.stores@kgatelopele.gov.za,mm@kgatelopele.gov.za,techmanager@kgatelopele.gov.za,thando@nktt.co.za,senior.admin@kgatelopele.gov.za"));
    }
}
