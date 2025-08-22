package zuk.sast.rules.controller.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import zuk.sast.rules.controller.mapper.entity.ProjectEntity;

import java.util.List;

@Mapper
public interface ProjectMapper {

    @Insert("INSERT INTO project(name) VALUES(#{name})")
    //@Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProjectEntity project);

    @Select("SELECT * FROM project ORDER BY CREATEDTIME DESC ")
    List<ProjectEntity> selectAll();

}
