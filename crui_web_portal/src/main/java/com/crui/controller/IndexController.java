package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.pojo.business.Ad;
import com.crui.service.business.AdService;
import com.crui.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Reference
    private AdService adService;

    @Reference
    private CategoryService categoryService;

    @GetMapping("/index")
    public String index(Model model){

        List<Ad> lbtList = adService.findByPosition("web_index_lb");
        System.out.println(lbtList.size());
        model.addAttribute("lbt", lbtList);

        List<Map> categoryList = categoryService.findCategoryTree();
        model.addAttribute("categoryList", categoryList);
        return "index";
    }


}
