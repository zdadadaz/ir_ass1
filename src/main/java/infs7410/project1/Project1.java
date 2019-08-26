package infs7410.project1;
import infs7410.ranking.TF_IDF;
import infs7410.fusion.Fusion_run;
import infs7410.ranking.BM25;
import infs7410.util.topicInfo;
import infs7410.evaluation.evalution;
import org.terrier.structures.Index;
import org.terrier.matching.models.WeightingModel;
import java.util.Timer;

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
//        String path = dirPath + "tar/2017-TAR/training/topics/";
//        training(path, "tfidf", "./train/" + "tfidf.res");
//        long stopTime = System.nanoTime();
//        System.out.println(stopTime - startTime);


//      ===== fusion ====
//        fusion_main();


//      ==== evaluation ===
        //        String qrels  = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/tar/2017-TAR/testing/qrels/2017-qrel_abs_test.qrels.txt";
//        String inputFolder = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/fusion/";
//        String outputFolder = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/project/eval/";
//       evalution_set(qrels, inputFolder,outputFolder);

    }
    public static void training(String path, String RunName, String outName) throws IOException {
//       ==== Training =====
        Index index = Index.createIndex("./var/index", "pubmed");
        InputFile Alltopic = new InputFile(path);
        Reranker reranker = new Reranker(index);

        WeightingModel alg;
        switch(RunName) {
            case "bm25":
                alg = new BM25();
//                alg.setParameter();
                break;
            default:
                alg = new TF_IDF();
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
            results.setRunName(RunName); // "example1"
            results.write(outName); //"example1.res"
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

        String Alg = "borda";

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


        String outputFuse = "./fusion/" + "sheffield4_booles" + "_borda"+".res";
        fusion_comb(resultFilenames,FilenamesList,fusionPath,outputFuse,Alg);

    }

    public static void fusion_comb(List<String> resultFilenamesPath, List<String> FilenamesList,String dirPAhh, String outputName, String Alg ) throws IOException {
//                =======  fusion  ======
//        List<String> resultFilenames = new ArrayList<>();
//        resultFilenames.clear();
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/Test_Data_Sheffield-run-4.res");
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/runs/2017/result_bool_es_test.res");
//        InputStream is = new FileInputStream(resultFilenames.get(0));
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w5/tutorial-5/runs/ECNU_TASK2_RUN1_TFIDF.task2.res");
//        resultFilenames.add("/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w5/tutorial-5/runs/sheffield-general_terms.task2.res");
//        String outputName = "test.res";


//        ==  try all combination ==
//        for (Integer i=0; i< Math.pow(2,resultFilenamesPath.size()); i++){
//            List<String> inputList = new ArrayList<>();
//            StringBuilder outputNameTmp  = new StringBuilder();
//            Integer count = 0;
//            Integer tmp = i;
//            Integer acc = 0;
//            while (tmp !=0 ){
//                if ((tmp & 1) == 1){
//                    inputList.add(resultFilenamesPath.get(acc));
//                    outputNameTmp.append(FilenamesList.get(acc).substring(0,FilenamesList.get(acc).length()-4) + "_");
//                    count += 1;
//                }
//                tmp = (tmp>>1);
//                acc += 1;
//            }
//            System.out.println("============= count : ="+ count);
//            if (count >1){
//                Fusion_run fusion1 = new Fusion_run(inputList);
//                fusion1.Fusion_do(Alg,Boolean.FALSE,dirPAhh + Alg + "_" + outputNameTmp.toString() + ".res" );
//            }
//        }

//      ===  do one increment only ===
        List<String> inputList = new ArrayList<>();
        StringBuilder outputNameTmp  = new StringBuilder();
        inputList.add(resultFilenamesPath.get(0));
        outputNameTmp.append(FilenamesList.get(0).substring(0,FilenamesList.get(0).length()-4) + "_");

        for (Integer i=1; i< resultFilenamesPath.size(); i++){
            inputList.add(resultFilenamesPath.get(i));
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

    public static void evalution_stat(evalution eval, List<String>  statTest,String outPath) throws Exception {
        //      ===== statistical test ====
//        List<String> statTest = new ArrayList<>();
//        String eval1 = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/sheffield-bm25.eval";
//        String eval2 = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/ECNU_RUN1_BM25.eval";
//        String eval3 = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/w2/tutorial-2/exercise-1+2/sheffield-boolean.eval";
//        statTest.add(eval1);
//        statTest.add(eval2);
//        statTest.add(eval3);
        HashMap<String, double[]> statall = new HashMap<>();
        for (int i =0; i<statTest.size();i++) {
            for (int j =i+1; j<statTest.size();j++) {
                double [] pval_tmp = eval.statistical_test(statTest.get(i),statTest.get(j));
                statall.put(Integer.toString(i) + "_" + Integer.toString(j), pval_tmp);
            }
        }
        eval.writeHash(statTest, statall,outPath);
    }

}
