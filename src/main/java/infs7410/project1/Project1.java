package infs7410.project1;
import infs7410.ranking.TF_IDF;
import infs7410.fusion.Fusion_run;
import infs7410.ranking.BM25;
import infs7410.util.topicInfo;
import infs7410.evaluation.evalution;
import org.terrier.matching.models.basicmodel.In;
import org.terrier.structures.Index;
import org.terrier.matching.models.WeightingModel;
import java.util.Timer;
import infs7410.evaluation.stateTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Project1 {
    public static void main(String[] args) throws Exception {
        String dirPath = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/";
        long startTime = System.nanoTime();

//      ====   Training ====
        String path = dirPath + "tar/2017-TAR/training/topics/";
        Double [] coef = {0.25,0.5,0.75,1.0};
        training(path, "bm25", "./train/" + "bm25.res", coef);
        long stopTime = System.nanoTime();
        System.out.println(stopTime - startTime);


//      ===== fusion ====
//        fusion_main();


//      ==== evaluation ===
//      Input: qrels file path, inputfolder, output fodder (with two subfoler "set", "eval" in it)
//      Output: mean of Precision recall map in set folder, each topic of Precision recall map in eval folder
//        String qrels  = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/tar/2017-TAR/testing/qrels/2017-qrel_abs_test.qrels.txt";
//        String inputFolder = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/fusion/";
//        String outputFolder = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/eval/";
//        evalution_set(qrels, inputFolder,outputFolder);

//      ==== Ttest ====
//      input: folder contains eval, output path
//      output: write p value out.
//        String foldername = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/eval/eval/";
//        String outPath = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/eval/stat/fusion.stat";
//        evalution_stat( foldername, outPath);


    }
    public static void training(String path, String RunName, String outName, Double [] coef) throws IOException {
//       ==== Training =====
        Index index = Index.createIndex("./var/index", "pubmed");
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

    public static void fusion_main() throws IOException {
        List<String> resultFilenames = new ArrayList<>();
        List<String> FilenamesList = new ArrayList<>();

        String file1 = "booles.res";
        String file2 = "picoes.res";
        String file3 = "run1.res";
        String file4 = "BM25.res";
        String file5 = "Sheffield1.res";
        String file6 = "Sheffield2.res";
        String file7 = "Sheffield3.res";
        String file8 = "Sheffield4.res";

        String Alg = "combmnz";

        String fusionPath  = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/fusion/";
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file1);
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file2);
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file3);
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file4);
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file5);
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file6);
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file7);
        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/" + file8);

        FilenamesList.add(file1);
        FilenamesList.add(file2);
        FilenamesList.add(file3);
        FilenamesList.add(file4);
        FilenamesList.add(file5);
        FilenamesList.add(file6);
        FilenamesList.add(file7);
        FilenamesList.add(file8);

        String qrels  = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/tar/2017-TAR/testing/qrels/2017-qrel_abs_test.qrels.txt";
        fusion_comb(resultFilenames,FilenamesList,fusionPath, Alg, qrels);
    }

    public static void fusion_comb(List<String> resultFilenamesPath, List<String> FilenamesList,String dirPAhh, String Alg,String qrels ) throws IOException {
//                =======  fusion  ======
//        List<String> resultFilenames = new ArrayList<>();
//        resultFilenames.clear();
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/Test_Data_Sheffield-run-4.res");
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/result_bool_es_test.res");
//        InputStream is = new FileInputStream(resultFilenames.get(0));
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w5/tutorial-5/runs/ECNU_TASK2_RUN1_TFIDF.task2.res");
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w5/tutorial-5/runs/sheffield-general_terms.task2.res");
//        String outputName = "test.res";


//      ==== greedy selection ====
//        String qrels  = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/tar/2017-TAR/testing/qrels/2017-qrel_abs_test.qrels.txt";
        List<String> inputList = new ArrayList<>(resultFilenamesPath);
        List<String> greedyList = new ArrayList<>();
        String outputTmp = "./tmp.res";
        while (!inputList.isEmpty()){
            ArrayList<Double> mapList = new ArrayList<Double>();
            for (String i : inputList){
                List<String> iteList = new ArrayList<>(greedyList);
                iteList.add(i);
                Fusion_run fusion1 = new Fusion_run(iteList);
                fusion1.Fusion_do(Alg,Boolean.FALSE,outputTmp );
                evalution eval = new evalution(qrels,outputTmp);
                Double map = eval.eval_map();
                mapList.add(map);
            }
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

//        System.out.println(greedyList);
        //      ===  do one increment only based on greedy list ===
        inputList.clear();
        StringBuilder outputNameTmp  = new StringBuilder();
        inputList.add(greedyList.get(0));
        outputNameTmp.append(FilenamesList.get(0).substring(0,FilenamesList.get(0).length()-4) + "_");

        for (Integer i=1; i< greedyList.size(); i++){
            inputList.add(greedyList.get(i));
            outputNameTmp.append(FilenamesList.get(i).substring(0,FilenamesList.get(i).length()-4) + "_");
            Fusion_run fusion1 = new Fusion_run(inputList);
            fusion1.Fusion_do(Alg,Boolean.FALSE,dirPAhh + Alg + "_" + outputNameTmp.toString() + ".res" );
        }

    }

    public static void evalution_set(String qrels, String foldername, String outputfolder) throws IOException {
        //        ======= evaluation =====
//        String qrels = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/task1.test.abs.qrels";
//        String res = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/sheffield-bm25.res";
//        String outPath = "./out.eval";
//        String outputEvalset = "./eval/" + "sheffield4_booles" +  "_borda"+".set";
//        String outputEvalq = "./eval/" + "sheffield4_booles" +  "_borda"+".eval";


        File[] files = new File(foldername).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                String inputFile = (file.getName());
//                System.out.println(inputFile);
                evalution eval = new evalution(qrels,file.getAbsolutePath());
                eval.eval_PR_map_udcq(outputfolder +"set/" + inputFile.substring(0,inputFile.length()-4) + ".set");  // Precision recall & map udcg
                eval.eval_q_map_udcg(outputfolder +"eval/" + inputFile.substring(0,inputFile.length()-4) + ".eval");   // every map * udcg for statistical test.
            }
        }


    }

    public static void evalution_stat(String foldername ,String outPath) throws Exception {
        //      ===== statistical test ====
//        List<String> statTest = new ArrayList<>();
//        String eval1 = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/sheffield-bm25.eval";
//        String eval2 = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/ECNU_RUN1_BM25.eval";
//        String eval3 = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/sheffield-boolean.eval";
//        statTest.add(eval1);
//        statTest.add(eval2);
//        statTest.add(eval3);

        List<String>  testList = new ArrayList<>();
        File[] files = new File(foldername).listFiles();
        stateTest tTest = new stateTest();
        for (File file : files) {
            String tmp = file.getName();
            if (!tmp.substring(1,5).equals("DS_S")) {
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
