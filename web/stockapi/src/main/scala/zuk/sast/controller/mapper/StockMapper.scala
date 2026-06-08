package zuk.sast.controller.mapper

import org.apache.ibatis.annotations._
import zuk.sast.controller.mapper.entity.{StockEntity}
import java.util

@Mapper
trait StockMapper {

  @Select(Array("SELECT * FROM stock"))
  def selectAll(): util.List[StockEntity]

  @Insert(Array("INSERT INTO stock(id, stock_code, name, stock_type) VALUES (#{id}, #{stock_code}, #{name}, #{stock_type})"))
  def insert(stock: StockEntity): Int

  @Delete(Array("DELETE FROM stock WHERE stock_code = #{stock_code}"))
  def deleteByCode(@Param("stock_code") stock_code: String): Int

  @Select(Array("SELECT * FROM stock WHERE stock_code = #{stock_code}"))
  def selectByCode(@Param("stock_code") stock_code: String): util.List[String]

}