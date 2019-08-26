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

    public Double eval_map() {
        Double output = 0.0;
        String cmd = this.trec_eval + " -m map -m ndcg -m ndcg_cut -m P " + this.qrels + " " + this.res;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", cmd);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String [] tmp;
            String name;
            while ((line = reader.readLine()) != null) {
                tmp = line.split("\t");
                name=tmp[0].replaceAll(" ","");
                if (name.equals("map")){
                    output =  Double.parseDouble(tmp[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

}
