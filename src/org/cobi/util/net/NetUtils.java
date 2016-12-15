/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
<<<<<<< HEAD
import java.nio.channels.FileChannel;
=======
>>>>>>> origin/master
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
<<<<<<< HEAD
import org.apache.http.client.config.RequestConfig;
=======
>>>>>>> origin/master
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.GlobalManager;
import org.cobi.kggseq.Options;
import org.cobi.kggseq.controller.Phenolyzer;
import org.cobi.util.download.stable.DownloadTaskEvent;
import org.cobi.util.download.stable.DownloadTaskListener;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.download.stable.HttpClient4DownloadTask;
import org.cobi.util.file.Tar;
import org.cobi.util.file.Zipper;

/**
 *
 * @author mxli
 */
public class NetUtils implements Constants {

    private static final Logger LOG = Logger.getLogger(NetUtils.class);

    public static void updateLocal() throws Exception {
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File copiedFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            File targetFile = new File(GlobalManager.LOCAL_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            //a file with size less than 1k is not normal
            if (copiedFile.length() > 1024 && copiedFile.length() != targetFile.length()) {
                copyFile(targetFile, copiedFile);
            }
        }
    }

    public static void copyFile(File targetFile, File sourceFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!sourceFile.exists()) {
                return;
            }

            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    targetFile.deleteOnExit();
                    //System.err.println("Cannot delete " + targetFile.getCanonicalPath());
                }
            }
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024 * 5];
            int size;
            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
                out.flush();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public static boolean needUpdate() throws Exception {
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            if (newLibFile.exists()) {
                long fileSize = newLibFile.length();
                String url = GlobalManager.KGGSeq_URL + URL_FILE_PATHES[i];
                long netFileLen = HttpClient4API.getContentLength(url);
                if (netFileLen <= 1024) {
                    return false;
                }
                if (fileSize != netFileLen) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean checkLibFileVersion(boolean autoUpdate) throws Exception {
        List<String> updatedLocalFiles = new ArrayList<String>();
        List<String> updatedURLFiles = new ArrayList<String>();
        boolean hasUpdated = false;
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            if (!newLibFile.exists()) {
                updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                updatedURLFiles.add(URL_FILE_PATHES[i]);
            } else {
                long fileSize = newLibFile.length();
                String url = GlobalManager.KGGSeq_URL + URL_FILE_PATHES[i];

                long netFileLen = HttpClient4API.getContentLength(url);
                if (netFileLen <= 0) {
                    updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                    updatedURLFiles.add(URL_FILE_PATHES[i]);
                }
                if (fileSize != netFileLen) {
                    updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                    updatedURLFiles.add(URL_FILE_PATHES[i]);
                }
            }
        }

        if (autoUpdate && !updatedLocalFiles.isEmpty()) {
            int MAX_TASK = 1;
            int runningThread = 0;
            ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
            CompletionService serv = new ExecutorCompletionService(exec);
            LOG.info("Updating libraries...");
            int filesNum = updatedLocalFiles.size();

            for (int i = 0; i < filesNum; i++) {
                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(GlobalManager.KGGSeq_URL + updatedURLFiles.get(i), 10);
                File newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + updatedLocalFiles.get(i));
                File libFolder = newLibFile.getParentFile();
                if (!libFolder.exists()) {
                    libFolder.mkdirs();
                }
                task.setLocalPath(newLibFile.getCanonicalPath());

                final String dbLabel = newLibFile.getName();
                task.addTaskListener(new DownloadTaskListener() {

                    @Override
                    public void autoCallback(DownloadTaskEvent event) {
                        int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                        String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                        System.out.print(infor);
                        char[] bs = new char[infor.length()];
                        Arrays.fill(bs, '\b');
                        System.out.print(bs);
                    }

                    @Override
                    public void taskCompleted() throws Exception {
                        String msg1 = dbLabel + " has been downloaded!";
                        LOG.info(msg1);
                    }
                });
                runningThread++;
                TimeUnit.MILLISECONDS.sleep(500);
                serv.submit(task);
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
                hasUpdated = true;
            }
            exec.shutdown();
            LOG.info("The library of has been updated! Please re-initiate this application!");
            updateLocal();
<<<<<<< HEAD
        } else if (!autoUpdate && !updatedLocalFiles.isEmpty()) {
=======
        } else if (!autoUpdate) {            
>>>>>>> origin/master
            LOG.info("A new version of KGGSeq is available! Please visit http://grass.cgs.hku.hk/limx/kggseq/download.php to see the updates\n and enable library updated automatically by '--lib-update'. To disable library checking, add '--no-lib-check'.");
        }
        return hasUpdated;
    }

    public static boolean checkInstallPhenolyzer(String strURL, File fleOutput) throws InterruptedException, Exception {
        boolean needDownload = false;
        try {
            //        File fleOutput=new File("D:\\01WORK\\KGGseq\\tool\\HttpsDownload\\phenolyzer-master.zip");
            if (!fleOutput.getParentFile().exists()) {
                fleOutput.getParentFile().mkdirs();
            }

            if (!fleOutput.exists()) {
                needDownload = true;
            } else {
                //half an year later
                long time = fleOutput.lastModified() / 1000 + 30 * 24 * 60 * 60;
                Date fileData = new Date(time * 1000);
                Date today = new Date();
                //if too small or too early
                if (fleOutput.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                    //an incomplete file
                    fleOutput.delete();
                    needDownload = true;
                }
            }

            if (needDownload) {
<<<<<<< HEAD
                CloseableHttpClient httpclient = HttpClients.custom()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                // Waiting for a connection from connection manager
                                .setConnectionRequestTimeout(10000)
                                // Waiting for connection to establish
                                .setConnectTimeout(5000)
                                .setExpectContinueEnabled(false)
                                // Waiting for data
                                .setSocketTimeout(5000)
                                .setCookieSpec("easy")
                                .build())
                        .setMaxConnPerRoute(20)
                        .setMaxConnTotal(100)
                        .build();
                HttpGet httpget = new HttpGet(strURL);
                HttpResponse response = httpclient.execute(httpget);
                //System.out.println(response.getStatusLine());
=======
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet(strURL);
                HttpResponse response = httpclient.execute(httpget);
                System.out.println(response.getStatusLine());
>>>>>>> origin/master
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    try (InputStream in = entity.getContent()) {
                        BufferedInputStream bis = new BufferedInputStream(in);
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fleOutput));
                        int intCount;
                        //                    NumberFormat nf=NumberFormat.getPercentInstance();
                        //                    nf.setMinimumFractionDigits(2);
                        //                    nf.setMaximumIntegerDigits(3);
                        //                    System.out.println();
                        byte[] buffer = new byte[10 * 1024];
                        while ((intCount = bis.read(buffer)) != -1) {
                            //                        for(int i=0;i<7;i++)    System.out.print("\b");
                            //                        Thread.sleep(100);
                            //                        System.out.print("\r");
                            bos.write(buffer, 0, intCount);

                            //                        double dbl=longAdd/longSize;
                            //                        System.out.print(nf.format(dbl));
                        }
                        bis.close();
                        bos.close();

                        Zipper ziper = new Zipper();
                        ziper.extractZip(fleOutput.getCanonicalPath(), fleOutput.getParentFile().getCanonicalPath() + File.separator);

                        String path = fleOutput.getCanonicalPath();
                        String[] params = new String[2];
                        params[0] = "cd";
                        params[1] = path.substring(0, path.length() - 4);
                        Process pr = Runtime.getRuntime().exec("bash -c cd " + params[1] + " && bash -c make");

                        String line;
                        StringBuilder comInfor = new StringBuilder();
                        StringBuilder errInfor = new StringBuilder();
                        try {

                            BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                            while (((line = inputError.readLine()) != null)) {
                                errInfor.append(line);
                                errInfor.append("\n");
                            }

                            int exitVal = pr.waitFor();
                            pr.destroy();
                            inputError.close();
                            for (String param : params) {
                                comInfor.append(param);
                                comInfor.append(" ");
                            }
                            if (exitVal != 0) {
                                LOG.info("Failed to run " + comInfor + "\n" + errInfor);
                            }

                        } catch (Exception ex) {
                            LOG.error(ex);
                        }

                    } catch (IOException ex) {
                        LOG.error(ex);
                    } catch (RuntimeException ex) {
                        httpget.abort();
                    }
                    httpclient.close();
                }
            } else {
                // Zipper ziper = new Zipper();
                //  ziper.extractZip(fleOutput.getCanonicalPath(), fleOutput.getParentFile().getCanonicalPath() + File.separator);
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Phenolyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return needDownload;
    }

    //https://github.com/samtools/tabix/archive/master.zip    
    public static boolean checkInstallCpp(String strURL, File fleOutput) throws InterruptedException, Exception {
        boolean needDownload = false;
        String[] params = new String[2];
        try {
            //        File fleOutput=new File("D:\\01WORK\\KGGseq\\tool\\HttpsDownload\\phenolyzer-master.zip");
            if (!fleOutput.getParentFile().exists()) {
                fleOutput.getParentFile().mkdirs();
            }

            if (!fleOutput.exists()) {
                needDownload = true;
            } else {
                //half an year later
                long time = fleOutput.lastModified() / 1000 + 30 * 24 * 60 * 60;
                Date fileData = new Date(time * 1000);
                Date today = new Date();
                //if too small or too early
                if (fleOutput.length() < 0.01 * 1024 * 1024 || today.after(fileData)) {
                    //an incomplete file
                    fleOutput.delete();
                    needDownload = true;
                }
            }

            if (needDownload) {
<<<<<<< HEAD
                CloseableHttpClient httpclient = HttpClients.custom()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                // Waiting for a connection from connection manager
                                .setConnectionRequestTimeout(10000)
                                // Waiting for connection to establish
                                .setConnectTimeout(5000)
                                .setExpectContinueEnabled(false)
                                // Waiting for data
                                .setSocketTimeout(5000)
                                .setCookieSpec("easy")
                                .build())
                        .setMaxConnPerRoute(20)
                        .setMaxConnTotal(100)
                        .build();

                HttpGet httpget = new HttpGet(strURL);
                HttpResponse response = httpclient.execute(httpget);
                // System.out.println(response.getStatusLine());
=======
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet(strURL);
                HttpResponse response = httpclient.execute(httpget);
                System.out.println(response.getStatusLine());
>>>>>>> origin/master
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    try (InputStream in = entity.getContent()) {
                        BufferedInputStream bis = new BufferedInputStream(in);
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fleOutput));
                        int intCount;
                        //                    NumberFormat nf=NumberFormat.getPercentInstance();
                        //                    nf.setMinimumFractionDigits(2);
                        //                    nf.setMaximumIntegerDigits(3);
                        //                    System.out.println();
                        byte[] buffer = new byte[10 * 1024];
                        while ((intCount = bis.read(buffer)) != -1) {
                            //                        for(int i=0;i<7;i++)    System.out.print("\b");
                            //                        Thread.sleep(100);
                            //                        System.out.print("\r");
                            bos.write(buffer, 0, intCount);

                            //                        double dbl=longAdd/longSize;
                            //                        System.out.print(nf.format(dbl));
                        }
                        bis.close();
                        bos.close();

                        Zipper ziper = new Zipper();
                        String path = fleOutput.getCanonicalPath();
                        ziper.extractZip(path, fleOutput.getParentFile().getCanonicalPath() + File.separator);

                        params[0] = "cd";
                        params[1] = path.substring(0, path.length() - 4);
                        String info = "bash -c cd " + params[1] + " && bash -c make";

                        System.out.println(info);
                        Process pr = Runtime.getRuntime().exec(info);

                        String line;
                        StringBuilder comInfor = new StringBuilder();
                        StringBuilder errInfor = new StringBuilder();
                        try {
                            BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                            while (((line = inputError.readLine()) != null)) {
                                errInfor.append(line);
                                errInfor.append("\n");
                            }

                            int exitVal = pr.waitFor();
                            pr.destroy();
                            inputError.close();
                            for (String param : params) {
                                comInfor.append(param);
                                comInfor.append(" ");
                            }
                            if (exitVal != 0) {
                                LOG.info("Failed to run " + comInfor + "\n" + errInfor);
                            }

                        } catch (Exception ex) {
                            LOG.error(ex);
                            LOG.info("The plug from the source " + strURL + " couldn't install automatically! Please make the file in person and re-run your command.");
                        }

                    } catch (IOException ex) {
                        LOG.error(ex);
                    } catch (RuntimeException ex) {
                        httpget.abort();
                    }
                    httpclient.close();
                }
            } else {
                // Zipper ziper = new Zipper();
                //  ziper.extractZip(fleOutput.getCanonicalPath(), fleOutput.getParentFile().getCanonicalPath() + File.separator);
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Phenolyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (needDownload) {
<<<<<<< HEAD
            String infor = "Please go to the folder by typing 'cd " + params[1] + "' and complile the cpp codes by typing 'make all'";
=======
            String infor = "Please go to the folder by typing 'cd " + params[1] + "' and complile the cpp codes by typing 'make'";
>>>>>>> origin/master
            LOG.info(infor);
        }
        return needDownload;
    }

    public static void checkResourceList(final Options options) {
        int MAX_TASK = 1;
        boolean toDownload = false;
        ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
        CompletionService serv = new ExecutorCompletionService(exec);
        int runningThread = 0;
        try {
            long startTime = System.currentTimeMillis();
            List<String> checkLabels = new ArrayList<String>();
            checkLabels.addAll(options.varaintDBLableList);
            checkLabels.addAll(options.varaintDBLableHardList);
            if (options.geneDBLabels != null) {
                checkLabels.addAll(Arrays.asList(options.geneDBLabels));
            }

            if (options.scoreDBLableList.contains("dbnsfp")) {
                String dbFileName = options.PUBDB_FILE_MAP.get("dbnsfp");
                String url = options.PUBDB_URL_MAP.get("dbnsfp");
                for (int j = 0; j < 24; j++) {
                    String newLabel = "dbnsfp" + STAND_CHROM_NAMES[j];
                    options.PUBDB_FILE_MAP.put(newLabel, dbFileName + STAND_CHROM_NAMES[j] + ".gz");
                    options.PUBDB_URL_MAP.put(newLabel, url + STAND_CHROM_NAMES[j] + ".gz");
                    checkLabels.add(newLabel);
                }
            }
            String refGenomeVersion = options.refGenomeVersion;

            if (options.ncFuncPred) {
                // download resources from gene-feature specific annotation function
                String[] annotationScore = {"funcnote_all.footprints.bed.gz.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.DNase-seq.cmp.gz", "funcnote_encode_megamix.bed.gz.FAIRE-seq.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.Histone.cmp.gz", "funcnote_encode_megamix.bed.gz.Tfbs.cmp.gz"};
                for (int j = 0; j < annotationScore.length; j++) {
                    String newLabel = "noncoding" + j;
                    options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    checkLabels.add(newLabel);
                }
                options.PUBDB_FILE_MAP.put("hgmd_model", "hgmd_model.obj");
                options.PUBDB_URL_MAP.put("hgmd_model", KGGSeq_URL + "/download/resources/modelFile/" + "hgmd_model.obj");
                checkLabels.add("hgmd_model");
            }
            /*
            String[] dbncfpAnnotationScore = new String[]{"known_SNV_dbNCFP.aa", "known_SNV_dbNCFP.ab", "known_SNV_dbNCFP.ac",
                "known_SNV_dbNCFP.ad", "known_SNV_dbNCFP.ae", "known_SNV_dbNCFP.af", "known_SNV_dbNCFP.ag",
                "known_SNV_dbNCFP.ah", "known_SNV_dbNCFP.ai", "known_SNV_dbNCFP.aj"};
             */

            String[] dbncfpAnnotationScore = new String[]{"known_SNV_dbNCFP.a.aa.gz", "known_SNV_dbNCFP.a.ab.gz", "known_SNV_dbNCFP.a.ac.gz",
                "known_SNV_dbNCFP.a.ad.gz", "known_SNV_dbNCFP.a.ae.gz", "known_SNV_dbNCFP.a.af.gz", "known_SNV_dbNCFP.a.ag.gz",
                "known_SNV_dbNCFP.a.ah.gz", "known_SNV_dbNCFP.a.ai.gz"};

            boolean needMergeDncfp = false;
            if (options.scoreDBLableList.contains("dbncfp_known")) {
                File rfFile = new File(GlobalManager.RESOURCE_PATH + "/" + refGenomeVersion + "/" + refGenomeVersion + "_known_SNV_dbNCFP.gz");
                if (!rfFile.exists()) {
                    // download resources for cell-type specific annotation function             
                    for (int j = 0; j < dbncfpAnnotationScore.length; j++) {
                        String newLabel = "dbncfp_known" + j;
                        options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + dbncfpAnnotationScore[j]);
                        options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + dbncfpAnnotationScore[j]);
                        checkLabels.add(newLabel);
                    }
                    /*
                String newLabel = "cell_signal";
                options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                checkLabels.add(newLabel);
                     */
                }

            }
            if (options.ncRegPred) {
                options.PUBDB_FILE_MAP.put("all_causal_distribution", "all_causal_distribution.zip");
                options.PUBDB_URL_MAP.put("all_causal_distribution", KGGSeq_URL + "/download/resources/" + "all_causal_distribution.zip");
                options.PUBDB_FILE_MAP.put("all_neutral_distribution", "all_neutral_distribution.zip");
                options.PUBDB_URL_MAP.put("all_neutral_distribution", KGGSeq_URL + "/download/resources/" + "all_neutral_distribution.zip");
                checkLabels.add("all_causal_distribution");
                checkLabels.add("all_neutral_distribution");
            }

            if (options.dgvcnvAnnotate) {
                checkLabels.add("dgvcnv");
            }
            if (options.mergeGtyDb != null) {
                String piResource = options.PUBDB_URL_MAP.get(options.mergeGtyDb);
                String url = "http://grass.cgs.hku.hk/limx/genotypes/";

                if (piResource != null) {
                    if (piResource.contains("_CHROM_")) {
                        for (int j = 0; j < 23; j++) {
                            String newLabel = "mergeddb" + STAND_CHROM_NAMES[j];
                            //remove resources/
                            options.PUBDB_FILE_MAP.put(newLabel, piResource.substring(10).replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            options.PUBDB_URL_MAP.put(newLabel, url + piResource.replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            checkLabels.add(newLabel);
                            // System.out.println(piResource);                            
                        }
                        options.mergeGtyDb = piResource.substring(0, piResource.indexOf(".chr_"));
                        options.mergeGtyDb = options.mergeGtyDb.substring(10);
                    } else {
                        String newLabel = "mergeddb";
                        options.PUBDB_URL_MAP.put(newLabel, url + piResource);
                        checkLabels.add(newLabel);
                        options.mergeGtyDb = piResource.substring(10);
                        options.PUBDB_FILE_MAP.put(newLabel, options.mergeGtyDb);
                        //remove the tar lable
                        options.mergeGtyDb = options.mergeGtyDb.substring(0, options.mergeGtyDb.lastIndexOf('.'));
                    }
                }
            }
            //force to download the small database 
            checkLabels.add("cano");
            checkLabels.add("cura");
            checkLabels.add("onco");
            checkLabels.add("cmop");
            checkLabels.add("onto");

            checkLabels.add("morbidmap");

            checkLabels.add("string");
            checkLabels.add("ideogram");

            checkLabels.add("proteindomain");
            checkLabels.add("uniportrefseqmap");
            checkLabels.add("uniportgencodemap");
            checkLabels.add("uniportucscgenemap");

            checkLabels.add("mendelcausalrare.param");
            checkLabels.add("cancer.param");
            checkLabels.add("cancer.mutsig");
            checkLabels.add("mendelgene");

            checkLabels.add("cancer.null.driver.score");
            if (options.rsid) {
                checkLabels.add("rsid");
            }
            if (options.dbscSNVAnnote) {
                checkLabels.add("dbscSNV");
            }
            if (options.cosmicAnnotate) {
                checkLabels.add("cosmicdb");
            }
            if (options.isTFBSCheck) {
                checkLabels.add("tfbs");
            }
            if (options.isEnhancerCheck) {
                checkLabels.add("enhancer");
            }

            if (options.superdupAnnotate || options.superdupFilter) {
                checkLabels.add("superdup");
            }

            if (options.zebraFish) {
                checkLabels.add("zebrafish.pheno");
            }

            List<String> toDownloadList = new ArrayList<String>();

            boolean toDownloadHGNC = false;
            String hgncFileName = "HgncGene.txt";
            File resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);

            if (!resourceFile.exists()) {
                toDownloadHGNC = true;
                toDownloadList.add(hgncFileName);
            } else {
                //one month later
                long time = resourceFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                Date fileData = new Date(time * 1000);
                Date today = new Date();
                //if too small or too early
                if (resourceFile.length() < 1.2 * 1024 * 1024 || today.after(fileData)) {
                    //an incomplete file
                    resourceFile.delete();
                    toDownloadList.add(hgncFileName);
                    toDownloadHGNC = true;
                }
            }

            boolean toDownloadMousePheno = false;
            if (options.phenoMouse) {
                checkLabels.add("impcmouse");
                File fleMouse1 = new File(GlobalManager.RESOURCE_PATH + "/" + "HMD_HumanPhenotype.rpt.txt");
                File fleMouse2 = new File(GlobalManager.RESOURCE_PATH + "/" + "VOC_MammalianPhenotype.rpt.txt");
                if (!fleMouse1.exists() || !fleMouse2.exists()) {
                    toDownloadMousePheno = true;
                } else {
                    if (fleMouse1.exists()) {
                        //one month later
                        long time = fleMouse1.lastModified() / 1000 + 30 * 24 * 60 * 60;
                        Date fileData = new Date(time * 1000);
                        Date today = new Date();
                        //if too small or too early
                        if (fleMouse1.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                            //an incomplete file                            
                            fleMouse1.delete();
                            toDownloadMousePheno = true;
                        }
                    }
                    if (!toDownloadMousePheno && fleMouse2.exists()) {
                        //one month later
                        long time = fleMouse2.lastModified() / 1000 + 30 * 24 * 60 * 60;
                        Date fileData = new Date(time * 1000);
                        Date today = new Date();
                        //if too small or too early
                        if (fleMouse2.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                            //an incomplete file
                            fleMouse2.delete();
                            toDownloadMousePheno = true;
                        }
                    }
                }
                if (toDownloadMousePheno) {
                    FTP ftp = new FTP("64.147.54.33", 21, "anonymous", "");
                    if (ftp.openFTP()) {
                        ftp.downloadFTP("/pub/reports/HMD_HumanPhenotype.rpt", fleMouse1);
                        ftp.downloadFTP("/pub/reports/VOC_MammalianPhenotype.rpt", fleMouse2);
                    }
                    ftp.closeFTP();
                }

                /*
                //IMPC database
                //ftp://ftp.ebi.ac.uk/pub/databases/impc/release-4.0/csv/ALL_genotype_phenotype.csv.gz.
                fleMouse1 = new File(GlobalManager.RESOURCE_PATH + "/" + "ALL_genotype_phenotype.csv.gz");

                if (!fleMouse1.exists()) {
                    toDownloadMousePheno = true;
                } else if (fleMouse1.exists()) {
                    //one month later
                    long time = fleMouse1.lastModified() / 1000 + 30 * 24 * 60 * 60;
                    Date fileData = new Date(time * 1000);
                    Date today = new Date();
                    //if too small or too early
                    if (fleMouse1.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                        //an incomplete file
                        resourceFile.delete();
                        toDownloadMousePheno = true;
                    }
                }
                if (toDownloadMousePheno) {
                    FTP ftp = new FTP("ftp.ebi.ac.uk", 21, "anonymous", "");
                    if (ftp.openFTP()) {
                        ftp.downloadFTP("/pub/databases/impc/release-4.0/csv/ALL_genotype_phenotype.csv.gz", fleMouse1);
                    }
                    ftp.closeFTP();
                }
                 */
<<<<<<< HEAD
            }
            if (options.dddPhenotypes) {
                boolean needDownload = false;
                File rfFile = new File(GlobalManager.RESOURCE_PATH + "/DDG2P.csv.gz");
                if (!rfFile.exists()) {
                    needDownload = true;
                } else {
                    //one month later
                    long time = rfFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                    Date fileData = new Date(time * 1000);
                    Date today = new Date();
                    //if too small or too early
                    if (rfFile.length() < 0.2 * 1024 || today.after(fileData)) {
                        //an incomplete file
                        rfFile.delete();
                        needDownload = true;
                    }
                }

                if (needDownload) {
                    HttpClient4API.simpleRetriever("http://www.ebi.ac.uk/gene2phenotype/downloads/DDG2P.csv.gz", rfFile.getCanonicalPath());

                }
            }
=======
            }
            if (options.dddPhenotypes) {
                boolean needDownload = false;
                File rfFile = new File(GlobalManager.RESOURCE_PATH + "/ddg2p/ddg2p.zip");
                if (!rfFile.exists()) {
                    needDownload = true;
                } else {
                    //one month later
                    long time = rfFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                    Date fileData = new Date(time * 1000);
                    Date today = new Date();
                    //if too small or too early
                    if (rfFile.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                        //an incomplete file
                        rfFile.delete();
                        needDownload = true;
                    }
                }

                if (needDownload) {
                    HttpClient4API.simpleRetriever("https://decipher.sanger.ac.uk/files/ddd/ddg2p.zip", rfFile.getCanonicalPath());
                    Zipper ziper = new Zipper();
                    ziper.extractZip(rfFile.getCanonicalPath(), rfFile.getParentFile().getCanonicalPath() + File.separator);
                }

            }
