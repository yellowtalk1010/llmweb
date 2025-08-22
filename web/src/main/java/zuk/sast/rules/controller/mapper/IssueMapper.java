package zuk.sast.rules.controller.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import zuk.sast.rules.controller.mapper.entity.IssueEntity;
import zuk.sast.rules.controller.mapper.entity.ProjectEntity;

import java.util.List;

@Mapper
public interface IssueMapper {

    @Insert("INSERT INTO issue(id, project_id, num, content) VALUES(#{id}, #{projectId}, #{num}, #{content})")
    int insert(IssueEntity issue);

    @Select("SELECT * FROM issue ORDER BY num ")
    List<ProjectEntity> selectAll();

    @Select("SELECT count(*) as num FROM issue where project_id = #{projectId}")
    long selectProjectCount(@Param("projectId") String projectId);

}
