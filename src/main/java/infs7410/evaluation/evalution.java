package infs7410.evaluation;

import infs7410.evaluation.TrecEvaluation;
import java.io.PrintWriter;
import org.apache.commons.math3.stat.inference.TTest;
import java.io.*;
import java.util.HashMap;
import java.util.List;

public class evalution {
    private String qrels;
    private String res;
    private String trec_eval;

    public evalution(String qrels, String res) {
        this.qrels = qrels;
        this.res = res;
        trec_eval = "/Users/chienchichen/Desktop/UQ/course/INFS7410_ir/trec_eval/trec_eval";
    }

    public void eval_PR_map_udcq(String outPath) throws IOException {
        StringBuilder PR = this.eval_PR(outPath);
        StringBuilder mapUdcg = this.eval_map_udcg(outPath);
        PR.append(mapUdcg);
        this.write(PR,outPath);

    }
    public void write(StringBuilder outs,String outPath) throws IOException {
        File file = new File(outPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(outs.toString());
        }
    }

    public StringBuilder eval_PR(String outPath) {
        StringBuilder output = new StringBuilder();
        String cmd = this.trec_eval + " -m set " + this.qrels + " " + this.res;
//        String cmd = this.trec_eval + " -m set " + this.qrels + " " + this.res + " > " + outPath;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", cmd);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
    public StringBuilder eval_map_udcg(String outPath) {
        StringBuilder output = new StringBuilder();
        String cmd = this.trec_eval + " -m map -m ndcg -m ndcg_cut -m P " + this.qrels + " " + this.res;
//        String cmd = this.trec_eval + " -m map -m ndcg -m ndcg_cut -m P " + this.qrels + " " + this.res + " > " + outPath;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", cmd);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    public void eval_q_map_udcg(String outPath) {
        String cmd = this.trec_eval + " -q -m map -m ndcg -m ndcg_cut -m P " + this.qrels + " " + this.res + " > " + outPath;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", cmd);
        try {
            Process process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double [] statistical_test(String filename1, String filename2) throws Exception {
        String [] measure = {"map", "P_5","P_10","P_15","P_20","P_30","P_100","P_200","P_500","P_1000","ndcg","ndcg_cut_5","ndcg_cut_10","ndcg_cut_15","ndcg_cut_20","ndcg_cut_30","ndcg_cut_100","ndcg_cut_200","ndcg_cut_500","ndcg_cut_1000"};
        // Open the two files and extract the topics and measures from the eval files.
        TrecEvaluation file1 = new TrecEvaluation(filename1);
        TrecEvaluation file2 = new TrecEvaluation(filename2);
        double [] pvalueArr = new double [measure.length];
//        PrintWriter pw = new PrintWriter("out.txt");

        for (int i =0; i<measure.length;i++){
            // Create two arrays that contain the scores from each of the eval files for a particular measure.
            double[] scores1 = file1.getScoresForMeasure(measure[i]);
            double[] scores2 = file2.getScoresForMeasure(measure[i]);

            // Create a new TTest object to perform significance testing.
            TTest tTest = new TTest();

            double pvalue = tTest.pairedTTest(scores1, scores2);
            pvalueArr[i] = pvalue;
//            pw.print(measure[i]);
//            pw.print("\t");
//            pw.print(pvalue);
//            pw.print("\n");

            // Output the result so we can see it on the screen later.
//            System.out.printf("p-value for files %s and %s given measure %s: %f\n", filename1, filename2, measure, pvalue);

        }
//        pw.flush();
//        pw.close();
        return pvalueArr;
    }
    public void writeHash(List<String> filelist, HashMap<String, double[]> map, String outPath) throws IOException {
        String [] measure = {"map", "P_5","P_10","P_15","P_20","P_30","P_100","P_200","P_500","P_1000","ndcg","ndcg_cut_5","ndcg_cut_10","ndcg_cut_15","ndcg_cut_20","ndcg_cut_30","ndcg_cut_100","ndcg_cut_200","ndcg_cut_500","ndcg_cut_1000"};
        PrintWriter pw = new PrintWriter(outPath);

        for (int i =0; i<filelist.size();i++){
            pw.println(filelist.get(i));
        }
        for(HashMap.Entry<String,double[]> m :map.entrySet()){
            pw.println(m.getKey());
//            pw.println("\n");
            for (int i =0; i<measure.length;i++){
                pw.print(measure[i]);
                pw.print("\t");
                pw.println(m.getValue()[i]);
//                pw.println("\n");
            }

        }
        pw.flush();
        pw.close();
    }

}
