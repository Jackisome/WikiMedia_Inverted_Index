package com.dengyj.model;

import org.springframework.context.annotation.Bean;

public class QueryEntry {
    String title;
    String preReadText;

    public String getTitle() {
        return title;
    }

    public String getPreReadText() {
        return preReadText;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPreReadText(String preReadText) {
        this.preReadText = preReadText;
    }
}
