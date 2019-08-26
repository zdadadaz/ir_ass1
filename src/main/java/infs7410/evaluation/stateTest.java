package infs7410.evaluation;

import org.apache.commons.math3.stat.inference.TTest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class stateTest {
    private String [] measure = {"map", "P_5","P_10","P_15","P_20","P_30","P_100","P_200","P_500","P_1000","ndcg","ndcg_cut_5","ndcg_cut_10","ndcg_cut_15","ndcg_cut_20","ndcg_cut_30","ndcg_cut_100","ndcg_cut_200","ndcg_cut_500","ndcg_cut_1000"};
    public stateTest(){
    }

    public double []  statistical_test(String filename1, String filename2) throws Exception {
        // Open the two files and extract the topics and measures from the eval files.
        TrecEvaluation file1 = new TrecEvaluation(filename1);
        TrecEvaluation file2 = new TrecEvaluation(filename2);
        double [] pvalueArr = new double [measure.length];
//        PrintWriter pw = new PrintWriter("out.txt");

        for (int i =0; i<measure.length;i++){
            // Create two arrays that contain the scores from each of the eval files for a particular measure.
            double[] scores1 = file1.getScoresForMeasure(measure[i]);
            double[] scores2 = file2.getScoresForMeasure(measure[i]);

//            System.out.println("scores1");
//            System.out.println(scores1.length);
//            System.out.println("scores2");
//            System.out.println(scores2.length);
//            System.out.println(filename2);

            // Create a new TTest object to perform significance testing.
            TTest tTest = new TTest();

            double pvalue = tTest.pairedTTest(scores1, scores2);
            pvalueArr[i] = pvalue;

        }
        return pvalueArr;
    }
    public void writeHash(List<String> filelist, HashMap<String, double[]> map, String outPath) throws IOException {
        PrintWriter pw = new PrintWriter(outPath);

        for (int i =0; i<filelist.size();i++){
            pw.println(extractfilename(filelist.get(i)));
        }
        pw.println("\n");

        for(HashMap.Entry<String,double[]> m :map.entrySet()){
            String [] qq = m.getKey().split(" ");
            pw.println(extractfilename(qq[0]));
            pw.println(extractfilename(qq[1]));
//            for (int i =0; i<measure.length;i++){
            pw.print(measure[0]);
            pw.print("\t");
            pw.println(m.getValue()[0]);
            pw.print(measure[10]);
            pw.print("\t");
            pw.println(m.getValue()[10]);
//            }
        }
        pw.flush();
        pw.close();
    }

    public String extractfilename(String input) {
        Integer tmp = input.lastIndexOf("/");
        return input.substring(tmp+1,input.length());
    }


}