>>>>>>> origin/master

            if (options.flankingSequence > 0) {
                for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                    if (STAND_CHROM_NAMES[i].equals("XY") || STAND_CHROM_NAMES[i].equals("Un")) {
                        continue;
                    }
                    File fastAFile = new File(GlobalManager.RESOURCE_PATH + "/" + options.refGenomeVersion + "/chr" + STAND_CHROM_NAMES[i] + ".fa.gz");
                    if (!fastAFile.exists()) {
                        String[] fileURL = new String[2];
                        fileURL[0] = options.refGenomeVersion + "/chr" + STAND_CHROM_NAMES[i] + ".fa.gz";
                        fileURL[1] = "http://hgdownload.cse.ucsc.edu/goldenPath/" + options.refGenomeVersion + "/chromosomes/chr" + STAND_CHROM_NAMES[i] + ".fa.gz";

                        String newLabel = "fasta" + STAND_CHROM_NAMES[i];
                        //remove resources/
                        options.PUBDB_FILE_MAP.put(newLabel, fileURL[0]);
                        options.PUBDB_URL_MAP.put(newLabel, fileURL[1]);
                        checkLabels.add(newLabel);
                    }
                }
            }

            for (String dbLabelName : checkLabels) {
                String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + dbFileName);
                // System.out.println(resourceFile.getCanonicalPath());
                if (resourceFile.exists()) {
                    continue;
                }
                toDownloadList.add(dbLabelName);
            }

            if (!GlobalManager.isConnectInternet && !toDownloadList.isEmpty()) {
                String infor = "KGGSeq stopped due to lack of the following resource data:";
                LOG.info(infor);

                for (String dbLabelName : toDownloadList) {
                    LOG.info(dbLabelName);
                }
                System.exit(1);
            } else if (toDownloadList.isEmpty()) {
                return;
            }

            //downloading does not support multiple thread 
