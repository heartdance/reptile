package com.hidebush.reptile.entity;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.validators.PositiveInteger;
import lombok.Data;

@Data
public class ReptileArgs {

    @Parameter(names = "--help", help = true)
    private boolean help;

    @Parameter(
            names = { "--name", "-n" },
            description = "Name",
            required = true,
            validateValueWith = NameValidator.class
    )
    private String name;

    @Parameter(
            names = { "--idCard", "-i" },
            description = "ID Card",
            required = true,
            validateValueWith = IdCardValidator.class
    )
    private String idCard;

    @Parameter(
            names = { "--startDate", "-s" },
            description = "Start date, like 2022-01-01",
            required = true,
            validateValueWith = DateValidator.class
    )
    private String startDate;

    @Parameter(
            names = { "--endDate", "-e" },
            description = "End date, like 2022-12-31",
            required = true,
            validateValueWith = DateValidator.class
    )
    private String endDate;

    @Parameter(names = { "--privateKey", "-p" }, description = "RSA Private Key")
    private String privateKey;

    @Parameter(names = { "--aesKey", "-a" }, description = "AES key")
    private String aesKey;

    @Parameter(
            names = { "--threadCount", "-t" },
            description = "Download thread count",
            validateWith = PositiveInteger.class
    )
    private Integer threadCount;

    public static class NameValidator implements IValueValidator<String> {
        @Override
        public void validate(String name, String value) throws ParameterException {
            if (value.isEmpty()) {
                throw new ParameterException("Parameter " + name
                        + " should be positive (found " + value +")");
            }
        }
    }

    public static class IdCardValidator implements IValueValidator<String> {
        @Override
        public void validate(String name, String value) throws ParameterException {
            String idCardReg = "\\d{17}[\\dxX]";
            if (!value.matches(idCardReg)) {
                throw new ParameterException("Parameter " + name
                        + " should be 18 numbers or 17 numbers ending with x (found " + value +")");
            }
        }
    }

    public static class DateValidator implements IValueValidator<String> {
        @Override
        public void validate(String name, String value) throws ParameterException {
            String dateReg = "\\d{4}-\\d{2}-\\d{2}";
            if (!value.matches(dateReg)) {
                throw new ParameterException("Parameter " + name
                        + " should be a date like 2022-01-01 (found " + value +")");
            }
        }
    }
}
