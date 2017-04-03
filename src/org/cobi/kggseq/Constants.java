/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq;

/**
 *
 * @author MX Li
 */
public interface Constants {

    String PVERSION = "1.0";        // 3 chars
    String PREL = "KGGSeq";               // space or p (full, or prelease)
    String PDATE = "16/Dec./2016"; // 11 chars
    final static String[] STAND_CHROM_NAMES = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
        "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "XY", "M", "Un"};
    String KGGSeq_URL = "http://grass.cgs.hku.hk/limx/kggseq/";
    public static String[] VAR_FEATURE_NAMES = new String[]{"frameshift", "nonframeshift", "startloss", "stoploss", "stopgain", "splicing", "missense", "synonymous", "exonic", "5UTR", "3UTR", "intronic", "upstream", "downstream", "ncRNA", "intergenic", "monomorphic", "unknown"};
    final static String[] ReguPredicNames = {"GWAVA_region_score", "GWAVA_TSS_score", "GWAVA_unmatched_score", "CADD_cscore", "DANN_score", "Fathmm_MKL_score", "FunSeq_score", "FunSeq2_score", "GWAS3D_score", "SuRFR_score"};

    //"UserManual.pdf"
    static String[] LOCAL_FILE_PATHES = {"kggseq.jar"};
    static String[] URL_FILE_PATHES = {"autodownload.php?file=kggseq.jar&ver=10"};
    //static String[] URL_FILE_PATHES = {"download/lib/v10/kggseq.jar"};

}
