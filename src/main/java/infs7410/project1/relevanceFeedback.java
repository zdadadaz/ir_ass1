package infs7410.project1;

import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.basicmodel.In;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;

import infs7410.ranking.BM25_rsj;
import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.io.*;
import java.util.*;

public class relevanceFeedback {

    private Index index;

    private HashMap<String,HashMap<String,Integer>> qrels;

    public relevanceFeedback(Index index) {
        this.index = index;
    }

    public void readqrels(String filename) throws IOException {
        this.qrels = new HashMap<String,HashMap<String,Integer>>();
        InputStream is = new FileInputStream(filename);
        BufferedReader buf = new BufferedReader((new InputStreamReader(is)));

        String line = buf.readLine();
        while (line != null) {
            splitLine(line);
            line = buf.readLine();
        }
    }

    private void splitLine(String line) {
        String[] parts = line.split("\\s+");
        int rel = Integer.valueOf(parts[3]);

        // debug ////////////////////// 2018 qrel has no 1
//        if (rel ==1){
//            System.out.println("check rel == 1");
//        }
        if (this.qrels.containsKey(parts[0])){
            //  read existing doc_map
            HashMap<String,Integer> doc_map = qrels.get(parts[0]);
            doc_map.put(parts[2],rel);
            this.qrels.put(parts[0],doc_map);
        }
        else{
            //  create new doc_map
            HashMap<String,Integer> doc_map = new HashMap<String,Integer>();
            doc_map.put(parts[2],rel);
            this.qrels.put(parts[0],doc_map);
        }
    }

    public TrecResults runBM25_RSJ(String topic, ArrayList<String>  queryTerms, ArrayList<String>  docIds,TrecResults baseResult,BM25_rsj bm25_rsj) throws IOException {
        Lexicon lex = index.getLexicon();
        PostingIndex invertedIndex = index.getInvertedIndex();
        MetaIndex meta = index.getMetaIndex();
        bm25_rsj.setCollectionStatistics(index.getCollectionStatistics());
        HashSet<String> docIdSet = new HashSet<String>(docIds);

        //// calculate baseline result of R ////
        topic = topic.replace(" ","");
        HashMap<String, Integer> doc_map = baseResult.getDocByTopics(topic);
        String [] doc_arr = new String [doc_map.size()]; // rank from 1
        for (Map.Entry<String, Integer> entry : doc_map.entrySet()) {
            doc_arr[entry.getValue() - 1] = entry.getKey(); // rank - 1 = position
        }
        Integer [] doc_R = caculate_R(doc_arr, topic);

        //// calculate baseline result of ri.
        //// <term, ri>
        HashMap<String, Integer []> rihash = calculate_ri( topic,queryTerms, lex, invertedIndex, doc_map, meta);

        //// calculate scores when R is different
        ArrayList<Integer> rel_change = new ArrayList<>();
        Set<Integer> rel_change_set = new HashSet<>();
        // first element is relevant
        if (doc_R[0] == 1){
            rel_change.add(1);
            rel_change_set.add(1);
        }
        // record relevance index when increase
        for (int q = 1; q < doc_R.length; q++){
            if (doc_R[q] != doc_R[q-1]){
                rel_change.add(q+1);
                rel_change_set.add(q+1);
            }
        }
        ArrayList<TrecResults> scores_news = reranker(topic, queryTerms, lex, invertedIndex, bm25_rsj, meta, docIdSet, doc_map, doc_R,rihash, rel_change);
        Integer count_scores_news = 0;

        // init
        TrecResults results = new TrecResults();
        TrecResults scores_new = baseResult;
        Set<String > include_doc = new HashSet<>();
        include_doc.add(doc_arr[0]);
//        results.getTrecResults().add(scores_new.getTrecResults().get(0));

        ArrayList<String> doclist = new ArrayList<>();
        ArrayList<Double> scorelist = new ArrayList<>();
        doclist.add(doc_arr[0]);
        scorelist.add(scores_new.getTrecResults().get(0).getScore());

        int curIndx = 1;
        //// run baseline result and re-rank
        for (int i = 1; i<doc_arr.length; i++){
            // if R change or relevance at 1st place then do re rank,
//            if ((doc_R[i] != 0 && doc_R[i] != doc_R[i-1]) || (doc_R[0] != 0 && i == 1)){
            if(rel_change_set.contains(i)){
                scores_new = scores_news.get(count_scores_news);
                count_scores_news += 1;
                curIndx = 0;
            }

            // otherwise, use the old one to assign score and document
            for (int qq = 0; qq < scores_new.getTrecResults().size();qq++){
                String tmp_doc = scores_new.getTrecResults().get(curIndx).getDocID();
                TrecResult add_doc = scores_new.getTrecResults().get(curIndx);
                if(!include_doc.contains(add_doc.getDocID())){
//                    results.getTrecResults().add(add_doc);

                    doclist.add(add_doc.getDocID());
                    scorelist.add(add_doc.getScore());

                    curIndx += 1;
                    include_doc.add(add_doc.getDocID());
                    break;
                }
                else{
                    curIndx += 1;
                }
            }

        }
        results.getTrecResults().clear();
        for (int i = 0; i<doclist.size(); i++){
            results.getTrecResults().add(new TrecResult(
                    topic,
                    doclist.get(i),
                    i+1,
                    scorelist.get(i),
                    "bm25_rsj"
            ));
        }


        // Assign the rank to the documents.
//        for (int i = 0; i < results.getTrecResults().size(); i++) {
//            results.getTrecResults().get(i).setRank(i + 1);
//            results.getTrecResults().get(i).setRunName("bm25_rsj");
//        }

        return results;
    }

