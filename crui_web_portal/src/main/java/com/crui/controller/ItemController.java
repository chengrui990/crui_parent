package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.crui.pojo.goods.Goods;
import com.crui.pojo.goods.Sku;
import com.crui.pojo.goods.Spu;
import com.crui.service.goods.CategoryService;
import com.crui.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Reference
    private SpuService spuService;

    @Reference
    private CategoryService categoryService;

    @Value("${pagePath}")
    private String pagePath;

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping("/createPage")
    public void createPage(String spuId){
        //1.查询商品信息
        Goods goods = spuService.findGoodsById(spuId);
        //获取商品信息
        Spu spu = goods.getSpu();
        //获取sku列表
        List<Sku> skuList = goods.getSkuList();

        //查询商品分类
        List<String> categoryList =new ArrayList<>(3);
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName());
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName());
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName());

        Map<String,String> urlMap = new HashMap<>();
        for (Sku sku: skuList){
            if ("1".equals(sku.getStatus())){
                String specJson = JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
                urlMap.put(specJson,sku.getId()+".html");
            }


        }
        System.out.println("urlMap:   "+urlMap.toString());

        //2.批量生产sku页面
        for (Sku sku : skuList){
            System.out.println("sku:"+sku.getId());
            //创建上下文和数据模型
            Context context = new Context();
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("spu", spu);
            dataModel.put("sku", sku);
            dataModel.put("categoryList", categoryList);
            dataModel.put("skuImages", sku.getImages().split(","));
            dataModel.put("spuImages", spu.getImages().split(","));
            dataModel.put("paraItems", JSON.parseObject(spu.getParaItems()));
            //{'颜色': '蓝色', '尺码': '27'}
            Map<String, String> skuSpecItems = (Map) JSON.parseObject(sku.getSpec());
            dataModel.put("specItems", skuSpecItems);
            //{"颜色":["深色","黑色","蓝色"],"尺码":["24","25","26","27","28","29","30","31","32","33"]}
            //{"颜色":[{'option':"深色",'checked':true},{'option':"黑色",'checked':true},,"蓝色"],"尺码":["24","25","26","27","28","29","30","31","32","33"]}
            Map<String,List> spuSpecItems =  (Map)JSON.parseObject(spu.getSpecItems());
            for (String spuKey: spuSpecItems.keySet()){
                List<String> optionList = spuSpecItems.get(spuKey);
                List<Map> newOptionList = new ArrayList<>();
                for (String option: optionList){
                    Map map = new HashMap();
                    map.put("option",option);
                    if (option.equals(skuSpecItems.get(spuKey))){
                        map.put("checked",true);
                    }else {
                        map.put("checked",false);
                    }
                    Map<String, String> spec = (Map) JSON.parseObject(sku.getSpec());//当前sku{'颜色': '蓝色', '尺码': '27'}
                    System.out.println(spec.getClass().toString());
                    spec.put(spuKey,option);//变成其他的规格，
                    String specJson = JSON.toJSONString(spec, SerializerFeature.MapSortField);
                    map.put("url", urlMap.get(specJson));
                    newOptionList.add(map);
                }
                spuSpecItems.put(spuKey,newOptionList);


            }
            System.out.println("spuSpecItems:"+spuSpecItems.toString());
            dataModel.put("spuSpecItems", spuSpecItems);

            context.setVariables(dataModel);
            //
            File dir = new File(pagePath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            File dest = new File(dir, sku.getId()+".html");

            PrintWriter writer = null;
            try {
                writer = new PrintWriter(dest, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            templateEngine.process("item", context, writer);
        }
    }
}
