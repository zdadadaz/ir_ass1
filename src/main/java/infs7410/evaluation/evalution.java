package infs7410.evaluation;

import infs7410.evaluation.TrecEvaluation;
import java.io.PrintWriter;
import org.apache.commons.math3.stat.inference.TTest;
import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * evalution - Evaluate the result of algorithm
 * @author Chien-chi chen
 */
public class evalution {
    /**
     * The path of ground truth
     */
    private String qrels;
    /**
     * The target result to evaluate
     */
    private String res;
    /**
     * The absolute path of trec_eval.exe
     */
    private String trec_eval;
    /**
     * Assign input into private class
     *
     * @param qrels Input query path
     * @param res Input result path
     * @require {@code qrels != null,res != null}
     */
    public evalution(String qrels, String res, String trec_eval_in) {
        this.qrels = qrels;
        this.res = res;
        this.trec_eval = trec_eval_in;
    }
    /**
     * calculate precision & recall & map & udcg
     *
     * @param outPath outPath of evaluation result.
     * @require {@code outPath != null}
     */
    public void eval_PR_map_udcq(String outPath) throws IOException {
        StringBuilder PR = this.eval_PR(outPath);
        StringBuilder mapUdcg = this.eval_map_udcg(outPath);
//        System.out.println(PR.toString());
//        System.out.println(mapUdcg.toString());

        PR.append(mapUdcg);
        this.write(PR,outPath);

    }
    /**
     * Write out evaluation result
     *
     * @param outs evaluation result.
     * @param outPath outpu  path of evaluation result.
     * @require {@code outs != null,outPath != null}
     */
    public void write(StringBuilder outs,String outPath) throws IOException {
        File file = new File(outPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(outs.toString());
        }
    }
    /**
     * Calculate precison and recall
     *
     * @param outPath output  path of evaluation result.
     * @require {@code outPath != null}
     * @return The evaluation result as String
     */
    public StringBuilder eval_PR(String outPath) {
        StringBuilder output = new StringBuilder();
//        System.out.println(this.qrels);
//        System.out.println(this.res);
        String cmd = this.trec_eval + " -m set " + this.qrels + " " + this.res;
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
//        System.out.println(output);

        return output;
    }
    /**
     * Calculate map and udcg
     *
     * @param outPath output  path of evaluation result.
     * @require {@code outPath != null}
     * @return The evaluation result as String
     */
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
    /**
     * Calculate map and udcg for each topic
     *
     * @param outPath output  path of evaluation result.
     * @require {@code outPath != null}
     */
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
    /**
     * Calculate map
     *
     * @return map value
     */
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
