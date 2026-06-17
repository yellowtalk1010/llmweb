package zuk.sast.controller.mapper

import org.apache.ibatis.annotations._
import zuk.sast.controller.mapper.entity.{StockEntity}
import java.util

@Mapper
trait StockMapper {

  @Select(Array("SELECT id, stock_code, name, stock_type, createtime, remark FROM stock"))
  @Results(Array(
    new Result(property = "id", column = "id"),
    new Result(property = "stock_code", column = "stockCode"),
    new Result(property = "name", column = "name"),
    new Result(property = "stock_type", column = "stockType"),
    new Result(property = "createtime", column = "createtime"),
    new Result(property = "remark", column = "remark")
  ))
  def selectAll(): util.List[StockEntity]

  @Select(Array("SELECT id, stock_code, name, stock_type, createtime, remark FROM stock WHERE createtime = #{createtime}"))
  @Results(Array(
    new Result(property = "id", column = "id"),
    new Result(property = "stock_code", column = "stockCode"),
    new Result(property = "name", column = "name"),
    new Result(property = "stock_type", column = "stockType"),
    new Result(property = "createtime", column = "createtime"),
    new Result(property = "remark", column = "remark")
  ))
  def select_MA4_MA5_By_Createtime(@Param("createtime") createtime: String): util.List[StockEntity]

  @Insert(Array("INSERT INTO stock(id, stock_code, name, stock_type, createtime) VALUES (#{id}, #{stockCode}, #{name}, #{stockType}, #{createtime})"))
  def insert(stock: StockEntity): Int

  @Delete(Array("DELETE FROM stock WHERE stock_code = #{stock_code} AND stock_type = #{stock_type}"))
  def deleteByCode(@Param("stock_code") stock_code: String,
                   @Param("stock_type") stock_type: String): Int

  @Delete(Array("DELETE FROM stock WHERE id = #{id}"))
  def deleteById(@Param("id") id: String): Int

  @Select(Array("SELECT * FROM stock WHERE stock_code = #{stock_code}"))
  def selectByCode(@Param("stock_code") stock_code: String): util.List[String]

}