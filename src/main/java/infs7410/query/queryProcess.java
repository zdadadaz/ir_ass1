package infs7410.query;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;
import org.terrier.querying.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class queryProcess {
    private String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=";
    private String indexPath = "./var/index/pubmed";

    public queryProcess(){

    }
    public String expandQeury(ArrayList<String> terms, int k,String topic) throws IOException {
        StringBuilder termlist = new StringBuilder();
        for (String s : terms){
            StringBuilder tmp = this.expandQeuryOne(s);
            termlist.append(tmp);
        }

        return this.runIDFreduction(termlist.toString(),topic,k);

    }

    public StringBuilder expandQeuryOne(String term)throws IOException {
        StringBuilder termlist = new StringBuilder();
        if (!term.endsWith("*")) {
            return termlist;
        }
        try{
            Scanner s = new Scanner(new URL(this.url + term).openStream());
            String tmp = s.findWithinHorizon("<QueryTranslation>\\s*(.*)\\s*<\\/QueryTranslation>",0);
            termlist = this.parseResponse(tmp);
            System.out.println(termlist.toString());
            Thread.sleep(1000);
        }catch (NullPointerException | InterruptedException nfe){
        }

        return termlist;
    }

    public String runIDFreduction(String query, String topic, int k) throws IOException {
        IndexRef ref = IndexRef.of(this.indexPath + ".properties"); // not sure
        TrecResults resultsIDFr = new TrecResults();
        IDFReduction expansion = new IDFReduction();
//        Manager queryManager = ManagerFactory.from(ref);

        String idfrQuery = expansion.reduce(query,k, ref);
//        logger.info(String.format("PRF QE: [topic %s] issuing query %s: ", topic, idfrQuery));
//        SearchRequest srq = queryManager.newSearchRequestFromQuery(idfrQuery);
//        srq.setControl(SearchRequest.CONTROL_WMODEL, "BM25");
//        queryManager.runSearchRequest(srq);

//        resultsIDFr.getTrecResults().addAll(scoredDocs2TrecResults(srq.getResults(), topic).getTrecResults());
        return idfrQuery;
    }

    public StringBuilder parseResponse(String input){
        StringBuilder queryList= new StringBuilder();
        String [] strArr = input.split(" OR ");
        for (String s : strArr){
            s = s.toLowerCase();
            s = s.replace("<querytranslation>","");
            s = s.replace("<\\/querytranslation>","");
            s = s.replace("all fields","");
            s = s.replaceAll("[^a-zA-Z0-9]", "");
            if (!s.isEmpty()){
                queryList.append(" "+s);
            }
        }
        return queryList;
    }

    static TrecResults scoredDocs2TrecResults(ScoredDocList resultsList, String topic) {
        TrecResults trecResults = new TrecResults();
        for (int i = 0; i < resultsList.size(); i++) {
            ScoredDoc doc = resultsList.get(i);
            trecResults.getTrecResults().add(new TrecResult(topic, doc.getMetadata("docno"), i + 1, doc.getScore(), "qe"));
        }
        return trecResults;
    }



}
