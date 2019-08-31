package infs7410.project1;
import infs7410.ranking.TF_IDF;
import infs7410.fusion.Fusion_run;
import infs7410.ranking.BM25;
import infs7410.util.topicInfo;
import infs7410.evaluation.evalution;
import org.apache.log4j.BasicConfigurator;
import org.terrier.structures.Index;
import org.terrier.matching.models.WeightingModel;
import org.apache.commons.io.FileUtils;

import infs7410.evaluation.stateTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.terrier.terms.Stopwords;
import org.terrier.terms.PorterStemmer;

/**
 * main function - Run training, testing, evaluation and T-test
 * @author Chien-chi chen
 */
public class Project1 {
    public static void main(String[] args) throws Exception {
//      the path of folder containing runs and tar folders
        String dirPath = "/home/zdadadaz/Desktop/course/INFS7401/ass1/";
        String indexPath = "./var/index";
        String trec_evalPath = "/home/zdadadaz/Desktop/course/INFS7401/trec_eval/trec_eval";
        File file;
        BasicConfigurator.configure();
        /**
         * Choose case and year for training and testing in different years
         * Case: train or test
         * year: 2017 or 2018
         */
        String Case = "train";
        String year ="2017";

        /**
         * Training
         * input: path: indexin path, outName: out put path name
         * output: training res
         */
        String yearCasefolder = year+Case;
        file = new File("./" + yearCasefolder +"/");
        if(!file.exists()){
            file.mkdirs();
        }
        File[] files = new File(dirPath + "tar/"+year+"-TAR/"+Case+"ing/qrels/").listFiles();
        String qrels = "";
        for (File f : files){
            if (!f.getName().substring(0,1).equals(".")){
                qrels = f.getAbsolutePath();
            }
        }
        if (qrels.equals("")){
            throw new RuntimeException("Qrels is not exist");
        }

        String path = dirPath + "tar/"+year+"-TAR/"+ Case + "ing/topics/";
        Double [] coefbm25 = {0.35,0.45,0.55,0.65,0.75,0.85,0.9};
        Double [] kcoefbm25 = {0.5,0.7,0.9,1.1,1.2,1.3,1.5,1.7,1.9};
        //Double [] coef = {1.0};
//        training(indexPath, path, "tfidf", "./"+yearCasefolder+"/" + "tfidf.res", coef);
//        training25(indexPath, path, "bm25", "./"+yearCasefolder+"/" + "bm25.res", coefbm25,kcoefbm25);

       /**
         * fusion
         * input: qrels: groundtruth, trainSet: run.res folder, fusionPath:output path
         * output: result of fusion for three methods.
         */
        String trainSet = dirPath + "runs/"+year+"/";
        String fusionPath  = "./"+yearCasefolder+"/";
        if (Case.equals("test")){
            fusion_main(qrels,trainSet,fusionPath,trec_evalPath);
        }

        /**
         * evaluation for map and udcg
         * Input: qrels file path, inputfolder, output fodder (with two subfoler "set", "eval" in it)
         * Output: mean of Precision recall map in set folder, each topic of Precision recall map in eval folder
         */
        String inputFolder = "./"+yearCasefolder+"/";
        evalution_set(qrels, inputFolder, trec_evalPath);

       /**
         * T-test
         * input: folder contains eval, output path
         * output: write p value out.
         */
        file = new File("./"+yearCasefolder+"/stat");
        if(!file.exists()){
            file.mkdirs();
        }
        String foldername = "./"+yearCasefolder+"/eval/";
        String outPath = "./"+yearCasefolder+"/stat/"+Case+".stat";
        evalution_stat( foldername, outPath);

    }
    /**
     * Training Bm25 algorithm
     *
     * @param path Indexing path
     * @param RunName Run Name
     * @param outName output result name
     * @param coef array of adjust coeficient if exist
     * @require {@code path != null,RunName != null,outName != null, coef != null}
     */
    public static void training(String indexPath, String path, String RunName, String outName, Double [] coef) throws IOException {
        Index index = Index.createIndex(indexPath, "pubmed");
        InputFile Alltopic = new InputFile(path);
        Reranker reranker = new Reranker(index);

        WeightingModel alg;
        switch(RunName) {
            case "bm25":
                alg = new BM25();
                break;
            default:
                alg = new TF_IDF();
        }

        for (double c: coef)  {
            System.out.println("Coeficient : " + Double.toString(c));

            StringBuilder runNameTmp = new StringBuilder(RunName);
            StringBuilder outNameTmp = new StringBuilder(outName);
            if (RunName.equals("bm25")){
                alg.setParameter(c);

                runNameTmp.append("_"+ Double.toString(c));
                outNameTmp.delete(outNameTmp.length()-4,outNameTmp.length());
                outNameTmp.append("_"+ Double.toString(c) + ".res");
            }
            for (Integer i=0; i < Alltopic.getFileSize(); i++){
                topicInfo tmpTopic = Alltopic.getOutput(i);
                System.out.println("filename: "+ tmpTopic.getFilename());
                System.out.println("Topic: "+ tmpTopic.getTopic());
                System.out.println("Title: "+ tmpTopic.getTitle());
                TrecResults results = reranker.rerank(
                        tmpTopic.getTopic(),
                        tmpTopic.getTitle(),
                        tmpTopic.getPid(),
                        alg);
                results.setRunName(runNameTmp.toString()); // "example1"
                results.write(outNameTmp.toString()); //"example1.res"
            }
        }

    }
    /**
     * Training Bm25 algorithm
     *
     * @param path Indexing path
     * @param RunName Run Name
     * @param outName output result name
     * @param coef array of adjust coeficient if exist
     * @require {@code path != null,RunName != null,outName != null, coef != null}
     */
    public static void training25(String indexPath, String path, String RunName, String outName, Double [] coef,Double [] coefk) throws IOException {
        Index index = Index.createIndex(indexPath, "pubmed");
        InputFile Alltopic = new InputFile(path);
        Reranker reranker = new Reranker(index);

        BM25 alg = new BM25();

        for (double c: coef)  {
            for (double k:coefk){
                System.out.println("Coeficient : " + Double.toString(c));
                System.out.println("Coeficient k : " + Double.toString(k));

                StringBuilder runNameTmp = new StringBuilder(RunName);
                StringBuilder outNameTmp = new StringBuilder(outName);

                alg.setParameter(c);
                alg.setParameter2(k);
                runNameTmp.append("_"+ Double.toString(c)+"_"+ Double.toString(k));
                outNameTmp.delete(outNameTmp.length()-4,outNameTmp.length());
                outNameTmp.append("_"+ Double.toString(c)+"_"+ Double.toString(k) + ".res");

               for (Integer i=0; i < Alltopic.getFileSize(); i++){
                    topicInfo tmpTopic = Alltopic.getOutput(i);
                    System.out.println("filename: "+ tmpTopic.getFilename());
                    System.out.println("Topic: "+ tmpTopic.getTopic());
                    System.out.println("Title: "+ tmpTopic.getTitle());
                    TrecResults results = reranker.rerank(
                            tmpTopic.getTopic(),
                            tmpTopic.getTitle(),
                            tmpTopic.getPid(),
                            alg);
                    results.setRunName(runNameTmp.toString()); // "example1"
                    results.write(outNameTmp.toString()); //"example1.res"
                }
            }

        }

    }
    /**
     * Training fusion algorithm initialization
     *
     * @param qrels The file path of ground truth
     * @param trainSet Traing set folder path
     * @require {@code qrels != null,trainSet != null}
     */
    public static void fusion_main(String qrels, String trainSet, String fusionPath, String trec_evalPath) throws IOException {
        List<String> resultFilenames = new ArrayList<>();
        List<String> FilenamesList = new ArrayList<>();

//        String file1 = "booles.res";
//        String file2 = "picoes.res";
//        String file3 = "run1.res";
//        String file4 = "BM25.res";
//        String file5 = "Sheffield1.res";
//        String file6 = "Sheffield2.res";
//        String file7 = "Sheffield3.res";
//        String file8 = "Sheffield4.res";

        File[] files = new File(trainSet).listFiles();
        for (File file: files){
            if (file.getName().endsWith(".res") && !file.getName().substring(0,1).equals(".")){
                resultFilenames.add(trainSet + file.getName());
                FilenamesList.add(file.getName());
            }
        }


//      == choose algorithm ==
        String[] Alg = {"borda","combsum","combmnz"};

//      == assign input filename
//        resultFilenames.add(trainSet + file1);
//        resultFilenames.add(trainSet + file2);
//        resultFilenames.add(trainSet + file3);
//        resultFilenames.add(trainSet + file4);
//        resultFilenames.add(trainSet + file5);
//        resultFilenames.add(trainSet + file6);
//        resultFilenames.add(trainSet + file7);
//        resultFilenames.add(trainSet + file8);
//
//        FilenamesList.add(file1);
//        FilenamesList.add(file2);
//        FilenamesList.add(file3);
//        FilenamesList.add(file4);
//        FilenamesList.add(file5);
//        FilenamesList.add(file6);
//        FilenamesList.add(file7);
//        FilenamesList.add(file8);

        File file = new File(fusionPath);
        if(!file.exists()){
            file.mkdirs();
        }

        for (String a:Alg){
            fusion_comb(resultFilenames,FilenamesList,fusionPath, a, qrels, trec_evalPath);
        }

    }
    /**
     * Training fusion algorithm with greedy selection
     *
     * @param resultFilenamesPath The file path of prepared for fusion
     * @param FilenamesList The file name of prepared for fusion
     * @param dirPAhh output diretory path
     * @param Alg fusion algorithm
     * @param qrels The file path of ground truth
     * @require {@code resultFilenamesPath != null,FilenamesList != null,dirPAhh != null,Alg != null, qrels != null}
     */
    public static void fusion_comb(List<String> resultFilenamesPath, List<String> FilenamesList,String dirPAhh, String Alg,String qrels, String trec_evalPath ) throws IOException {
//      ==== greedy selection ====
        List<String> inputList = new ArrayList<>(resultFilenamesPath);
        List<String> greedyList = new ArrayList<>();
        String outputTmp = "./tmp.res";
        while (!inputList.isEmpty()){
            ArrayList<Double> mapList = new ArrayList<Double>();
            for (String i : inputList){
                List<String> iteList = new ArrayList<>(greedyList);
                iteList.add(i);
                Fusion_run fusion1 = new Fusion_run(iteList);
                fusion1.Fusion_do(Alg,Boolean.TRUE,outputTmp );
                evalution eval = new evalution(qrels,outputTmp,trec_evalPath);
                Double map = eval.eval_map();
                mapList.add(map);
            }
            System.out.println("Greedy array:");
            System.out.println(mapList);

            Double maxV = mapList.get(0);
            Integer index = 0;
            for (Integer j = 1; j< mapList.size(); j++){
                if(mapList.get(j) > maxV){
                    maxV = mapList.get(j);
                    index = j;
                }
            }
            greedyList.add(inputList.get(index));
            inputList.remove(index.intValue());

        }

        //      ===  do one increment only based on greedy list ===
        inputList.clear();
        StringBuilder outputNameTmp  = new StringBuilder();
        inputList.add(greedyList.get(0));
        outputNameTmp.append(FilenamesList.get(0).substring(0,FilenamesList.get(0).length()-4) + "_");

        for (Integer i=1; i< greedyList.size(); i++){
            inputList.add(greedyList.get(i));
            outputNameTmp.append(FilenamesList.get(i).substring(0,FilenamesList.get(i).length()-4) + "_");
            Fusion_run fusion1 = new Fusion_run(inputList);
            fusion1.Fusion_do(Alg,Boolean.TRUE,dirPAhh + Alg + "_" + outputNameTmp.toString() + ".res" );
        }

    }
    /**
     * Evaluation for precision/recall, map, udcg
     *
     * @param qrels The file path of ground truth
     * @param foldername folder prepare for evaluation
     * @require {@code qrels != null,foldername != null,outputfolder != null}
     */
    public static void evalution_set(String qrels, String foldername, String trec_evalPath) throws IOException {
        File filec1 = new File(foldername +"set/");
        if(filec1.exists()){
            File[] contents = filec1.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    f.delete();
                }
            }
        }
        File filec = new File(foldername +"eval/");
        if(filec.exists()){
            File[] contents = filec.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    f.delete();
                }
            }
            filec.delete();
        }
        filec.mkdirs();
        filec1.mkdirs();

        File[] files = new File(foldername).listFiles();

