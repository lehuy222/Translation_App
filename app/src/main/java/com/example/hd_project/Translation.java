package com.example.hd_project;

public class Translation {
    private Integer id;
    private String sourceLanguage;
    private String destinationLanguage;
    private String input;
    private String output;
    private String datetime;
//    private boolean status;

    public Translation(String sourceLanguage, String destinationLanguage, String input, String output, String datetime) {
        this(null, sourceLanguage, destinationLanguage, input, output, datetime);
    }

    // Constructor with ID for database operations
    public Translation(Integer id, String sourceLanguage, String destinationLanguage, String input, String output, String datetime) {
        this.id = id;
        this.sourceLanguage = sourceLanguage;
        this.destinationLanguage = destinationLanguage;
        this.input = input;
        this.output = output;
        this.datetime = datetime;
    }

    public String toString(){return id+": "+sourceLanguage+" - "+destinationLanguage+"\n"+"Input text: "+input+"\n";}
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setDestinationLanguage(String destinationLanguage) {
        this.destinationLanguage = destinationLanguage;
    }

    public Integer getId() {
        return id;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getDestinationLanguage() {
        return destinationLanguage;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

}

