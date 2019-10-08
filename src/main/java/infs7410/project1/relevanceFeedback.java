package infs7410.project1;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;

import infs7410.ranking.BM25_rsj;
import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.io.*;
import java.util.*;

public class relevanceFeedback {

    private Index index;

    private TrecResults qrels = new TrecResults();

    public relevanceFeedback(Index index) {
        this.index = index;
    }

    public void readqrels(String filename) throws IOException {

//        use two hashmap might be better
        HashMap<String,ArrayList<Integer>> qrels = new HashMap<String,ArrayList<Integer>>();
        InputStream is = new FileInputStream(filename);
        BufferedReader buf = new BufferedReader((new InputStreamReader(is)));

        String line = buf.readLine();
        while (line != null) {
            TrecResult t = splitLine(line);
//            this.qrels.add(t);
            line = buf.readLine();
        }
    }

    private TrecResult splitLine(String line) {
        String[] parts = line.split("\\s+");
        int rel = Integer.valueOf(parts[3]);
        return new TrecResult(parts[0], parts[2], rel, 0, "");
    }

    public void runBM25_RSJ(String topic, ArrayList<String>  queryTerms, ArrayList<String>  docIds,List<TrecResults> baseResult){
        BM25_rsj bm25_rsj = new BM25_rsj();
        Lexicon lex = index.getLexicon();
        PostingIndex invertedIndex = index.getInvertedIndex();
        MetaIndex meta = index.getMetaIndex();
        bm25_rsj.setCollectionStatistics(index.getCollectionStatistics());
        HashSet<String> docIdSet = new HashSet<String>(docIds);

        for (TrecResults trecResults : baseResult) {
            int Rprev = 0;
            int Rcur = 0;
            HashMap<String, Double> scores = new HashMap<>();
            for (int i = 0 ; i < trecResults.getTrecResults().size(); i ++){
                TrecResult result = trecResults.getTrecResults().get(i);
//                result.getDocID()

            }
//            for (TrecResult result : trecResults.getTrecResults())



        }

    }

//    public


}
