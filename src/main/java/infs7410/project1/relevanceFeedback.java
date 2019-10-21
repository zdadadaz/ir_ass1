package infs7410.project1;

import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.WeightingModelLibrary;
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

    // query-> docId-> ip
    private HashMap<String, HashMap<String,ArrayList<Double>>> query_doc_ip;
    private HashMap<String, ArrayList<ArrayList<Double>>> repeat_doc_ip;

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

        // declare query_doc_ip
        this.query_doc_ip = new HashMap<>();
        this.repeat_doc_ip = new HashMap<>();

        //// calculate baseline result of R ////
        topic = topic.replace(" ","");
        HashMap<String, Integer> doc_map = baseResult.getDocByTopics(topic);
        String [] doc_arr = new String [doc_map.size()]; // rank from 1
        for (Map.Entry<String, Integer> entry : doc_map.entrySet()) {
            doc_arr[entry.getValue() - 1] = entry.getKey(); // rank - 1 = position
        }

        //// record ri and record every info. for each query term.
        //// <term, ri>
        HashMap<String, HashSet<String>> rihash = calculate_ri( topic,queryTerms, lex, invertedIndex, docIdSet, meta,bm25_rsj);

        // init
        ArrayList<String> doclist = new ArrayList<>();
        ArrayList<Double> scorelist = new ArrayList<>();
        TrecResults scores_new = baseResult;
        Set<String > include_doc = new HashSet<>();
        // init assign
        scores_new = reranker(topic, queryTerms, docIdSet, doclist, 0,rihash, 0.45);
        include_doc.add(scores_new.getTrecResults().get(0).getDocID());
        doclist.add(scores_new.getTrecResults().get(0).getDocID());
        scorelist.add(scores_new.getTrecResults().get(0).getScore());
        String pre_doc = scores_new.getTrecResults().get(0).getDocID();
        docIdSet.remove(pre_doc);
        int curIndx = 1;
        Integer R = 0;
        //// run baseline result and re-rank
        for (int i = 1; i<doc_arr.length; i++){
            // if R change then do re rank,
            if(isRelevance(topic,pre_doc)){
                R += 1;
                scores_new = reranker(topic, queryTerms, docIdSet, doclist, R,rihash, bm25_rsj.getParameter());
                curIndx = 0;
            }
            String cur_doc = scores_new.getTrecResults().get(curIndx).getDocID();
            // otherwise, use the old one to assign score and document
            doclist.add(cur_doc);
            scorelist.add(scores_new.getTrecResults().get(curIndx).getScore());
            curIndx += 1;
            pre_doc = cur_doc;
            include_doc.add(cur_doc);
            docIdSet.remove(cur_doc);
        }

        TrecResults results = new TrecResults();
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
        for (int i = 0; i < results.getTrecResults().size(); i++) {
            results.getTrecResults().get(i).setRank(i + 1);
            results.getTrecResults().get(i).setRunName("bm25_rsj");
        }

        return results;
    }

    public TrecResults reranker(String topic,ArrayList<String>  queryTerms,HashSet<String> docIdSet, ArrayList<String> doclist,Integer doc_R, HashMap<String, HashSet<String>> ri_hash, Double b) throws IOException {
        ////   reranker //////
        HashMap<String, Double> scores = new HashMap<>();
        // Iterate over all query terms.
        for (String queryTerm : queryTerms) {
            if (!this.query_doc_ip.containsKey(queryTerm)) {
                continue; // This term is not in the index, go to next document.
            }

            HashSet<String> ri_set = ri_hash.get(queryTerm);
            // accumulate ri from rank 1 to rank i, where i is current found document
            Integer ri = 0;
            for (String dl: doclist){
                if (ri_set.contains(dl)){
                    ri += 1;
                }
            }
            if (ri> doc_R){
                System.out.println("===================Wrong ri=====================================");
            }

            // calculate score
            double score = 0.0;
            HashMap<String,ArrayList<Double>> query_hash = this.query_doc_ip.get(queryTerm);
            for(HashMap.Entry<String,ArrayList<Double>> m :query_hash.entrySet()){
                String docId = m.getKey();
//                if(docId.equals("25085796")){
//                    System.out.println(queryTerm);
//                    System.out.println("aa");
//                }
                if (docIdSet.contains(docId)) {
                    score = bm25_rsj_calculate_score(queryTerm, docId, (double) doc_R, (double) ri,b);
                    if (!scores.containsKey(docId)) {
                        scores.put(docId, score);
                    } else {
                        scores.put(docId, scores.get(docId) + score);
                    }
                }
            }
        }
        // Set score to 0 for docs that do not contain any term.
        for (String id : docIdSet) {
            if (!scores.containsKey(id)) {
                scores.put(id, 0.0);
            }
        }


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

        // Sort the documents by the score assigned by the weighting model.
        Collections.sort(results.getTrecResults());
        Collections.reverse(results.getTrecResults());
//
//        // Assign the rank to the documents.
        for (int i = 0; i < results.getTrecResults().size(); i++) {
            results.getTrecResults().get(i).setRank(i + 1);
        }

        return results;

    }

    public Double bm25_rsj_calculate_score(String query,String docId, Double R, Double ri,Double b){
        ArrayList<Double> data = this.query_doc_ip.get(query).get(docId);
        double score = 0;
        score = bm25_rsj_calculate_score_data(data, R, ri, b);
        if (data.get(6) >1){
            ArrayList<ArrayList<Double>> tmp = this.repeat_doc_ip.get(query+docId);
            for (ArrayList<Double> t : tmp){
                score+= bm25_rsj_calculate_score_data(t, R, ri, b);
            }
        }
        return score;
    }

    public Double bm25_rsj_calculate_score_data(ArrayList<Double> data, Double R, Double ri,Double b){
        double k_1 = 1.2d;
        double k_3 = 8d;
        double numberOfDocuments = data.get(0);
        double documentFrequency = data.get(1);
        double averageDocumentLength = data.get(2);
        double docLength = data.get(3);
        double tf = data.get(4);
        double keyFrequency = data.get(5);
        final double K = k_1 * ((1 - b) + b * docLength / averageDocumentLength);
        return WeightingModelLibrary.log((numberOfDocuments - documentFrequency - R + ri + 0.5d) / (documentFrequency - ri + 0.5d) * (ri + 0.5d)/(R - ri +0.5d)) *
                ((k_1 + 1d) * tf / (K + tf)) *
                ((k_3+1)*keyFrequency/(k_3+keyFrequency));
    }


    public HashMap<String, HashSet<String>> calculate_ri(String topic,ArrayList<String>  queryTerms, Lexicon lex, PostingIndex invertedIndex,HashSet<String> doc_map,MetaIndex meta, BM25_rsj bm25_rsj) throws IOException {
        HashMap<String, HashSet<String>> output = new HashMap<>();
        for (String queryTerm : queryTerms) {
            LexiconEntry entry = lex.getLexiconEntry(queryTerm);
            if (entry == null) {
                continue; // This term is not in the index, go to next document.
            }
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

            HashMap<String,ArrayList<Double>> query_hash = new HashMap<>();
            this.query_doc_ip.put(queryTerm,query_hash);

            // check query_hash is call by reference
            // Calculate ri: document frequecy within relevant documents R.
            IterablePosting ip = invertedIndex.getPostings(entry);
            HashSet<String> ri_arr = getDocumentFreq(ip, meta, doc_map, topic,bm25_rsj,query_hash,queryTerm);
            output.put(queryTerm,ri_arr);
        }
        return output;
    }

    public HashSet<String> getDocumentFreq(IterablePosting ip, MetaIndex meta, HashSet<String> doc_map_hash, String Topic,BM25_rsj bm25_rsj,HashMap<String,ArrayList<Double>> query_hash, String queryTerm) throws IOException {
        //    ri contain in doc_map && rank < cur_rank

        HashSet<String> doc_ri = new HashSet<String>();
        while (ip.next() != IterablePosting.EOL) {
            String docId = meta.getItem("docno", ip.getId());
            if (doc_map_hash.contains(docId)){
                double score = bm25_rsj.score(ip);
                ArrayList<Double> tmp = bm25_rsj.getData();
                tmp.add(1.0);
//                if(docId.equals("25085796")){
//                    System.out.println(queryTerm);
//                    System.out.println(score);
//                }
                if(query_hash.containsKey(docId)){
                    if (this.repeat_doc_ip.containsKey(queryTerm+docId)){
                        this.repeat_doc_ip.get(queryTerm+docId).add(tmp);
                    }else{
                        ArrayList<ArrayList<Double>> newTmp = new ArrayList<>();
                        newTmp.add(tmp);
                        this.repeat_doc_ip.put(queryTerm+docId,newTmp);
                    }
                    tmp = query_hash.get(docId);
                    tmp.set(6,tmp.get(6)+1);
                }else {
                    query_hash.put(docId,tmp);
                }
                if (isRelevance(Topic,docId)) {
                    doc_ri.add(docId);
                }
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
