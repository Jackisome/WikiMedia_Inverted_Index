package com.dengyj.service;

import com.dengyj.mapper.TermMapper;
import com.dengyj.mapper.TitleMapper;
import com.dengyj.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QueryServer {
    private final List<String> exTitleList = new ArrayList<>();
    private final StringBuilder ex = new StringBuilder();
    FileChannel idxFile = new RandomAccessFile("D:\\share1\\term_title_corre_filter.txt", "r").getChannel();
    FileChannel xmlFile = new RandomAccessFile("D:\\share1\\enwiki-latest-pages-articles.xml", "r").getChannel();

    @Autowired
    private TermMapper termMapper;

    @Autowired
    private TitleMapper titleMapper;

    public QueryServer() throws FileNotFoundException {
    }

    public List<QueryEntry> getEntry(String query) throws IOException {
//        query = "Natural && Process && !000000000";
        List<Map.Entry<String, Float>> srcList = getDocList(query);

        List<QueryEntry> queryEntries = new ArrayList<>();
        int length = Math.min(srcList.size(), 100);
        for (int i = 0; i < length; i++) {
            String preReadText = "";
            QueryEntry queryEntry = new QueryEntry();
            queryEntry.setTitle(srcList.get(i).getKey());
            queryEntry.setPreReadText(preReadText);
            queryEntries.add(queryEntry);
        }
        return queryEntries;
    }

    public String getAllText(String title) throws IOException {
        Title titleEntry = titleMapper.selectByPrimaryKey(title);
        String allText = "";
        if (titleEntry != null) {
            long position = titleEntry.getPosition();
            long length = titleEntry.getLength();
//            xmlFile.seek(position);
            byte[] content = new byte[(int) length];
            MappedByteBuffer out = xmlFile.map(FileChannel.MapMode.READ_ONLY, position, length);
            out.get(content);
            allText = new String(content);
        }
        return allText;
    }

    public List<Map.Entry<String, Float>> getDocList(String query) throws IOException {
        String[] srcTokens = query.split("\\s+?\\|\\|\\s+");
        System.out.println(srcTokens.length);
        List<Map<String, Float>> docLists = new ArrayList<>();
        for (String token : srcTokens) {
            docLists.add(getOrList(token));
        }
        System.out.println("3");
        getExList();
        System.out.println("4");
        for (String str: exTitleList) {
            System.out.println(str);
        }
        Map<String, Float> docMap = new TreeMap<>();
        for (Map<String, Float> map : docLists) {
            for (Map.Entry<String, Float> entry : map.entrySet()) {
                String key = entry.getKey();
                if (!exTitleList.contains(key)) {
                    docMap.put(key, entry.getValue() + (docMap.containsKey(key) ? docMap.get(key) : 0));
                }
            }
        }
        System.out.println("5");
        List<Map.Entry<String, Float>> sortedDocMap = new ArrayList<>(docMap.entrySet());
        sortedDocMap.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return sortedDocMap;
    }

    private void getExList() throws IOException {
        EnglishAnalyzer exAnalyzer = new EnglishAnalyzer(null);
        List<String> exTokens = new ArrayList<>();
        TokenStream ts = new PorterStemFilter(exAnalyzer.tokenStream(null, ex.toString()));
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
        ts.reset();//必须的
        while (ts.incrementToken()) {
            String term = charTermAttribute.toString();
            exTokens.add(term);
        }
        ts.end();
        exAnalyzer.close();

        for (String token: exTokens) {
            Set<String> doc = getOneDocList(token).keySet();
            if (doc.size() != 0) {
                exTitleList.addAll(doc);
            }
        }
    }

    private Map<String, Float> getOrList(String query) throws IOException {
        String[] tokens = query.split("\\s+?&&\\s+");
        StringBuilder reserve = new StringBuilder();
        EnglishAnalyzer reserveAnalyzer = new EnglishAnalyzer(null);
        for (String token: tokens) {
            System.out.println("in here");
            if (token.matches("!.*")) {
                ex.append(token.substring(1)).append("\t");
            }
            else {
                reserve.append(token).append("\t");
            }
        }
        List<String> reserveTokens = new ArrayList<>();
        TokenStream ts = new PorterStemFilter(reserveAnalyzer.tokenStream(null, reserve.toString()));
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
        ts.reset();//必须的
        while (ts.incrementToken()) {
            String term = charTermAttribute.toString();
            reserveTokens.add(term);
            System.out.println(term);
        }
        ts.end();
        reserveAnalyzer.close();
        
        return getDoc(reserveTokens);
    }

    private Map<String, Float> getDoc(List<String> reserveTokens) throws IOException {
        List<Map<String, Float>> docLists = new ArrayList<>();
        for (String token: reserveTokens) {
            Map<String, Float> docList = getOneDocList(token);
            if (docList.size() != 0) {
                docLists.add(docList);
            }
        }
        Map<String, Float> docMap = new TreeMap<>();
        Set<String> docSet = docLists.get(0).keySet();
        for (Map<String, Float> map : docLists) {
            docSet.retainAll(map.keySet());
        }
        for (Map<String, Float> map : docLists) {
            for (String key: docSet) {
                docMap.put(key, map.get(key) + (docMap.containsKey(key) ? docMap.get(key) : 0));
            }
        }
        return docMap;
    }

    private Map<String, Float> getOneDocList(String token) throws IOException {
        System.out.println(token);
        System.out.println("******************************");
        Term termEntry = termMapper.selectByPrimaryKey(token);
        Map<String, Float> entries = new TreeMap<>();
        if (termEntry == null) {
            return entries;
        }
        long position = termEntry.getPosition();
        long length = termEntry.getLength();
        MappedByteBuffer out = idxFile.map(FileChannel.MapMode.READ_ONLY, position, length);
        byte[] content = new byte[(int) length];
        out.get(content);
        String termIndex = new String(content);
//        idxFile.seek(position);
//        String termIndex = idxFile.readLine();
//        System.out.println(termIndex);

        String[] set = termIndex.split("\t");
        for (int i = 1; i < set.length; i += 2) {
            entries.put(set[i], Float.parseFloat(set[i + 1]));
        }
        return entries;
    }
}
