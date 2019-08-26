package infs7410.util;

import java.util.ArrayList;

public class topicInfo {
    private String filename;
    private String topic;
    private ArrayList<String> title;
    private ArrayList<String> query;
    private ArrayList<String> pid;

    public topicInfo(String topic, ArrayList<String> title, ArrayList<String> query, ArrayList<String> pid){
        this.topic = topic;
        this.title = new ArrayList<String>(title);
        this.query = new ArrayList<String>(query);
        this.pid = new ArrayList<String>(pid);
    }
    public void setFilename(String filename){
        this.filename = filename;
    }
    public String getFilename() {
        return this.filename;
    }
    public String getTopic(){
        return this.topic;
    }
    public ArrayList<String> getTitle(){
        return this.title;
    }
    public ArrayList<String> getQuery(){
        return this.query;
    }
    public ArrayList<String> getPid(){
        return this.pid;
    }
}