//        String testfile = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/train/tfidf.res";
//        String inputFile = stateTest.extractfilename(testfile);
//        evalution eval = new evalution(qrels,testfile);
//        eval.eval_PR_map_udcq(foldername.toString() +"set/" + inputFile.substring(0,inputFile.length()-4) + ".set");  // Precision recall & map udcg
//        eval.eval_q_map_udcg(foldername.toString() +"eval/" + inputFile.substring(0,inputFile.length()-4) + ".eval");   // every map * udcg for statistical test.

        for (File file : files) {
            if (file.isFile()) {
                if(!file.getName().substring(0,1).equals(".") && file.getName().endsWith(".res")){
                    String inputFile = (file.getName());
                    evalution eval = new evalution(qrels,file.getAbsolutePath(),trec_evalPath);
                    eval.eval_PR_map_udcq(foldername +"set/" + inputFile.substring(0,inputFile.length()-4) + ".set");  // Precision recall & map udcg
                    eval.eval_q_map_udcg(foldername +"eval/" + inputFile.substring(0,inputFile.length()-4) + ".eval");   // every map * udcg for statistical test.
                }
            }
        }


    }
    /**
     * Evaluation for T-test
     *
     * @param foldername The folder path for T-test
     * @param outPath output path for T-test
     * @require {@code foldername != null,outPath != null}
     */
    public static void evalution_stat(String foldername ,String outPath) throws Exception {
        List<String>  testList = new ArrayList<>();
        File[] files = new File(foldername).listFiles();
        stateTest tTest = new stateTest();
        for (File file : files) {
            String tmp = file.getName();
            if (!tmp.substring(0,1).equals(".")) {
                //System.out.println(file.getAbsolutePath());
                //System.out.println(file.getName());
                testList.add(file.getAbsolutePath());
            }
        }
        HashMap<String, double[]> statall = new HashMap<>();
        for (int i =0; i<testList.size();i++) {
            for (int j =i+1; j<testList.size();j++) {
                double [] pval_tmp = tTest.statistical_test(testList.get(i),testList.get(j));
                statall.put(testList.get(i) + " " + testList.get(j), pval_tmp);
            }
        }
        tTest.writeHash(testList, statall, outPath);
    }

}
