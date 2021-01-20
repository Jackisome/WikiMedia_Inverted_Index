package com.dengyj.comtroller;

import com.dengyj.model.AllText;
import com.dengyj.model.QueryEntry;
import com.dengyj.service.QueryServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
public class QueryController {
    @Autowired
    QueryServer queryServer;

    public QueryController() throws FileNotFoundException {
    }

    @RequestMapping("/search")
    public String getQuery(Model model, @RequestParam(name = "query") String query) throws IOException {
        List<QueryEntry> ql = queryServer.getEntry(query);
        model.addAttribute("entries", ql);
        System.out.println("I am here");
        for (QueryEntry e: ql) {
            System.out.println(e.getTitle());
        }
        return "query";
    }

    @RequestMapping("/getAllText")
    public String getAllText(Model model, @RequestParam(name = "title") String title) throws IOException {
        model.addAttribute("title", title);
        String allText = queryServer.getAllText(title);
        System.out.println(allText);
        List<String> allTextList = new ArrayList<>();
        for (int i = 0; i < allText.length() / 100; i++) {
            allTextList.add(allText.substring(i * 100, (i + 1) * 100));
            if (i == allText.length() / 100 - 1) {
                allTextList.add(allText.substring((i + 1) * 100));
            }
        }
        model.addAttribute("list", allTextList);
        return "fulltext";
    }
}
