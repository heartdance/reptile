package com.hidebush.reptile;

import com.alibaba.fastjson2.JSON;
import com.hidebush.reptile.entity.*;
import com.hidebush.reptile.util.Aes;
import com.hidebush.reptile.util.Rsa;
import okhttp3.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReptileExecutor implements AutoCloseable {

    private final OkHttpClient client = new OkHttpClient();

    private final String privateKey;

    private final String aesKey;

    private final ExecutorService service;

    public ReptileExecutor(String privateKey, String aesKey, int threadCount) {
        this.privateKey = privateKey;
        this.aesKey = aesKey;
        service = Executors.newFixedThreadPool(threadCount);
    }

    public void download(String name, String card, String begDate, String endDate) throws Exception {
        if (service.isShutdown()) {
            throw new IllegalStateException("executor service is shutdown");
        }
        long startTime = System.currentTimeMillis();
        File dir = new File(name);
        if (dir.exists()) {
            throw new RuntimeException("目录已存在，请先删除此目录：" + dir.getAbsolutePath());
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("创建文件夹失败：" + dir.getAbsolutePath());
        }
        System.out.println("开始获取医疗信息列表...");
        SlbData data = getInvoiceData(name, card, begDate, endDate);
        List<SlbInvoice> invoicelist = data.getBODY().getINVOICELIST();
        System.out.println("获取医疗信息列表完成，共 " + invoicelist.size() + " 项");
        System.out.println("开始下载pdf文件...");
        List<Future<?>> futures = new ArrayList<>(invoicelist.size());
        AtomicInteger counter = new AtomicInteger();
        Lock reportLock = new ReentrantLock();
        for (SlbInvoice slbInvoice : invoicelist) {
            Future<?> future = service.submit(() -> {
                String url = slbInvoice.getINVOICE_URL();
                Request request = new Request.Builder().url(url).build();
                try (Response response = client.newCall(request).execute()) {
                    ResponseBody body = response.body();
                    if (body == null) {
                        reportProgress(url, false, counter, invoicelist.size(), reportLock);
                        return;
                    }
                    byte[] bytes = body.bytes();
                    Files.write(Paths.get("./" + name + "/" +
                            slbInvoice.getINVOICE_DATE() + "_" + slbInvoice.getINVOICE_NUMBER() + ".pdf"), bytes);
                    reportProgress(url, true, counter, invoicelist.size(), reportLock);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
        }
        for (Future<?> future : futures) {
            future.get();
        }
        int seconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
        System.out.println("全部下载完成，共耗时 " + seconds + " 秒，下载目录为：" + dir.getAbsolutePath());
    }

    private void reportProgress(String url, boolean success, AtomicInteger counter, int total, Lock lock) {
        lock.lock();
        int downloadCount = counter.incrementAndGet();
        if (success) {
            System.out.println("已完成（" + downloadCount + "/" + total + "）");
        } else {
            System.out.println("下载失败：" + url + "，（" + downloadCount + "/" + total + "）");
        }
        lock.unlock();
    }

    private SlbData getInvoiceData(String name, String card, String begDate, String endDate) throws Exception {
        SlbParam slbParam = buildParam(name, card, begDate, endDate);
        String slbParamJson = JSON.toJSONString(slbParam);
        String param = Aes.encrypt(slbParamJson, aesKey);

        FormBody body = new FormBody.Builder()
                .add("param", param)
                .add("actionType", "PBusEletcinvoiceBus")
                .build();
        Request request = new Request.Builder().url("https://apph5.ztejsapp.cn/nj/slbJsonApi")
                .addHeader("Accept", "application/json")
//                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
//                .addHeader("Connection", "keep-alive")
//                .addHeader("Content-Length", "405")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", "HWWAFSESID=b026373f17db27cc71; HWWAFSESTIME=1666668738315")
                .addHeader("Host", "apph5.ztejsapp.cn")
                .addHeader("Origin", "https://apph5.ztejsapp.cn")
                .addHeader("qhsign", Rsa.sign(param, privateKey))
                .addHeader("Referer", "https://apph5.ztejsapp.cn/nj/view/elecInvoiceForOther/index.html?HOS_ID=160&USE_TYPE=1&REGION_ID=")
                .addHeader("sec-ch-ua", "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody resBody = response.body();
            if (resBody == null) {
                throw new RuntimeException("获取列表失败");
            }
            SlbRes slbRes = JSON.parseObject(resBody.string(), SlbRes.class);
            return JSON.parseObject(Aes.decrypt(slbRes.getData(), aesKey), SlbData.class);
        }
    }

    private SlbParam buildParam(String name, String card, String begDate, String endDate) {
        SlbParam param = new SlbParam();

        SlbParamHeader header = new SlbParamHeader();
        header.setMODULE("BusinessWCApp");
        header.setCZLX("0");
        header.setTYPE("INVOICEBATCHQUERY");

        SlbParamBody body = new SlbParamBody();
        body.setHOS_ID("160");
        body.setREGION_ID("");
        body.setSFZ_NO(card);
        body.setUSE_TYPE("1");
        body.setISSUE_BEG_DATE(begDate);
        body.setISSUE_END_DATE(endDate);
        body.setPAT_NAME(name);
        body.setBIZCODE("");

        param.setHEADER(header);
        param.setBODY(body);
        return param;
    }

    public synchronized void close() {
        service.shutdown();
    }

}
