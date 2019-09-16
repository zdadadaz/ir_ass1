package infs7410.project1;
import infs7410.ranking.TF_IDF;
import infs7410.fusion.Fusion_run;
//import infs7410.ranking.BM25;
import infs7410.util.topicInfo;
import infs7410.query.queryProcess;
import infs7410.evaluation.evalution;
import org.apache.log4j.BasicConfigurator;
import org.terrier.structures.Index;
import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.BM25;

import infs7410.evaluation.stateTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * main function - Run training, testing, evaluation and T-test
 * @author Chien-chi chen
 */
public class Project1 {
    public static void main(String[] args) throws Exception {
//      the path of folder containing runs and tar folders
//         String dirPath = "/home/zdadadaz/Desktop/course/INFS7401/ass1/";
//         String indexPath = "./var/index";
//         String trec_evalPath = "/home/zdadadaz/Desktop/course/INFS7401/trec_eval/trec_eval";

       String dirPath = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/ass1/";
       String indexPath = "./var/index";
       String trec_evalPath = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/trec_eval/trec_eval";
        File file;
        BasicConfigurator.configure();
        /**
         * Choose case and year for training and testing in different years
         * Case: train or test
         * year: 2017 or 2018
         * Query: title or boolean
         * QueryReduction: no or IDF or IDFr or KLI
         * QueryReduction_ks:0 or number of left query or % of left query ex: {0} or {3,5,7} or  {0.85,0.5,0.3}
         * QueryReduction_resPath: path of init retrieved document set for KLI
         */
        String Case = "test";
        String [] years ={"2017"};
        String Query = "title";
        String [] QueryReductions = {"KLI"};
        double[] QueryReduction_ks = {0.85};
//        double[] QueryReduction_ks = {0.3};
        for (String year:years){
            for (String QueryReduction:QueryReductions){
                for (double QueryReduction_k : QueryReduction_ks){
                    /**
                     * Training
                     * input: path: indexin path, outName: out put path name
                     * output: training res
                     */
                    String yearCasefolder = year+Case+Query+"_"+QueryReduction;
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
//        Double [] coefbm25 = {0.45,0.55,0.65,0.75,0.9};
                    Double [] coef = {1.0};
                    Double [] coefbm25 = {0.45};
                     training(indexPath, path, "tfidf", "./"+yearCasefolder+"/" + "tfidf.res", coef, Query,QueryReduction, QueryReduction_k);
                     training(indexPath, path, "bm25", "./"+yearCasefolder+"/" + "bm25.res", coefbm25, Query,QueryReduction, QueryReduction_k);

                    /**
                     * fusion
                     * input: qrels: groundtruth, trainSet: run.res folder, fusionPath:output path
                     * output: result of fusion for three methods.
                     */
//            String trainSet = dirPath + "runs/"+year+"/";
//            String fusionPath  = "./"+yearCasefolder+"/";
//            if (Case.equals("test")){
//                fusion_main(qrels,trainSet,fusionPath,trec_evalPath);
//            }

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
//                    String foldername = "./"+"eval/";
//                    String outPath = "./stat/"+Case+".stat";
                    evalution_stat( foldername, outPath);
                }
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
    public static void training(String indexPath, String path, String RunName, String outName, Double [] coef, String queryType, String QueryReduction, double QueryReduction_k) throws Exception {
        Index index = Index.createIndex(indexPath, "pubmed");
        InputFile Alltopic = new InputFile(path);
        Reranker reranker = new Reranker(index);
        String queryFolder = outName.substring(0,outName.lastIndexOf("/"));

        WeightingModel alg;
        switch(RunName) {
            case "bm25":
                alg = new BM25();
                break;
            default:
                alg = new TF_IDF();
        }

        for (double c: coef)  {
            double k = 1.2;
            System.out.println("Coeficient : " + Double.toString(c));
            System.out.println("Coeficient k : " + Double.toString(k));
            System.out.println("QueryReduction_k : " + Double.toString(QueryReduction_k));
            StringBuilder runNameTmp = new StringBuilder(RunName);
            StringBuilder outNameTmp = new StringBuilder(outName);

            alg.setParameter(c);
            runNameTmp.append("_"+ Double.toString(c)+"_"+ Double.toString(k));
            outNameTmp.delete(outNameTmp.length()-4,outNameTmp.length());
            outNameTmp.append("_"+ Double.toString(c)+"_"+ Double.toString(k)+"_"+ QueryReduction+ Double.toString(QueryReduction_k) + ".res");
            File fdelet = new File(outNameTmp.toString());
            if(fdelet.exists()){
                fdelet.delete();
            }
            for (Integer i=0; i < Alltopic.getFileSize(); i++){
                topicInfo tmpTopic = Alltopic.getOutput(i);
                System.out.println("filename: "+ tmpTopic.getFilename());
                System.out.println("Topic: "+ tmpTopic.getTopic());
                System.out.println("Title: "+ tmpTopic.getTitle());
                System.out.println("Query: "+ tmpTopic.getQuery());
                ArrayList<String> tmpQuery;
                queryProcess qp = new queryProcess(queryFolder,tmpTopic.getPid());

                if (queryType.equals("boolean")){
                    if (qp.HasBooleanQuery(tmpTopic.getTopic())){
                        tmpQuery = qp.GetBooleanQuery(tmpTopic.getTopic());
                    }else{
                        tmpQuery = qp.expandQeury(tmpTopic.getQuery(),QueryReduction_k,QueryReduction);
                    }
                    System.out.println("output query: "+tmpQuery.toString());
                    writeString(tmpQuery,outNameTmp.toString().substring(0,outNameTmp.toString().length()-4)+"_"+tmpTopic.getTopic()+".qr");
                }else{ // title
                    if (!QueryReduction.equals("no")){
                        StringBuilder termlist = new StringBuilder();
                        String idfrQuery;
                        for (String s : tmpTopic.getTitle()) {
                            termlist.append(" " + s);
                        }
                        if (QueryReduction.equals("IDFr")){
                            idfrQuery = qp.runIDFr(termlist.toString(),QueryReduction_k);
                        }else { // KLI
                            idfrQuery = qp.runKLI(termlist.toString(),QueryReduction_k);
                        }
                        String [] outArr = idfrQuery.split(" ");
                        tmpQuery = new ArrayList<String>();
                        for (String s: outArr){
                            tmpQuery.add(s);
                        }
                    }else{
                        tmpQuery = tmpTopic.getTitle();
                    }

                }
                TrecResults results = reranker.rerank(
                        tmpTopic.getTopic(),
//                        tmpTopic.getTitle(),
                        tmpQuery,
                        tmpTopic.getPid(),
                        alg);
                results.setRunName(runNameTmp.toString()); // "example1"
                results.write(outNameTmp.toString()); //"example1.res"
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

        File[] files = new File(trainSet).listFiles();
        for (File file: files){
            if ((file.getName().endsWith(".res") || file.getName().endsWith(".txt")) && !file.getName().substring(0,1).equals(".")){
                resultFilenames.add(trainSet + file.getName());
                FilenamesList.add(file.getName());
            }
        }

//      == choose algorithm ==
        String[] Alg = {"borda","combsum","combmnz"};

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
//            System.out.println(contents);
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

        StringBuilder head = new StringBuilder();
        StringBuilder body = new StringBuilder();
        head.append("name");
        head.append("\t"+"map");
        head.append("\t"+"Rprec");
        head.append("\t"+"ndcg");
        head.append("\n");

        for (File file : files) {
            if (file.isFile()) {
                if(!file.getName().substring(0,1).equals(".") && file.getName().endsWith(".res")){
                    String inputFile = (file.getName());
                    evalution eval = new evalution(qrels,file.getAbsolutePath(),trec_evalPath);
                    StringBuilder tmp = eval.eval_PR_map_udcq(foldername +"set/" + inputFile.substring(0,inputFile.length()-4) + ".set");  // Precision recall & map udcg
                    eval.eval_q_map_udcg(foldername +"eval/" + inputFile.substring(0,inputFile.length()-4) + ".eval");   // every map * udcg for statistical test.
                    body.append(tmp);
                }
            }
        }
        head.append(body);
        evalution.write_append(head,foldername +"set/" + "summary.set");

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
    public static void writeString(ArrayList<String> input,String outPath ) throws IOException {
        OutputStream os = new FileOutputStream(outPath);
        for (String result : input) {
            os.write(String.format("%s\n", result).getBytes());
        }
        os.flush();
        os.close();
    }

}
