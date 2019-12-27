package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.service.goods.SkuSearchService;
import com.crui.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SearchController {
    @Reference
    private SkuSearchService skuSearchService;

    @GetMapping("/search")
    public String search(Model model,@RequestParam Map<String, String> searchMap) throws Exception {
        System.out.println("SearchController,解决乱码前,searchMap="+searchMap);
        //解决中文乱码
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);
        System.out.println("SearchController,解决乱码后,searchMap="+searchMap);
        if (searchMap.get("pageNo")==null){
            searchMap.put("pageNo","1");
        }
        if (searchMap.get("sort")==null){
            searchMap.put("sort","");
        }
        if (searchMap.get("sortOrder")==null){
            searchMap.put("sortOrder","DESC");
        }


        Map result = skuSearchService.search(searchMap);
        System.out.println("SearchController,Service层返回result="+result);
        model.addAttribute("result", result);

        StringBuffer url = new StringBuffer("/search.do?");
        for (String key:searchMap.keySet()){
            url.append("&"+key+"="+searchMap.get(key));
        }

        model.addAttribute("url",url);
        model.addAttribute("searchMap",searchMap);

        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        model.addAttribute("pageNo",pageNo);

        int totalPage = new Integer(String.valueOf(result.get("totalPage")));
        int startPage=1;
        int endPage = totalPage;
        if (totalPage>5){
            startPage = pageNo-2;
            if (startPage<1){
                startPage =1;
            }
            endPage = pageNo+3;
            if (endPage>totalPage){
                endPage=totalPage;
            }
        }

        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);
        return "search";
    }

}
