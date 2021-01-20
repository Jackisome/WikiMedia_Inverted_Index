package com.dengyj.model;

public class QueryBean {
    String docID;
    float corre;

    public QueryBean(String docID, float corre) {
        this.docID = docID;
        this.corre = corre;
    }

    public String getDocID() {
        return docID;
    }

    public float getCorre() {
        return corre;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public void setCorre(float corre) {
        this.corre = corre;
    }
}
