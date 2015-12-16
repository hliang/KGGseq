/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import cern.colt.list.DoubleArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;
import org.apache.log4j.Logger;
import org.cobi.kggseq.Constants;
import static org.cobi.kggseq.Constants.STAND_CHROM_NAMES;
import org.cobi.kggseq.GlobalManager;
import static org.cobi.kggseq.GlobalManager.PLUGIN_PATH;
import org.cobi.kggseq.entity.Individual;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.thread.Task;

/**
 *
 * @author mxli
 */
public class RVTest {

    private static final Logger LOG = Logger.getLogger(RVTest.class);
    String rvtestFolder;
    String inputFileFolder;
    String outputFileFolder;
    HashMap<String, double[]> hmpRV;
    int N = 4;
    String strCov;
    String strPhe;

    enum SetTest {
        cmc, kbac, price, skat
    }

    public RVTest(String inputFileFolder) throws Exception {
        this.rvtestFolder = PLUGIN_PATH + "/rvtests-master/executable/rvtest";
        File f1 = new File(inputFileFolder);
        File f = new File(f1.getCanonicalPath());
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdir();
        }
        this.inputFileFolder = f1.getCanonicalPath();
        outputFileFolder = f1.getCanonicalPath() + ".rvtestTMP" + (int) (Math.random() * 10000);
//        outputFileFolder = f1.getCanonicalPath();
    }

    public void setPheno(String phenoName) {
        strPhe = "--pheno-name " + phenoName;
    }

    public void setCov(String pedFile, String[] covItem) {
        if (covItem != null) {
            strCov = " --covar " + pedFile + " --covar-name ";
            for (int i = 0; i < covItem.length; i++) {
                strCov += covItem[i] + ",";
            }
            strCov = strCov.substring(0, strCov.length() - 1);
        }
    }

    public void runBGzip() {
        try {
            String bgizpFolder = PLUGIN_PATH + "/tabix-master/";
            LocalFileFunc.gunzipFile(inputFileFolder + ".flt.vcf.gz", inputFileFolder + ".flt.vcf");
            String line;

            String cmd = bgizpFolder + "bgzip -f " + inputFileFolder + ".flt.vcf";
            Process pr = Runtime.getRuntime().exec(cmd);
            try {
                BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                while (((line = inputError.readLine()) != null)) {
                    //  System.out.println(line);
                }
                int exitVal = pr.waitFor();
                pr.destroy();
                inputError.close();
                if (exitVal != 0) {
                    LOG.info("Failed to run the command:" + cmd);
                }
            } catch (Exception ex) {
                LOG.error(ex + "\nbgzip failed to run.");
            }

            cmd = bgizpFolder + "tabix -f " + inputFileFolder + ".flt.vcf.gz";
            pr = Runtime.getRuntime().exec(cmd);
            try {
                BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                while (((line = inputError.readLine()) != null)) {
                    //  System.out.println(line);
                }
                int exitVal = pr.waitFor();
                pr.destroy();
                inputError.close();
                if (exitVal != 0) {
                    LOG.info("Failed to run the command:" + cmd);
                }
            } catch (Exception ex) {
                LOG.error(ex + "\nFailed to run the command:" + cmd);
            }
        } catch (IOException ex) {
            LOG.error(ex);
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex);
        }

    }

    public void runTabix() {
        try {
            String bgizpFolder = PLUGIN_PATH + "/tabix-master/";
            LocalFileFunc.gunzipFile(inputFileFolder + ".flt.vcf.gz", inputFileFolder + ".flt.vcf");
            String line;

            String cmd = bgizpFolder + "tabix -f " + inputFileFolder + ".flt.vcf.gz";
            Process pr = Runtime.getRuntime().exec(cmd);
            try {
                BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                while (((line = inputError.readLine()) != null)) {
                    //  System.out.println(line);
                }
                int exitVal = pr.waitFor();
                pr.destroy();
                inputError.close();
                if (exitVal != 0) {
                    LOG.info("Failed to run the command:" + cmd);
                }
            } catch (Exception ex) {
                LOG.error(ex + "\nFailed to run the command:" + cmd);
            }
        } catch (IOException ex) {
            LOG.error(ex);
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex);
        }

    }

    public void runBGzip(String inputFileFolder) {
        try {
            if (!inputFileFolder.endsWith(".gz")) {
                return;
            }
            String bgizpFolder = PLUGIN_PATH + "/tabix-master/";
            File bgzipfile = new File(bgizpFolder + "/bgzip");
            if (!bgzipfile.exists()) {
                return;
            }
            int index = inputFileFolder.lastIndexOf('.');
            LocalFileFunc.gunzipFile(inputFileFolder, inputFileFolder.substring(0, index));

            String line;

            String cmd = bgizpFolder + "bgzip -f " + inputFileFolder.substring(0, index);
            Process pr = Runtime.getRuntime().exec(cmd);
            try {
                BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                while (((line = inputError.readLine()) != null)) {
                    //  System.out.println(line);
                }
                int exitVal = pr.waitFor();
                pr.destroy();
                inputError.close();
                if (exitVal != 0) {
                    LOG.info("Failed to run the command:" + cmd);
                }
            } catch (Exception ex) {
                LOG.error(ex + "\nbgzip failed to run.");
            }

            cmd = bgizpFolder + "tabix -f " + inputFileFolder;
            pr = Runtime.getRuntime().exec(cmd);
            try {
                BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                while (((line = inputError.readLine()) != null)) {
                    //  System.out.println(line);
                }
                int exitVal = pr.waitFor();
                pr.destroy();
                inputError.close();
                if (exitVal != 0) {
                    LOG.info("Failed to run the command:" + cmd);
                }
            } catch (Exception ex) {
                LOG.error(ex + "\nFailed to run the command:" + cmd);
            }
        } catch (IOException ex) {
            LOG.error(ex);
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex);
        }

    }

    public void collectResultGene(boolean boolKeep, boolean excel) throws Exception {
        hmpRV = new HashMap<String, double[]>();

        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            String strIn = outputFileFolder + ".chr" + STAND_CHROM_NAMES[i] + ".CMC.assoc";
            File file = new File(strIn);
            if (!file.exists()) {
                continue;
            }
            buildMap(hmpRV, boolKeep, file, SetTest.cmc.ordinal(), 6);
        }

        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            String strIn = outputFileFolder + ".chr" + STAND_CHROM_NAMES[i] + ".Kbac.assoc";
            File file = new File(strIn);
            if (!file.exists()) {
                continue;
            }
            buildMap(hmpRV, boolKeep, file, SetTest.kbac.ordinal(), 5);
        }

        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            String strIn = outputFileFolder + ".chr" + STAND_CHROM_NAMES[i] + ".VariableThresholdPrice.assoc";
            File file = new File(strIn);
            if (!file.exists()) {
                continue;
            }
            buildMap(hmpRV, boolKeep, file, SetTest.price.ordinal(), 13);
        }

        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            String strIn = outputFileFolder + ".chr" + STAND_CHROM_NAMES[i] + ".Skat.assoc";
            File file = new File(strIn);
            if (!file.exists()) {
                continue;
            }
            buildMap(hmpRV, boolKeep, file, SetTest.skat.ordinal(), 6);
        }

        //output the result. 
        File fleOutput = new File(this.inputFileFolder + ".rvtest.gene." + (excel ? "xlsx" : "txt"));
        if (!fleOutput.getParentFile().exists()) {
            fleOutput.getParentFile().mkdir();
        }
        if (!excel) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fleOutput));
            bw.write("Gene\tCMC\tKbac\tVariableThresholdPrice\tSkat\n");
            for (Map.Entry<String, double[]> entry : hmpRV.entrySet()) {
                String strTemp = entry.getKey();
                for (int i = 0; i < N; i++) {
                    strTemp += "\t" + entry.getValue()[i];
                }
                strTemp += "\n";
                bw.write(strTemp);
            }
            bw.close();
        } else {
            List<String[]> arrays = new ArrayList<String[]>();
            String[] titles = new String[]{"Gene", "CMC", "Kbac", "VariableThresholdPrice", "Skat"};

            for (Map.Entry<String, double[]> entry : hmpRV.entrySet()) {
                String strTemp = entry.getKey();
                String[] cells = new String[5];
                cells[0] = strTemp;
                for (int i = 0; i < N; i++) {
                    cells[i + 1] = String.valueOf(entry.getValue()[i]);
                }
                arrays.add(cells);
            }
            LocalExcelFile.writeArray2XLSXFile(fleOutput.getCanonicalPath(), titles, arrays);
        }
        hmpRV.clear();
        String infor = "The association analysis results of gene-base association are saved in " + fleOutput.getCanonicalPath();
        LOG.info(infor);
        
        //remove the temporary products. 
        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            File fleRM = new File(outputFileFolder + ".chr" + STAND_CHROM_NAMES[i] + ".log");
            if (fleRM.exists()) {
                fleRM.delete();
            }
        }

        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            File fleRM = new File(inputFileFolder + ".chr" + STAND_CHROM_NAMES[i] + ".gene.rvtest.grp.gz");
            if (fleRM.exists()) {
                fleRM.delete();
            }
        }
    }

    public void collectResultGeneset(boolean boolKeep, boolean excel) throws Exception {
        hmpRV = new HashMap<String, double[]>();

        String strIn = outputFileFolder + ".set.CMC.assoc";
        File file = new File(strIn);
        if (file.exists()) {
            buildMap(hmpRV, boolKeep, file, SetTest.cmc.ordinal(), 6);
        }
        strIn = outputFileFolder + ".set.Kbac.assoc";
        file = new File(strIn);
        if (file.exists()) {
            buildMap(hmpRV, boolKeep, file, SetTest.kbac.ordinal(), 5);
        }

        strIn = outputFileFolder + ".set.VariableThresholdPrice.assoc";
        file = new File(strIn);
        if (file.exists()) {
            buildMap(hmpRV, boolKeep, file, SetTest.price.ordinal(), 13);
        }
        strIn = outputFileFolder + ".set.Skat.assoc";
        file = new File(strIn);
        if (file.exists()) {
            buildMap(hmpRV, boolKeep, file, SetTest.skat.ordinal(), 6);
        }

        //output the result. 
        File fleOutput = new File(this.inputFileFolder + ".rvtest.geneset." + (excel ? "xlsx" : "txt"));
        if (!fleOutput.getParentFile().exists()) {
            fleOutput.getParentFile().mkdir();
        }
        if (!excel) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fleOutput));
            bw.write("Set\tCMC\tKbac\tVariableThresholdPrice\tSkat\n");
            for (Map.Entry<String, double[]> entry : hmpRV.entrySet()) {
                String strTemp = entry.getKey();
                for (int i = 0; i < N; i++) {
                    strTemp += "\t" + (entry.getValue()[i] == -1 ? "." : entry.getValue()[i]);
                }
                strTemp += "\n";
                bw.write(strTemp);
            }
            bw.close();
        } else {
            List<String[]> arrays = new ArrayList<String[]>();
            String[] titles = new String[]{"Set", "CMC", "Kbac", "VariableThresholdPrice", "Skat"};

            for (Map.Entry<String, double[]> entry : hmpRV.entrySet()) {
                String strTemp = entry.getKey();
                String[] cells = new String[5];
                cells[0] = strTemp;
                for (int i = 0; i < N; i++) {
                    cells[i + 1] = (entry.getValue()[i] == -1 ? "." : String.valueOf(entry.getValue()[i]));
                }
                arrays.add(cells);
            }
            LocalExcelFile.writeArray2XLSXFile(fleOutput.getCanonicalPath(), titles, arrays);
        }
        hmpRV.clear();
        String infor = "The association analysis results of geneset-base association are saved in " + fleOutput.getCanonicalPath();
        LOG.info(infor);
        
        //remove the temporary products. 
        File fleRM = new File(outputFileFolder + ".set.log");
        if (fleRM.exists()) {
            fleRM.delete();
        }

        fleRM = new File(inputFileFolder + ".geneset.rvtest.grp.gz");
        if (fleRM.exists()) {
            fleRM.delete();
        }
    }

    public void buildMap(HashMap<String, double[]> hmpRV, boolean boolKeep, File fleInput, int intP1, int intP2) throws FileNotFoundException, IOException {
        if (!fleInput.exists()) {
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(fleInput));
        String strLine = br.readLine();
        while ((strLine = br.readLine()) != null) {
            String strItem[] = strLine.split("\t");
            if (hmpRV.containsKey(strItem[0])) {
                try {
                    hmpRV.get(strItem[0])[intP1] = Double.valueOf(strItem[intP2]);
                } catch (Exception e) {
                    hmpRV.get(strItem[0])[intP1] = -1;
                }
            } else {
                double[] intItem = new double[4];
                Arrays.fill(intItem, 0.0);
                try {
                    intItem[intP1] = Double.valueOf(strItem[intP2]);
                } catch (Exception e) {
                    intItem[intP1] = -1;
                }
                hmpRV.put(strItem[0], intItem);
            }
        }
        br.close();
        if (!boolKeep) {
            fleInput.delete();
        }
        return;
    }

    public class CallRVTestTask extends Task implements Callable<String>, Constants {

        String phenoFilePath;
        String chrName;

        public CallRVTestTask(String phenoFilePath, String chrName) {
            this.phenoFilePath = phenoFilePath;
            this.chrName = chrName;
        }

        @Override
        public String call() throws Exception {
            try {
                File f1 = new File(phenoFilePath);
                phenoFilePath = f1.getCanonicalPath();
                String[] params = new String[15];
                params[0] = rvtestFolder;
                params[1] = " --inVcf";
                params[2] = inputFileFolder + ".flt.vcf.gz";
                params[3] = "--pheno";
                params[4] = phenoFilePath;
                params[5] = "--setFile";
                params[6] = inputFileFolder + ".chr" + chrName + ".gene.rvtest.grp.gz";
                params[7] = "--out";
                params[8] = outputFileFolder + ".chr" + chrName;
                params[9] = "--burden";
                params[10] = "cmc";
                params[11] = "--vt";
                params[12] = "price";
                params[13] = "--kernel";
                params[14] = "skat[nPerm=1000:alpha=0.001:beta1=1:beta2=20],kbac";
                StringBuilder comInfor = new StringBuilder();
                for (String param : params) {
                    comInfor.append(param);
                    comInfor.append(" ");
                }
                if (strPhe != null) {
                    comInfor.append(strPhe);
                    comInfor.append(" ");
                }
                if (strCov != null) {
                    comInfor.append(strCov);
                }
                //example command
                //plugin/rvtests/executable/rvtest --inVcf test1.flt.vcf.gz --pheno assoc.ped --out output --setFile test1.gene.rvtest.grp.gz --burden cmc --vt price --kernel skat[nPerm=100:alpha=0.001:beta1=1:beta2=20],kbac
                //System.out.println(comInfor.toString());
                Process pr = Runtime.getRuntime().exec(comInfor.toString());

                String line;
                StringBuilder errorMsg = new StringBuilder();
                try {
                    BufferedReader inputOut = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    while (((line = inputOut.readLine()) != null)) {
                        //  System.out.println(line);
                    }

                    int exitVal;
                    try (BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()))) {
                        while (((line = inputError.readLine()) != null)) {
                            errorMsg.append(line);
                            errorMsg.append("\n");
                        }
                        exitVal = pr.waitFor();
                        pr.destroy();
                    }

                    if (exitVal != 0) {
                        LOG.info("Rvtest failed to run by the command: " + comInfor.toString() + "\n" + errorMsg);
                    }
                } catch (Exception ex) {
                    LOG.error(ex + "\nRvtest failed to run!");
                }

            } catch (IOException ex) {
                LOG.error(ex);
            } catch (Exception ex) {
                LOG.error(ex);
            }
            return "";
        }

    }

    public void runGeneAssoc(String phenoFilePath, int maxThreadNum) throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(maxThreadNum);
        final CompletionService<String> serv = new ExecutorCompletionService<String>(exec);
        int runningThread = 0;
        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            File f = new File(inputFileFolder + ".chr" + STAND_CHROM_NAMES[i] + ".gene.rvtest.grp.gz");

            if (!f.exists()) {
                continue;
            }

            CallRVTestTask task = new CallRVTestTask(phenoFilePath, STAND_CHROM_NAMES[i]);
            serv.submit(task);
            runningThread++;
        }
        for (int s = 0; s < runningThread; s++) {
            Future<String> task = serv.take();
            String infor = task.get();
            //  System.out.println(infor);
        }
        exec.shutdown();
        //collect data
    }

    public void generateGenesetAssocGroup(Map<String, List<Variant>> geneVars, BufferedWriter bw) {
        String chrName;
        String lb;
        try {
            for (Map.Entry<String, List<Variant>> gVars : geneVars.entrySet()) {
                List<Variant> snps = gVars.getValue();
                int size = snps.size();
                if (size == 0) {
                    continue;
                }
                bw.write(gVars.getKey());

                bw.write(" ");
                //bw.write(snps.get(0).label);
                chrName = STAND_CHROM_NAMES[snps.get(0).chrID];
                lb = chrName + ":" + snps.get(0).refStartPosition + "-" + (snps.get(0).refStartPosition);
                bw.write(lb);
                for (int i = 1; i < size; i++) {
                    bw.write(",");
                    chrName = STAND_CHROM_NAMES[snps.get(i).chrID];
                    lb = chrName + ":" + snps.get(i).refStartPosition + "-" + (snps.get(i).refStartPosition);
                    bw.write(lb);
                }
                bw.write("\n");
            }
        } catch (Exception ex) {
            LOG.error(ex);
        }

    }

    public void runGenesetAssoc(String phenoFilePath, File groupFile) throws Exception {

        File f1 = new File(phenoFilePath);
        phenoFilePath = f1.getCanonicalPath();
        String[] params = new String[15];
        params[0] = rvtestFolder;
        params[1] = " --inVcf";
        params[2] = inputFileFolder + ".flt.vcf.gz";
        params[3] = "--pheno";
        params[4] = phenoFilePath;
        params[5] = "--setFile";
        params[6] = groupFile.getCanonicalPath();
        params[7] = "--out";
        params[8] = outputFileFolder + ".set";
        params[9] = "--burden";
        params[10] = "cmc";
        params[11] = "--vt";
        params[12] = "price";
        params[13] = "--kernel";
        params[14] = "skat[nPerm=1000:alpha=0.001:beta1=1:beta2=20],kbac";
        StringBuilder comInfor = new StringBuilder();
        for (String param : params) {
            comInfor.append(param);
            comInfor.append(" ");
        }
        if (strPhe != null) {
            comInfor.append(strPhe);
            comInfor.append(" ");
        }
        if (strCov != null) {
            comInfor.append(strCov);
        }
        //example command
        //plugin/rvtests/executable/rvtest --inVcf test1.flt.vcf.gz --pheno assoc.ped --out output --setFile test1.gene.rvtest.grp.gz --burden cmc --vt price --kernel skat[nPerm=100:alpha=0.001:beta1=1:beta2=20],kbac
        //System.out.println(comInfor.toString());
        Process pr = Runtime.getRuntime().exec(comInfor.toString());

        String line;
        StringBuilder errorMsg = new StringBuilder();
        try {
            BufferedReader inputOut = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            while (((line = inputOut.readLine()) != null)) {
                //  System.out.println(line);
            }

            int exitVal;
            try (BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()))) {
                while (((line = inputError.readLine()) != null)) {
                    errorMsg.append(line);
                    errorMsg.append("\n");
                }
                exitVal = pr.waitFor();
                pr.destroy();
            }

            if (exitVal != 0) {
                LOG.info("Rvtest failed to run by the command: " + comInfor.toString() + "\n" + errorMsg);
            }
        } catch (Exception ex) {
            LOG.error(ex + "\nRvtest failed to run!");
        }
        //collect data
    }

    public void summarizeVarCountsBySubject(Map<String, List<Variant>> geneVars, List<Individual> subjectList, int[] pedEncodeGytIDMap, boolean isPhasedGty,
            Map<String, Integer> phenotypeColID, String exportPath, boolean outGZ) throws Exception {
        BufferedWriter bwPed = null;
        if (geneVars == null) {
            return;
        }
        if (outGZ) {
            bwPed = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(exportPath + ".gz"))));
        } else {
            bwPed = new BufferedWriter(new FileWriter(exportPath));
        }

        int[] gtys = null;
        int alleleNum, base = 2;
        int subID = -1;
        int gtyID = 0;
        int count;
        bwPed.write("SubjectID");
        String[] traitNames = new String[phenotypeColID.size()];
        for (Map.Entry<String, Integer> items : phenotypeColID.entrySet()) {
            traitNames[items.getValue()] = items.getKey();
        }
        for (int i = 0; i < traitNames.length; i++) {
            bwPed.write("\t");
            bwPed.write(traitNames[i]);
        }
        for (Map.Entry<String, List<Variant>> items : geneVars.entrySet()) {
            bwPed.write("\t");
            bwPed.write(items.getKey());
        }
        bwPed.write("\n");
        for (Individual indiv : subjectList) {
            if (indiv == null) {
                continue;
            }

            subID++;
            gtyID = pedEncodeGytIDMap[subID];
            if (gtyID < 0) {
                continue;
            }

            bwPed.write(indiv.getLabelInChip());
            double[] traits = indiv.getTraits();
            for (int i = 0; i < traits.length; i++) {
                bwPed.write("\t");
                bwPed.write(String.valueOf(traits[i]));
            }

            for (Map.Entry<String, List<Variant>> items : geneVars.entrySet()) {
                bwPed.write("\t");
                List<Variant> vars = items.getValue();
                if (vars == null || vars.isEmpty()) {
                    bwPed.write("0");
                    continue;
                }
                count = 0;
                for (Variant var : vars) {
                    alleleNum = var.getAltAlleles().length + 1;
                    if (isPhasedGty) {
                        base = GlobalManager.phasedAlleleBitMap.get(alleleNum);
                        gtys = BinaryGtyProcessor.getPhasedGtyAt(var.encodedGty, alleleNum, base, gtyID);
                    } else {
                        base = GlobalManager.unphasedAlleleBitMap.get(alleleNum);
                        gtys = BinaryGtyProcessor.getUnphasedGtyAt(var.encodedGty, alleleNum, base, gtyID);
                    }

                    if (gtys != null) {
                        if (gtys[0] != 0) {
                            count++;
                        }
                        if (gtys[1] != 0) {
                            count++;
                        }
                    }
                }
                bwPed.write(String.valueOf(count));
            }
            bwPed.write("\n");
        }
        bwPed.close();
    }
}
