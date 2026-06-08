package zuk.sast.controller.mapper

import org.apache.ibatis.annotations._
import zuk.sast.controller.mapper.entity.{StockEntity}
import java.util

@Mapper
trait StockMapper {

  @Select(Array("SELECT id, stock_code, name, stock_type FROM stock"))
  @Results(Array(
    new Result(property = "id", column = "id"),
    new Result(property = "stock_code", column = "stockCode"),
    new Result(property = "name", column = "name"),
    new Result(property = "stock_type", column = "stockType")
  ))
  def selectAll(): util.List[StockEntity]

  @Insert(Array("INSERT INTO stock(id, stock_code, name, stock_type) VALUES (#{id}, #{stockCode}, #{name}, #{stockType})"))
  def insert(stock: StockEntity): Int

  @Delete(Array("DELETE FROM stock WHERE stock_code = #{stock_code} AND stock_type = #{stock_type}"))
  def deleteByCode(@Param("stock_code") stock_code: String,
                   @Param("stock_type") stock_type: String): Int

  @Select(Array("SELECT * FROM stock WHERE stock_code = #{stock_code}"))
  def selectByCode(@Param("stock_code") stock_code: String): util.List[String]

}