    public ArrayList<TrecResults> reranker(String topic,ArrayList<String>  queryTerms, Lexicon lex, PostingIndex invertedIndex,BM25_rsj bm25_rsj,MetaIndex meta,HashSet<String> docIdSet, HashMap<String, Integer> doc_map,Integer [] doc_R, HashMap<String, Integer []> ri_hash,ArrayList<Integer> rel_indexs) throws IOException {
        ////   reranker //////
        ArrayList<HashMap<String, Double>> scores_arr = new ArrayList<>();
        // declare output
        for (Integer rel_i : rel_indexs){
            HashMap<String, Double> scores = new HashMap<>();
            scores_arr.add(scores);
        }

        // Iterate over all query terms.
        for (String queryTerm : queryTerms) {
            LexiconEntry entry = lex.getLexiconEntry(queryTerm);
            if (entry == null) {
                continue; // This term is not in the index, go to next document.
            }

            ArrayList<Integer> ri_arr4calc = new ArrayList<>();
            for (Integer rel_i : rel_indexs){
                Integer [] ri_arr = ri_hash.get(queryTerm);
                Integer rank = rel_i;
                // accumulate ri from rank 1 to rank i, where i is current found document
                Integer ri = 0;
                for (int rr = 0; rr < rank ; rr ++){ // rank - 1 == positio
                    if (ri_arr[rr] != null){
                        ri += 1;
                    }
                }
                ri_arr4calc.add(ri);
            }


            // Obtain entry statistics.
            bm25_rsj.setEntryStatistics(entry.getWritableEntryStatistics());

            // Set the number of times the query term appears in the query.
            double kf = 0.0;
            for (String otherTerm : queryTerms) {
                if (otherTerm.equals(queryTerm)) {
                    kf++;
                }
            }
            bm25_rsj.setKeyFrequency(kf);

            // Prepare the weighting model for scoring.
            bm25_rsj.prepare();

            IterablePosting ip = invertedIndex.getPostings(entry);
            ip = invertedIndex.getPostings(entry);
            double score = 0.0;
            while (ip.next() != IterablePosting.EOL) {
                String docId = meta.getItem("docno", ip.getId());
                if (docIdSet.contains(docId)) {
                    for (Integer ii = 0; ii < rel_indexs.size(); ii++){
//                        System.out.println(doc_R[rel_indexs.get(ii)-1].toString() + ri_arr4calc.get(ii).toString());
                        bm25_rsj.set_R_ri(doc_R[rel_indexs.get(ii)-1],ri_arr4calc.get(ii));
                        score = bm25_rsj.score(ip);
                        // by reference or value
                        HashMap<String, Double> scores = scores_arr.get(ii);
                        if (!scores.containsKey(docId)) {
                            scores.put(docId, score);
                        } else {
                            scores.put(docId, scores.get(docId) + score);
                        }
                    }

                }
            }
        }
        // Set score to 0 for docs that do not contain any term.
        for (Integer ii = 0; ii < rel_indexs.size(); ii++){
            HashMap<String, Double> scores = scores_arr.get(ii);
            for (String id : docIdSet) {
                if (!scores.containsKey(id)) {
                    scores.put(id, 0.0);
                }
            }
        }


        ArrayList<TrecResults> output = new ArrayList<>();
        for (Integer ii = 0; ii < rel_indexs.size(); ii++){
            HashMap<String, Double> scores = scores_arr.get(ii);
            //        // Create a results list from the scored documents.
            TrecResults results = new TrecResults();
            for (Map.Entry<String, Double> entry : scores.entrySet()) {
                results.getTrecResults().add(new TrecResult(
                        topic,
                        entry.getKey(),
                        0,
                        entry.getValue(),
                        null
                ));
            }
            output.add(results);
        }

        for (Integer ii = 0; ii < rel_indexs.size(); ii++){
            TrecResults results = output.get(ii);
            // Sort the documents by the score assigned by the weighting model.
            Collections.sort(results.getTrecResults());
            Collections.reverse(results.getTrecResults());
//
//        // Assign the rank to the documents.
            for (int i = 0; i < results.getTrecResults().size(); i++) {
                results.getTrecResults().get(i).setRank(i + 1);
            }
        }


        return output;

    }

