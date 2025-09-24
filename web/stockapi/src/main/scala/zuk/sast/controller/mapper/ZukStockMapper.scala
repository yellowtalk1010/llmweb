package zuk.sast.controller.mapper

import org.apache.ibatis.annotations._
import zuk.sast.controller.mapper.entity.{StockEntity}
import java.util

@Mapper
trait ZukStockMapper {

  @Select(Array("SELECT * FROM stock"))
  def selectAll(): util.List[StockEntity]

  @Insert(Array("INSERT INTO stock(id, code, jys, name) VALUES (#{id}, #{code}, #{jys}, #{name})"))
  def insert(stock: StockEntity): Int

  @Delete(Array("DELETE FROM stock WHERE code = #{code}"))
  def deleteByCode(@Param("code") code: String): Int

  @Select(Array("SELECT * FROM stock WHERE code = #{code}"))
  def selectByCode(@Param("code") code: String): util.List[String]

}