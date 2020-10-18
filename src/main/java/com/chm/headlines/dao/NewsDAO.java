package com.chm.headlines.dao;

import com.chm.headlines.model.News;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NewsDAO {

    String TABLE_NAME = "news";

    String INSERT_FIELDS = " title, link, image, like_count, comment_count,created_date,user_id ";

    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    //上面INSERT_FIELDS中变量名要符合数据库中的规范（如like_count），而下面@insert注解中变量名则要符合Java代码的规范（如likeCount)
    @Insert({"Insert into", TABLE_NAME, "(", INSERT_FIELDS, ") values (#{title}, #{link}, #{image}, #{likeCount}, #{commentCount}, #{createdDate}, #{userId})"})
    int addNews(News news);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    News getById(int id);

    @Update({"update", TABLE_NAME, " set comment_count  = #{commentCount} where id = #{id}"})
        //作为SQL语句的代码中的变量名要符合SQL的变量名命名规范（如comment_count)，而作为Java代码中的变量名则要符合Java变量名命名规范（如commentCount)
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    @Update({"update", TABLE_NAME, " set like_count  = #{likeCount} where id = #{id}"})
    int updateLikeCount(@Param("id") int id, @Param("likeCount") int likeCount);

    List<News> selectByUserIdAndOffset(@Param("userId") int userId,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);


}
