package com.hidebush.reptile;

import com.beust.jcommander.JCommander;
import com.hidebush.reptile.entity.ReptileArgs;

public class Reptile {

    private static final String defaultPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAITtOWcU" +
            "TuZRTsXyXYX+AVvvXtUgPTxJ2o2PAjaHrxAY7Zp6b1eSCHQXefiw+J5RBIeghUqX//bhgU3ZvwKnwSefuM1uvKVGh1TC5fLu" +
            "nb0tvbe8zUGMGrKrt4pA+thWK/thVKoScysSa4kNkvHUp3ZcoQED3EcoySarJ4qGlDuLAgMBAAECgYAtjg8qIG+Zxyuz8wfS" +
            "L+bPVw2tBMt3qefYs7YSchWw8pobYvJdlJKJFrKaZCwQNbvTr1N+6PCz7zydLY6knlE3ft677ExwjNvwE4TG0lCrp0EPUplx" +
            "jsE7rWD12+6ZDDESBkGYOP+hxWPdHdDx9456SMAllCz/mgMSTxQSrQMPIQJBANrcgrfs+ejtvqo74M1GnchzVGPh7Oej6ISF" +
            "A+Am2YszIELNemJOBUtf33W4RDVmeVJ3UeuX+k6QAYpV0Uvgnw8CQQCbe6Z04Xx7aWJ+AYkXLdbcASM/pwgi81HZ6VAFryVJ" +
            "VFWKuP2B0zuDaesitFZxs8S9/wdbiA19mFZ64dfa2NvFAkBfuUKTH1rOve0+l6HjJpesLIUkipQLXG0+SM9BIrzTXTEnBqgY" +
            "hvdZ2DzsSAPNN1yo7Pcvi/E2m1WRI6e/ACeRAkAuoCzxnfDbfWXYzZMTzV5CSWENpIRPHtJr24hwu+4diFnCqOj0tqiBJxEp" +
            "LhVCMZuNMl49d5Y8FKPSY8l8Sk3VAkEAz4E4bUsFD4IQc08sTIQ2ZVYKpeN9rPDSplCeJkLMDJEQXt5dM/8ojnSl06dFRjPn" +
            "BcYfPY/vOGFWXli3LvvE/A==";

    private static final String defaultAesKey = "3268DTHG736D4C11659CIT9BJ86D4LPH";

    public static void main(String[] args) throws Exception {
        ReptileArgs reptileArgs = new ReptileArgs();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(reptileArgs)
                .build();
        jCommander.parse(args);

        if (reptileArgs.isHelp()) {
            jCommander.usage();
            return;
        }

        System.out.println("姓名：" + reptileArgs.getName());
        System.out.println("身份证号：" + reptileArgs.getIdCard());
        System.out.println("起始日期：" + reptileArgs.getStartDate());
        System.out.println("截止日期：" + reptileArgs.getEndDate());

        String privateKey = defaultPrivateKey;
        if (reptileArgs.getPrivateKey() != null && !reptileArgs.getPrivateKey().isEmpty()) {
            System.out.println("私钥：" + reptileArgs.getPrivateKey());
            privateKey = reptileArgs.getPrivateKey();
        }
        String aesKey = defaultAesKey;
        if (reptileArgs.getAesKey() != null && !reptileArgs.getAesKey().isEmpty()) {
            System.out.println("AES Key：" + reptileArgs.getAesKey());
            aesKey = reptileArgs.getAesKey();
        }
        int threadCount = 8;
        if (reptileArgs.getThreadCount() != null) {
            System.out.println("Download Thread：" + reptileArgs.getThreadCount());
            threadCount = reptileArgs.getThreadCount();
        }

        System.out.println("-----------------------------");

        try (ReptileExecutor executor = new ReptileExecutor(privateKey, aesKey, threadCount)) {
            executor.download(reptileArgs.getName(), reptileArgs.getIdCard(),
                    reptileArgs.getStartDate(), reptileArgs.getEndDate());
        }
    }

}