    public HashMap<String, Integer []> calculate_ri(String topic,ArrayList<String>  queryTerms, Lexicon lex, PostingIndex invertedIndex,HashMap<String, Integer> doc_map,MetaIndex meta) throws IOException {
        HashMap<String, Integer []> output = new HashMap<>();
        for (String queryTerm : queryTerms) {
            LexiconEntry entry = lex.getLexiconEntry(queryTerm);
            if (entry == null) {
                continue; // This term is not in the index, go to next document.
            }

            // Calculate ri: document frequecy within relevant documents R.
            IterablePosting ip = invertedIndex.getPostings(entry);
            Integer[] ri_arr = getDocumentFreq(ip, meta, doc_map, topic);
            output.put(queryTerm,ri_arr);
        }
        return output;
    }

    public Integer[] getDocumentFreq(IterablePosting ip, MetaIndex meta, HashMap<String, Integer> doc_map_hash, String Topic) throws IOException {
        //    ri contain in doc_map && rank < cur_rank
        Integer[] doc_ri = new Integer[doc_map_hash.size()];
        while (ip.next() != IterablePosting.EOL) {
            String docId = meta.getItem("docno", ip.getId());
            if (doc_map_hash.containsKey(docId) && isRelevance(Topic,docId)) {
                doc_ri[doc_map_hash.get(docId) - 1] = 1; // rank -1 = position
            }
        }
        return doc_ri;
    }

    public boolean isRelevance(String Topic, String docId){
        HashMap<String,Integer> doc_map = this.qrels.get(Topic);
        if (doc_map.containsKey(docId)){
            if (doc_map.get(docId) == 1){
                return true;
            }
            else{
                return false;
            }
        }else{
            return false;
        }
    }

    public Integer [] caculate_R(String [] doc_map, String Topic){
        Integer [] doc_R = new Integer[doc_map.length];
        Integer count = 0;
        for (int i = 0; i < doc_map.length; i++){
            if(isRelevance(Topic,doc_map[i])){
                count += 1;
            }
            doc_R[i] = count;
        }
        return doc_R;
    }




}
