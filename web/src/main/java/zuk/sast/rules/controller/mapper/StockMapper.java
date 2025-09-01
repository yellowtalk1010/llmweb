package zuk.sast.rules.controller.mapper;

import org.apache.ibatis.annotations.*;
import zuk.sast.rules.controller.mapper.entity.IssueEntity;
import zuk.sast.rules.controller.mapper.entity.StockEntity;

import java.util.List;

@Mapper
public interface StockMapper {

    @Select("SELECT * FROM stock ")
    List<StockEntity> selectAll();

    @Insert("INSERT INTO stock(id, code, jys, name, type) VALUES (#{id}, #{code}, #{jys}, #{name}, #{type}) ")
    int insert(StockEntity stock);

    @Delete("DELETE FROM stock WHERE code = #{code} ")
    int deleteByCode(@Param("code") String code);

    @Select("SELECT * FROM stock WHERE code = ${code} ")
    List<String> selectByCode(@Param("code") String code);

}
