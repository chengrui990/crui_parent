package com.crui.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.crui.dao.CategoryBrandMapper;
import com.crui.dao.CategoryMapper;
import com.crui.dao.SkuMapper;
import com.crui.pojo.goods.*;
import com.crui.service.goods.SkuService;
import com.crui.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.crui.dao.SpuMapper;
import com.crui.entity.PageResult;
import com.crui.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private SkuService skuService;

    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        Map map = new HashMap();
        map.put("spuId",id);
        List<Sku> skuList = skuService.findList(map);
        for (Sku sku: skuList){
            skuService.deletePriceFromRedis(sku.getId());
        }
        spuMapper.deleteByPrimaryKey(id);
        //TODO 删除sku列表
    }

    @Override
    @Transactional
    public void saveGoods(Goods goods) {
        //1.新增spu 的id 为空
        //2.修改，spu 的id 不为空
        //        前端传的sku列表中，sku 的id有空的和不为空的，
        //        sku的id不为空的在数据库中保留并更新，其他的删除
        //        sku的id为空记录插入表中
        Spu spu = goods.getSpu();
        List<Sku> skuList = goods.getSkuList();
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
//        List<Sku> originSkuList = null;
        if (spu.getId() == null){ // 新增
            spu.setId(String.valueOf(idWorker.nextId()));
            spuMapper.insert(spu);

            skuList.forEach(e -> {
                String name = spu.getName();
                if (e.getSpec()==null || "".equals(e.getSpec())){
                    e.setSpec("{}");
                }
                Map<String, String> specMap = JSON.parseObject(e.getSpec(),Map.class);
                for (String value: specMap.values()){
                    name += " " + value;
                }
                e.setName(name);
                e.setCategoryId(spu.getCategory3Id());
                e.setCategoryName(category.getName());

                e.setId(String.valueOf(idWorker.nextId()));
                e.setSpuId(spu.getId());
                e.setCommentNum(0);
                e.setSaleNum(0);
                skuMapper.insert(e);
                skuService.savePrice2RedisById(e.getId(),e.getPrice());
            });
        }else {//修改
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId", spu.getId());
//            criteria.andIsNull("id");
            //只保留前端传了sku id的sku
            List<Sku> originSkuList = skuMapper.selectByExample(example);
            List<Sku> deleteSkus = new ArrayList<>();
            for (Sku originSku: originSkuList){
                boolean flag = false;
                for (Sku sku: skuList){
                    if (sku.getId()!=null && sku.getId().equals(originSku.getId())){
                        flag = true;
                    }
                }
                if (!flag){
                    deleteSkus.add(originSku);
                }
            }
            for (Sku sku: deleteSkus){
                skuMapper.deleteByPrimaryKey(sku.getId());
            }
            skuList.forEach(e -> {
                String name = spu.getName();
                Map<String, String> specMap = JSON.parseObject(e.getSpec(),Map.class);
                for (String value: specMap.values()){
                    name += " " + value;
                }
                e.setName(name);
                e.setCategoryId(spu.getCategory3Id());
                e.setCategoryName(category.getName());
                if (e.getId()==null){
                    e.setId(String.valueOf(idWorker.nextId()));
                    e.setSpuId(spu.getId());
                    e.setCommentNum(0);
                    e.setSaleNum(0);
                    skuMapper.insert(e);
                }else {
                    skuMapper.updateByPrimaryKeySelective(e);
                }
                skuService.savePrice2RedisById(e.getId(),e.getPrice());
            });
//          skuMapper.deleteByExample(example);
            spuMapper.updateByPrimaryKeySelective(spu);
        }

        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setCategoryId(spu.getCategory3Id());
        categoryBrand.setBrandId(spu.getBrandId());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count==0){
            categoryBrandMapper.insert(categoryBrand);
        }

    }

    @Override
    public Goods findGoodsById(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        List<Sku> skuList = skuMapper.selectByExample(example);
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    /**
     * 商品审核
     *
     * @param id
     * @param status
     */
    @Override
    @Transactional
    public void audit(String id, String status,String message) {
//        Spu spu = spuMapper.selectByPrimaryKey(id);
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        if ("1".equals(status)){
            spu.setIsMarketable("1");
        }
        spuMapper.updateByPrimaryKeySelective(spu);
        //记录商品审核记录

        //记录商品日志
    }

    @Override
    public void pull(String id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);

        // 记录商品日志

        //
    }

    @Override
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (! "1".equals(spu.getStatus())){
            throw new RuntimeException("此商品未通过审核");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public Integer putMany(String[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("1");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable","0");
        criteria.andEqualTo("status", "1");
        Integer success = spuMapper.updateByExampleSelective(spu, example);

        //添加商品日志

        //
        return success;
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
