package zuk.sast.controller.mapper

import org.apache.ibatis.annotations.{Insert, Mapper, Result, Results, Select}
import zuk.sast.controller.mapper.entity.StockInfoEntity

import java.util

@Mapper
trait StockInfoMapper {

  @Select(Array("SELECT id, stock_code, stock_name, concept, create_time FROM stock_info"))
  @Results(id = "stockResultMap",
    value = Array(
      new Result(property = "id", column = "id"),
      new Result(property = "stock_code", column = "stockCode"),
      new Result(property = "stock_name", column = "stockName"),
      new Result(property = "concept", column = "concept")
    ))
  def selectAll(): util.List[StockInfoEntity]


  @Insert(Array("INSERT INTO " +
    "stock(id, stock_code, stock_name, concept) " +
    "VALUES (#{id}, #{stockCode}, #{stockName}, #{concept}"))
  def insert(stock: StockInfoEntity): Int

}