<<<<<<< HEAD
            //String url = "http://www.genenames.org/cgi-bin/hgnc_downloads?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_pub_eg_id&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";
            String url = "http://www.genenames.org/cgi-bin/download?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_pub_eg_id&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";
=======
            String url = "http://www.genenames.org/cgi-bin/hgnc_downloads?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_pub_eg_id&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";
>>>>>>> origin/master

            if (toDownloadHGNC) {
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);
                String msg1 = "Donwloading HGNC gene annotations ... ";
                LOG.info(msg1);
                File parePath = resourceFile.getParentFile();
                if (!parePath.exists()) {
                    parePath.mkdirs();
                }
                HttpClient4API.simpleRetriever(url, resourceFile.getCanonicalPath());
                toDownload = true;
            }
            int i = 0;
            for (String dbLabelName : checkLabels) {
                String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + dbFileName);
                if (resourceFile.exists()) {
                    continue;
                } else {
                    File parePath = resourceFile.getParentFile();
                    if (!parePath.exists()) {
                        parePath.mkdirs();
                    }
                }
                if (i == 0) {
                    LOG.info("Downloading resource " + dbLabelName);
                }
                if (dbLabelName.startsWith("dbncfp_known")) {
                    needMergeDncfp = true;
                }
                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(options.PUBDB_URL_MAP.get(dbLabelName), 20);

                String urlT = options.PUBDB_URL_MAP.get(dbLabelName);
                if (urlT != null) {
                    if (urlT.startsWith("http://grass.cgs.hku.hk/")) {
                        task.setDataMd5(HttpClient4API.getContent(urlT + ".md5"));
                    }
                    File filePath = new File(GlobalManager.RESOURCE_PATH);
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                    task.setLocalPath(resourceFile.getCanonicalPath());
                    final String dbLabel = dbLabelName;
                    task.addTaskListener(new DownloadTaskListener() {

                        @Override
                        public void autoCallback(DownloadTaskEvent event) {
                            int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                            String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                            System.out.print(infor);
                            char[] bs = new char[infor.length()];
                            Arrays.fill(bs, '\b');
                            System.out.print(bs);
                        }

                        @Override
                        public void taskCompleted() throws Exception {
                            // File savedFile = new File(task.getLocalPath()); 
                            if (task.getLocalPath().endsWith(".tar")) {
                                Tar.untar(task.getLocalPath(), GlobalManager.RESOURCE_PATH + options.refGenomeVersion + File.separator);
                                File file = new File(task.getLocalPath());
                                // file.delete();
                            } else if (task.getLocalPath().endsWith(".zip")) {
                                Zipper ziper = new Zipper();
                                File file = new File(task.getLocalPath());
                                ziper.extractZip(task.getLocalPath(), file.getParent() + File.separator);
                                // file.delete();
                            }

                            /**
                             * File file = new File(task.getLocalPath() +
                             * ".md5"); // if file doesnt exists, then create it
                             * if (!file.exists()) { file.createNewFile(); }
                             *
                             * FileWriter fw = new
                             * FileWriter(file.getAbsoluteFile());
                             * BufferedWriter bw = new BufferedWriter(fw);
                             * bw.write(task.getDataMd5()); bw.close();
                             */
                            String msg1 = "Resource " + dbLabel + " has been downloaded!";
                            LOG.info(msg1);
                        }
                    });

                    runningThread++;
                    TimeUnit.MILLISECONDS.sleep(500);
                    serv.submit(task);
                    toDownload = true;
                    i++;
                }
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
            }
            exec.shutdown();
            if (needMergeDncfp) {
                concatenateFiles(GlobalManager.RESOURCE_PATH + refGenomeVersion + "/" + refGenomeVersion + "_", dbncfpAnnotationScore, "known_SNV_dbNCFP.gz");
            }
            if (toDownload) {
                StringBuilder inforString = new StringBuilder();
                inforString.append("The lapsed time for downloading is : ");
                long endTime = System.currentTimeMillis();
                inforString.append((endTime - startTime) / 1000.0);
                inforString.append(" Seconds.\n");
                LOG.info(inforString.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void downloadFiles(List<String[]> fileURLList) {
        int MAX_TASK = 1;
        boolean toDownload = false;
        ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
        CompletionService serv = new ExecutorCompletionService(exec);
        int runningThread = 0;
        int i = 0;
        try {
            long startTime = System.currentTimeMillis();

            for (String[] fileURL : fileURLList) {
                File resourceFile = new File(fileURL[0]);
                if (resourceFile.exists()) {
                    long fileSize = resourceFile.length();
                    long netFileLen = HttpClient4API.getContentLength(fileURL[1]);
                    if (netFileLen <= 1024 || fileSize == netFileLen) {
                        continue;
                    }
                }

                if (i == 0) {
                    LOG.info("Downloading resources ...");
                }
                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(fileURL[1], 20);

                if (!resourceFile.getParentFile().exists()) {
                    resourceFile.getParentFile().mkdirs();
                }
                task.setLocalPath(resourceFile.getCanonicalPath());
                final String dbLabel = resourceFile.getName();
                task.addTaskListener(new DownloadTaskListener() {

                    @Override
                    public void autoCallback(DownloadTaskEvent event) {
                        int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                        String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                        System.out.print(infor);
                        char[] bs = new char[infor.length()];
                        Arrays.fill(bs, '\b');
                        System.out.print(bs);
                    }

                    @Override
                    public void taskCompleted() throws Exception {
                        String msg1 = "Resource " + dbLabel + " has been downloaded!";
                        LOG.info(msg1);
                    }
                });
                runningThread++;
                TimeUnit.MILLISECONDS.sleep(500);
                serv.submit(task);
                toDownload = true;

                i++;
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
            }
            exec.shutdown();
            if (toDownload) {
                StringBuilder inforString = new StringBuilder();
                inforString.append("The lapsed time for downloading is : ");
                long endTime = System.currentTimeMillis();
                inforString.append((endTime - startTime) / 1000.0);
                inforString.append(" Seconds.\n");
                LOG.info(inforString.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void checkLatestResource(final Options options) {
        int MAX_TASK = 1;
        boolean toDownload = false;

        int runningThread = 0;

        try {
            long startTime = System.currentTimeMillis();
            List<String> checkLabels = new ArrayList<String>();
            checkLabels.addAll(options.varaintDBLableList);
            checkLabels.addAll(options.varaintDBLableHardList);

            if (options.geneDBLabels != null) {
                checkLabels.addAll(Arrays.asList(options.geneDBLabels));
            }

            if (options.scoreDBLableList.contains("dbnsfp")) {
                String dbFileName = options.PUBDB_FILE_MAP.get("dbnsfp");
                String url = options.PUBDB_URL_MAP.get("dbnsfp");
                for (int j = 0; j < 24; j++) {
                    String newLabel = "dbnsfp" + STAND_CHROM_NAMES[j];
                    options.PUBDB_FILE_MAP.put(newLabel, dbFileName + STAND_CHROM_NAMES[j] + ".gz");
                    options.PUBDB_URL_MAP.put(newLabel, url + STAND_CHROM_NAMES[j] + ".gz");
                    checkLabels.add(newLabel);
                }
            }

            if (options.mergeGtyDb != null) {
                String piResource = options.PUBDB_URL_MAP.get(options.mergeGtyDb);
                String url = "http://grass.cgs.hku.hk/limx/genotypes/";

                if (piResource != null) {
                    if (piResource.contains("_CHROM_")) {
                        for (int j = 0; j < 23; j++) {
                            String newLabel = "mergeddb" + STAND_CHROM_NAMES[j];
                            //remove resources/
                            options.PUBDB_FILE_MAP.put(newLabel, piResource.substring(10).replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            options.PUBDB_URL_MAP.put(newLabel, url + piResource.replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            checkLabels.add(newLabel);
                            // System.out.println(piResource);                            
                        }
                        options.mergeGtyDb = piResource.substring(0, piResource.indexOf(".chr_"));
                        options.mergeGtyDb = options.mergeGtyDb.substring(10);
                    } else {
                        String newLabel = "mergeddb";
                        options.PUBDB_URL_MAP.put(newLabel, url + piResource);
                        checkLabels.add(newLabel);
                        options.mergeGtyDb = piResource.substring(10);
                        options.PUBDB_FILE_MAP.put(newLabel, options.mergeGtyDb);
                        //remove the tar lable
                        options.mergeGtyDb = options.mergeGtyDb.substring(0, options.mergeGtyDb.lastIndexOf('.'));
                    }
                }
            }

            String refGenomeVersion = options.refGenomeVersion;
            if (options.ncFuncPred) {
                // download resources from gene-feature specific annotation function
                String[] annotationScore = {"funcnote_all.footprints.bed.gz.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.DNase-seq.cmp.gz", "funcnote_encode_megamix.bed.gz.FAIRE-seq.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.Histone.cmp.gz", "funcnote_encode_megamix.bed.gz.Tfbs.cmp.gz"};
                for (int j = 0; j < annotationScore.length; j++) {
                    String newLabel = "noncoding" + j;
                    options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    checkLabels.add(newLabel);
                }
                options.PUBDB_FILE_MAP.put("hgmd_model", "hgmd_model.obj");
                options.PUBDB_URL_MAP.put("hgmd_model", KGGSeq_URL + "/download/resources/modelFile/" + "hgmd_model.obj");
                checkLabels.add("hgmd_model");
            }
            /*
            String[] dbncfpAnnotationScore = new String[]{"hg19_known_SNV_dbNCFP.aa", "hg19_known_SNV_dbNCFP.ab", "hg19_known_SNV_dbNCFP.ac",
                "hg19_known_SNV_dbNCFP.ad", "hg19_known_SNV_dbNCFP.ae", "hg19_known_SNV_dbNCFP.af", "hg19_known_SNV_dbNCFP.ag",
                "hg19_known_SNV_dbNCFP.ah", "hg19_known_SNV_dbNCFP.ai", "hg19_known_SNV_dbNCFP.aj"};
             */
            String[] dbncfpAnnotationScore = new String[]{"known_SNV_dbNCFP.a.aa.gz", "known_SNV_dbNCFP.a.ab.gz", "known_SNV_dbNCFP.a.ac.gz",
                "known_SNV_dbNCFP.a.ad.gz", "known_SNV_dbNCFP.a.ae.gz", "known_SNV_dbNCFP.a.af.gz", "known_SNV_dbNCFP.a.ag.gz",
                "known_SNV_dbNCFP.a.ah.gz", "known_SNV_dbNCFP.a.ai.gz"};

            boolean needMergeDncfp = false;
            if (options.scoreDBLableList.contains("dbncfp_known")) {
                File rfFile = new File(GlobalManager.RESOURCE_PATH + "/" + refGenomeVersion + "/" + refGenomeVersion + "_known_SNV_dbNCFP.gz");
                if (!rfFile.exists()) // download resources for cell-type specific annotation function
                {
                    needMergeDncfp = true;
                } else {
                    long fileSize = rfFile.length();
                    long netFileLen = HttpClient4API.getContentLength(KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_known_SNV_dbNCFP.gz");
                    if (netFileLen <= 1024 || fileSize == netFileLen) {
                        needMergeDncfp = true;
                    }
                }
                if (needMergeDncfp) {
                    for (int j = 0; j < dbncfpAnnotationScore.length; j++) {
                        String newLabel = "dbncfp_known" + j;
                        options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + dbncfpAnnotationScore[j]);
                        options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + dbncfpAnnotationScore[j]);
                        checkLabels.add(newLabel);
                    }
                }
                /*
                String newLabel = "cell_signal";
                options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                checkLabels.add(newLabel);
                 */
            }
            if (options.ncRegPred) {
                options.PUBDB_FILE_MAP.put("all_causal_distribution", "all_causal_distribution.zip");
                options.PUBDB_URL_MAP.put("all_causal_distribution", KGGSeq_URL + "/download/resources/" + "all_causal_distribution.zip");
                options.PUBDB_FILE_MAP.put("all_neutral_distribution", "all_neutral_distribution.zip");
                options.PUBDB_URL_MAP.put("all_neutral_distribution", KGGSeq_URL + "/download/resources/" + "all_neutral_distribution.zip");
                checkLabels.add("all_causal_distribution");
                checkLabels.add("all_neutral_distribution");
            }

            if (options.dgvcnvAnnotate) {
                checkLabels.add("dgvcnv");
            }

            //force to download the small database
            checkLabels.add("cano");
            checkLabels.add("cura");
            checkLabels.add("onco");
            checkLabels.add("cmop");
            checkLabels.add("onto");

            checkLabels.add("morbidmap");

            checkLabels.add("string");
            checkLabels.add("ideogram");
            checkLabels.add("proteindomain");
            checkLabels.add("uniportrefseqmap");
            checkLabels.add("uniportgencodemap");
            checkLabels.add("uniportucscgenemap");
            checkLabels.add("mendelcausalrare.param");
            checkLabels.add("cancer.param");
            checkLabels.add("cancer.mutsig");
            checkLabels.add("mendelgene");
            checkLabels.add("cancer.null.driver.score");
            if (options.rsid) {
                checkLabels.add("rsid");
            }
            if (options.dbscSNVAnnote) {
                checkLabels.add("dbscSNV");
            }
            if (options.cosmicAnnotate) {
                checkLabels.add("cosmicdb");
            }

            if (options.isTFBSCheck) {
                checkLabels.add("tfbs");
            }
            if (options.isEnhancerCheck) {
                checkLabels.add("enhancer");
            }

            if (options.superdupAnnotate || options.superdupFilter) {
                checkLabels.add("superdup");
            }
            if (options.zebraFish) {
                checkLabels.add("zebrafish.pheno");
            }

            boolean toDownloadHGNC = false;
            String hgncFileName = "HgncGene.txt";
            File resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);

            if (!resourceFile.exists()) {
                toDownloadHGNC = true;
            } else {
                //half an year later
                long time = resourceFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                Date fileData = new Date(time * 1000);
                Date today = new Date();
                //if too small or too early
                if (resourceFile.length() < 3.2 * 1024 * 1024 || today.after(fileData)) {
                    //an incomplete file
                    resourceFile.delete();
                    toDownloadHGNC = true;
                }
            }

            boolean toDownloadMousePheno = false;
            if (options.phenoMouse) {
                checkLabels.add("impcmouse");
                File fleMouse1 = new File(GlobalManager.RESOURCE_PATH + "/" + "HMD_HumanPhenotype.rpt.txt");
                File fleMouse2 = new File(GlobalManager.RESOURCE_PATH + "/" + "VOC_MammalianPhenotype.rpt.txt");
                if (!fleMouse1.exists() || !fleMouse2.exists()) {
                    toDownloadMousePheno = true;
                } else {
                    if (fleMouse1.exists()) {
                        //one month later
                        long time = fleMouse1.lastModified() / 1000 + 30 * 24 * 60 * 60;
                        Date fileData = new Date(time * 1000);
                        Date today = new Date();
                        //if too small or too early
                        if (fleMouse1.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                            //an incomplete file
                            fleMouse1.delete();
                            toDownloadMousePheno = true;
                        }
                    }
                    if (!toDownloadMousePheno && fleMouse2.exists()) {
                        //one month later
                        long time = fleMouse2.lastModified() / 1000 + 30 * 24 * 60 * 60;
                        Date fileData = new Date(time * 1000);
                        Date today = new Date();
                        //if too small or too early
                        if (fleMouse2.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                            //an incomplete file
                            fleMouse2.delete();
                            toDownloadMousePheno = true;
                        }
                    }
                }
                if (toDownloadMousePheno) {
                    FTP ftp = new FTP("64.147.54.33", 21, "anonymous", "");
                    if (ftp.openFTP()) {
                        ftp.downloadFTP("/pub/reports/HMD_HumanPhenotype.rpt", fleMouse1);
                        ftp.downloadFTP("/pub/reports/VOC_MammalianPhenotype.rpt", fleMouse2);
                    }
                    ftp.closeFTP();
                }
                toDownloadMousePheno = false;

                /*
                //IMPC database
                //ftp://ftp.ebi.ac.uk/pub/databases/impc/release-4.0/csv/ALL_genotype_phenotype.csv.gz.
                fleMouse1 = new File(GlobalManager.RESOURCE_PATH + "/" + "ALL_genotype_phenotype.csv.gz");

                if (!fleMouse1.exists()) {
                    toDownloadMousePheno = true;
                } else if (fleMouse1.exists()) {
                    //one month later
                    long time = fleMouse1.lastModified() / 1000 + 30 * 24 * 60 * 60;
                    Date fileData = new Date(time * 1000);
                    Date today = new Date();
                    //if too small or too early
                    if (fleMouse1.length() < 0.2 * 1024 * 1024 || today.after(fileData)) {
                        //an incomplete file
                        resourceFile.delete();
                        toDownloadMousePheno = true;
                    }
                }
                if (toDownloadMousePheno) {
                    FTP ftp = new FTP("ftp.ebi.ac.uk", 21, "anonymous", "");
                    if (ftp.openFTP()) {
                        ftp.downloadFTP("/pub/databases/impc/release-4.0/csv/ALL_genotype_phenotype.csv.gz", fleMouse1);
                    }
                    ftp.closeFTP();
                }
                 */
<<<<<<< HEAD
            }

            if (options.dddPhenotypes) {
                boolean needDownload = false;
                File rfFile = new File(GlobalManager.RESOURCE_PATH + "/DDG2P.csv.gz");
                if (!rfFile.exists()) {
                    needDownload = true;
                } else {
                    //one month later
                    long time = rfFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                    Date fileData = new Date(time * 1000);
                    Date today = new Date();
                    //if too small or too early
                    if (rfFile.length() < 0.2 * 1024 || today.after(fileData)) {
                        //an incomplete file
                        rfFile.delete();
                        needDownload = true;
                    }
                }
=======
            }

            if (options.dddPhenotypes) {
                boolean needDownload = false;
                File rfFile = new File(GlobalManager.RESOURCE_PATH + "/ddg2p/ddg2p.zip");
                if (!rfFile.exists()) {
                    needDownload = true;
                } else {
                    //one month later
                    long time = rfFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                    Date fileData = new Date(time * 1000);
                    Date today = new Date();
                    //if too small or too early
                    if (rfFile.length() < 0.1 * 1024 * 1024 || today.after(fileData)) {
                        //an incomplete file
                        rfFile.delete();
                        needDownload = true;
                    }
                }

                if (needDownload) {
                    HttpClient4API.simpleRetrieverHttps("https://decipher.sanger.ac.uk/files/ddd/ddg2p.zip", rfFile.getCanonicalPath());
                    Zipper ziper = new Zipper();
                    ziper.extractZip(rfFile.getCanonicalPath(), rfFile.getParentFile().getCanonicalPath() + File.separator);
                }
            }

            //downloading does not support multiple thread 
            String url = "http://www.genenames.org/cgi-bin/hgnc_downloads?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_pub_eg_id&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";
>>>>>>> origin/master

                if (needDownload) {
                    HttpClient4API.simpleRetriever("http://www.ebi.ac.uk/gene2phenotype/downloads/DDG2P.csv.gz", rfFile.getCanonicalPath());
                }
            }

            //downloading does not support multiple thread 
            //String url = "http://www.genenames.org/cgi-bin/hgnc_downloads?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_pub_eg_id&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";
            String url = "http://www.genenames.org/cgi-bin/download?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_pub_eg_id&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";
            if (toDownloadHGNC) {
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);
                String msg1 = "Donwloading HGNC gene annotations ... ";

                File parePath = resourceFile.getParentFile();
                if (!parePath.exists()) {
                    parePath.mkdirs();
                }
                HttpClient4API.simpleRetriever(url, resourceFile.getCanonicalPath());
                toDownload = true;
            }

            if (!checkLabels.isEmpty()) {
                LOG.info("Checking the latest resources ...");
            }

            if (options.flankingSequence > 0) {
                for (int t = 0; t < STAND_CHROM_NAMES.length; t++) {
                    if (STAND_CHROM_NAMES[t].equals("XY") || STAND_CHROM_NAMES[t].equals("Un")) {
                        continue;
                    }
                    File fastAFile = new File(GlobalManager.RESOURCE_PATH + "/" + options.refGenomeVersion + "/chr" + STAND_CHROM_NAMES[t] + ".fa.gz");

                    if (!fastAFile.exists()) {
                        String[] fileURL = new String[2];
                        fileURL[0] = options.refGenomeVersion + "/chr" + STAND_CHROM_NAMES[t] + ".fa.gz";
                        fileURL[1] = "http://hgdownload.cse.ucsc.edu/goldenPath/" + options.refGenomeVersion + "/chromosomes/chr" + STAND_CHROM_NAMES[t] + ".fa.gz";

                        String newLabel = "fasta" + STAND_CHROM_NAMES[t];
                        //remove resources/
                        options.PUBDB_FILE_MAP.put(newLabel, fileURL[0]);
                        options.PUBDB_URL_MAP.put(newLabel, fileURL[1]);
                        checkLabels.add(newLabel);
                    }
                }
            }
            ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
            CompletionService serv = new ExecutorCompletionService(exec);
            int downloadCount = 0;
            for (String dbLabelName : checkLabels) {
                String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + dbFileName);

                if (resourceFile.exists()) {
                    long fileSize = resourceFile.length();

                    long netFileLen = HttpClient4API.getContentLength(options.PUBDB_URL_MAP.get(dbLabelName));
                    if (netFileLen <= 1024 || fileSize == netFileLen) {
                        continue;
                    }
                }
                // System.out.println(dbLabelName);
<<<<<<< HEAD
                if (downloadCount == 0) {
=======
                if (i == 0) {
>>>>>>> origin/master
                    LOG.info("Downloading resources ...");
                }

                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(options.PUBDB_URL_MAP.get(dbLabelName), 20);
                String urlT = options.PUBDB_URL_MAP.get(dbLabelName);
                if (urlT.startsWith("http://grass.cgs.hku.hk/")) {
                    task.setDataMd5(HttpClient4API.getContent(urlT + ".md5"));
                }

                File filePath = new File(GlobalManager.RESOURCE_PATH);
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                task.setLocalPath(resourceFile.getCanonicalPath());
                final String dbLabel = dbLabelName;
                if (dbLabel.startsWith("dbncfp")) {
                    needMergeDncfp = true;
                }
                task.addTaskListener(new DownloadTaskListener() {
                    @Override
                    public void autoCallback(DownloadTaskEvent event) {
                        int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                        String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                        System.out.print(infor);
                        char[] bs = new char[infor.length()];
                        Arrays.fill(bs, '\b');
                        System.out.print(bs);
                    }

                    @Override
                    public void taskCompleted() throws Exception {
                        // File savedFile = new File(task.getLocalPath()); 
                        if (task.getLocalPath().endsWith(".tar")) {
                            File file = new File(task.getLocalPath());
                            Tar.untar(task.getLocalPath(), file.getParent() + File.separator);

                            // file.delete();
                        } else if (task.getLocalPath().endsWith(".zip")) {
                            Zipper ziper = new Zipper();
                            File file = new File(task.getLocalPath());
                            ziper.extractZip(task.getLocalPath(), file.getParent() + File.separator);
                            //  file.delete();
                        }
                        String msg1 = "Resource " + dbLabel + " has been downloaded!";
                        LOG.info(msg1);
                    }
                });
                runningThread++;
                TimeUnit.MILLISECONDS.sleep(500);
                serv.submit(task);
                toDownload = true;

                downloadCount++;
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
            }
            exec.shutdown();

            if (needMergeDncfp) {
                concatenateFiles(GlobalManager.RESOURCE_PATH + refGenomeVersion + "/" + refGenomeVersion + "_", dbncfpAnnotationScore, "known_SNV_dbNCFP.gz");
            }
            if (toDownload) {
                StringBuilder inforString = new StringBuilder();
                inforString.append("The lapsed time for downloading is : ");
                long endTime = System.currentTimeMillis();
                inforString.append((endTime - startTime) / 1000.0);
                inforString.append(" Seconds.\n");
                LOG.info(inforString.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean concatenateFiles(String path, String[] altFiles, String outFile) {
        try {
            FileChannel fclOutput = new FileOutputStream(path + outFile).getChannel();
            for (String strFile : altFiles) {
                FileChannel fclInput = new FileInputStream(path + strFile).getChannel();
                fclInput.transferTo(0, fclInput.size(), fclOutput);
            }
            fclOutput.force(true);
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
