package zuk.sast.rules.controller.stock;

import com.beust.jcommander.internal.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.controller.mapper.StockMapper;
import zuk.sast.rules.controller.mapper.entity.StockEntity;

import java.util.*;
import java.util.stream.Collectors;

import zuk.sast.rules.controller.stock.analysis.LoaderStockData;

/***
 *
 */
@Slf4j
@RestController
@RequestMapping("stock")
public class AllStockController {

    //https://xtrade.newone.com.cn/market/json?funcno=30029&version=1&stock_code=300287&market=SZ&start=0&dayitems=1&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756699944822.16897853520034
    //https://xtrade.newone.com.cn/market/json?funcno=20006&version=1&stock_code=300287&market=SZ&count=100&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756699944822.16897853520033
    //https://xtrade.newone.com.cn/market/json?funcno=30029&version=1&stock_code=300287&market=SZ&start=0&dayitems=5&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756700218269.16897853520262
    //https://xtrade.newone.com.cn/market/json?funcno=20044&version=1&stock_code=300287&market=SZ&count=1000&type=day&cms-trace-id=fe98fa982fbf42ce88b268b841bd8a84.1756700327368.16897853520352

    @Autowired
    private StockMapper stockMapper;

    @GetMapping("delete")
    public synchronized Map<String, Object> delete(String api_code) {
        log.info("delete:" + api_code);

        Map<String, Object> result = new HashMap<>();
        try {
            this.stockMapper.deleteByCode(api_code);

            result.put("status", "ok");
            return result;

        }catch (Exception e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            result.put("status", e.getMessage());
        }

        return result;
    }


    @GetMapping("add")
    public synchronized Map<String, Object> add(String api_code) {
        log.info("add:" + api_code);
        Map<String, Object> result = new HashMap<>();
        try {
            List list = this.stockMapper.selectByCode(api_code);
            if(list!=null && list.size()>0){
                result.put("status", "已存在");
                return result;
            }
            else {
                LoaderStockData.STOCKS.stream().filter(stock->stock.getApi_code().equals(api_code)).forEach(socket->{
                    StockEntity stockEntity = new StockEntity();
                    stockEntity.setId(UUID.randomUUID().toString());
                    stockEntity.setCode(socket.getApi_code());
                    stockEntity.setJys(socket.getJys());
                    stockEntity.setName(socket.getName());
                    stockEntity.setType("LOVE");
                    this.stockMapper.insert(stockEntity);
                });
                result.put("status", "ok");
                return result;
            }

        }catch (Exception e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            result.put("status", e.getMessage());
        }

        return result;
    }

    /***
     * 我的关注
     * @return
     */
    @GetMapping("my")
    public Map<String, Object> my(){
        try {
            List<StockEntity> stockEntities = stockMapper.selectAll();

            Map<String, Object> map = new HashMap<>();
            Set<String> sets = stockEntities.stream().map(e->e.getCode()).collect(Collectors.toSet());
            List<LoaderStockData.StockApiVO> ls = LoaderStockData.STOCKS.stream().filter(stock->{
                return sets.contains(stock.getApi_code());
            }).toList();

            map.put("stocks", ls);
            return map;
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return new HashMap<>();
    }

    public static final Set<String> tags = Arrays.asList(
                    "人工智能",
                    "deepseek",
                    "昇腾",
                    "数据中心",
                    "芯片"
            ).stream().collect(Collectors.toSet());

    @GetMapping("all")
    public Map<String, Object> all(String search){

        List<LoaderStockData.StockApiVO> list;
        if(search!=null && search.length()>0){
            Set<String> splits = Arrays.stream(search.split("\n")).filter(e->e!=null && e.trim().length()>0).collect(Collectors.toSet());

            list = LoaderStockData.STOCKS.stream().filter(e->{
                return splits.stream().filter(tag->{
                    return e.getGl().contains(tag);
                }).count() > 0;
            }).collect(Collectors.toList());

        }
        else {
            //没有输入条件，则默认输出1000条
            list = LoaderStockData.STOCKS.stream().filter(e->{
                return tags.stream().filter(tag->{
                    return e.getGl().contains(tag);
                }).count() > 0;
            }).collect(Collectors.toList());
            if(list!=null && list.size()>1000){
                list = list.subList(0, 1000);
            }
        }

        log.info("search:" + search +  ", stocks:" + list.size() + ", blocks:" + tags.size());

        Map<String, Object> map = new HashMap<>();
        map.put("stocks", list);
        map.put("blocks",tags);
        return map;
    }

}
