package com.hidebush.reptile.entity;

import lombok.Data;

@Data
public class SlbInvoice {

    private String HOS_ID;

    private String INVOICE_CODE;

    private String INVOICE_NUMBER;

    private String INVOICE_DATE;

    private String TOTAL_AMOUNT;

    private String INVOICE_URL;

    private String INVOICING_PARTY_NAME;

    private String PAYER_PARTY_NAME;

    private String SFTYPENAME;

    private String STATUS;
}
