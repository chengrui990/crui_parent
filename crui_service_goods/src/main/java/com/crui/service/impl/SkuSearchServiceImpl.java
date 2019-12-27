package com.crui.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.crui.dao.BrandMapper;
import com.crui.dao.SpecMapper;
import com.crui.pojo.goods.Sku;
import com.crui.service.goods.SkuSearchService;
import com.crui.service.goods.SkuService;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuSearchServiceImpl implements SkuSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private SkuService skuService;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SpecMapper specMapper;

    @Override
    public Map search(Map<String, String> searchMap) {
        System.out.println("SkuSearchServiceImpl,进入impl，searchMap="+searchMap);
        //封装查询请求
        SearchRequest searchRequest = new SearchRequest("sku");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //关键字搜索
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);

        //商品分类过滤
        if (searchMap.get("category")!=null){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName",searchMap.get("category"));
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //品牌过滤
        if (searchMap.get("brand")!=null){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName",searchMap.get("brand"));
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //规格过滤
        for (String key: searchMap.keySet()){
            if (key.startsWith("spec.")){
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key+".keyword",searchMap.get(key));
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //价格过滤
        if (searchMap.get("price")!=null){
            String[] prices = searchMap.get("price").split("-");
            if (!prices[0].equals("0")){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(prices[0]+"00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
            if (!prices[1].equals("*")){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(prices[1]+"00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        Integer pageNo = Integer.parseInt(searchMap.get("pageNo"));
        Integer pageSize = 30;
        int fromIndex = (pageNo-1) * pageSize;
        searchSourceBuilder.from(fromIndex);
        searchSourceBuilder.size(pageSize);
        //排序
        String sort = searchMap.get("sort");
        String sortOrder = searchMap.get("sortOrder");
        if (!"".equals(sort)){
            searchSourceBuilder.sort(sort, SortOrder.valueOf(sortOrder));
        }

        //高亮关键字
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<font style='color:red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

//      //商品分类（聚合查询）
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sku_category").field("categoryName");
        searchSourceBuilder.aggregation(termsAggregationBuilder);


        Map resultMap = new HashMap();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            TotalHits totalHits = searchHits.getTotalHits();
            System.out.println("SkuSearchServiceImpl,记录数："+totalHits.value);
//            searchMap.put("total",String.valueOf(totalHits.value));
            SearchHit[] hits = searchHits.getHits();
            //商品列表
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (SearchHit hit: hits){
                Map<String, Object> skuMap = hit.getSourceAsMap();
                //高亮处理
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                HighlightField nameField = highlightFieldMap.get("name");
                String name = nameField.getFragments()[0].toString();
                skuMap.put("name",name);
                resultList.add(skuMap);
            }
            resultMap.put("rows",resultList);

            //商品分类列表
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
            Terms terms = (Terms) aggregationMap.get("sku_category");

            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            List<String> categoryList = new ArrayList<>();
            for (Terms.Bucket bucket: buckets){
                categoryList.add(bucket.getKeyAsString());
            }
            resultMap.put("categoryList",categoryList);

            String categoryName = "";
            if (searchMap.get("category")==null){
                if (categoryList.size()>0){
                    categoryName = categoryList.get(0);
                }
            }else {
                categoryName = searchMap.get("category");
            }
            //品牌列表
            if (searchMap.get("brand")==null){
                List<Map> brandList = brandMapper.findListByCategoryName(categoryName);
                resultMap.put("brandList",brandList);
                System.out.println("SkuSearchServiceImpl,brandList" + brandList);
            }
            //规格列表
            List<Map> specList = specMapper.findListByCategoryName(categoryName);//规格列表
            for (Map spec: specList){
                String[] options = ((String) spec.get("options")).split(",");
                spec.put("options",options);
            }
            resultMap.put("specList",specList);
            //页码
            long totalCount = searchHits.getTotalHits().value;
            long pageCount = (totalCount % pageSize==0) ? totalCount/pageSize : (totalCount/pageSize)+1;
            resultMap.put("totalPage", pageCount);
            System.out.println("SkuSearchServiceImpl,categoryList" + categoryList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    @Override
    public void saveAll2Elastic() {
        Map<String,Object> searchMap = new HashMap();
        searchMap.put("status","1");
        List<Sku> skuList = skuService.findList(searchMap);

        BulkRequest bulkRequest = new BulkRequest();

        for (Sku sku: skuList){
            IndexRequest indexRequest = new IndexRequest("sku");
            Map skuMap = new HashMap();
            skuMap.put("name",sku.getName());
            skuMap.put("brandName",sku.getBrandName());
            skuMap.put("categoryName", sku.getCategoryName());
            skuMap.put("price",sku.getPrice());
            skuMap.put("createTime", sku.getCreateTime());
            skuMap.put("saleNum",sku.getSaleNum());
            skuMap.put("image",sku.getImage());
            skuMap.put("commentNum",sku.getCommentNum());
             //sku.getSpec();//{'颜色': '红色', '版本': '8GB+128GB'}
//            Map specMap =JSON.parseObject(sku.getSpec(),Map.class);
            Map<String, String> skuSpecItems = (Map) JSON.parseObject(sku.getSpec());
            skuMap.put("spec",skuSpecItems);
            indexRequest.source(skuMap);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("saveAll2Elastic,+status="+bulkResponse.status().getStatus());
        System.out.println("saveAll2Elastic,+message="+bulkResponse.buildFailureMessage());
//        try {
//            restHighLevelClient.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
