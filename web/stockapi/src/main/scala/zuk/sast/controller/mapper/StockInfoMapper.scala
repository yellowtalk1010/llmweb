package zuk.sast.controller.mapper

import org.apache.ibatis.annotations.{Delete, Insert, Mapper, Param, Result, Results, Select}
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
    "stock_info(id, stock_code, stock_name, concept) " +
    "VALUES (#{id}, #{stockCode}, #{stockName}, #{concept})"))
  def insert(stock: StockInfoEntity): Int

  @Delete(Array("DELETE FROM stock_info WHERE id = #{id}"))
  def deleteById(@Param("id") id: String): Int

}
