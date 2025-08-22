package zuk.sast.rules.controller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import zuk.sast.rules.controller.mapper.entity.Test;

import java.util.List;

@Mapper
public interface TestMapper {

//    @Insert("INSERT INTO user(name, email) VALUES(#{name}, #{email})")
//    @Options(useGeneratedKeys = true, keyProperty = "id")
//    int insert(User user);
//
//    @Select("SELECT * FROM user WHERE id = #{id}")
//    User selectById(Long id);
//
//    @Update("UPDATE user SET name=#{name}, email=#{email} WHERE id=#{id}")
//    int update(User user);
//
//    @Delete("DELETE FROM user WHERE id = #{id}")
//    int deleteById(Long id);

    @Select("SELECT * FROM test")
    List<Test> selectAll();
}
