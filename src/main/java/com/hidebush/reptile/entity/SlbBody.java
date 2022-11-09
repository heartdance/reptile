package com.hidebush.reptile.entity;

import lombok.Data;

import java.util.List;

@Data
public class SlbBody {

    private String CLBZ;

    private String CLJG;

    private List<SlbInvoice> INVOICELIST;
}
