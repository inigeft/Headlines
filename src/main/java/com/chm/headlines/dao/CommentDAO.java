package com.chm.headlines.dao;

import com.chm.headlines.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper     //注解勿漏！！
public interface CommentDAO {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " user_id, content, entity_type, entity_id, created_date, status ";
    String SELECT_FIELDS = "id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS, ")",
            "values (#{userId}, #{content}, #{entityType}, #{entityId}, #{createdDate}, #{status})"})
    int addComment(Comment comment);    //返回值为int，因为插入评论成功/失败会返回不同的int(0/1),，且成功后可能返回插入成功的评论id

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where entity_type=#{entityType} and entity_id=#{entityId} order by id desc"})
    List<Comment> selectByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    @Select({"select count(id) from ", TABLE_NAME, " where entity_type=#{entityType} and entity_id=#{entityId}"})
    //即统计该实体类型中的该实体id对应的实体下的评论总数（如资讯类中id=15的资讯实体对应的评论的总数）
    int getCommentCount(@Param("entityType") int entityType, @Param("entityId") int entityId);

    @Update({"update", TABLE_NAME, "set status = #{status} where entity_type=#{entityType} and entity_id=#{entityId}"})
    void updateStates(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("status") int status);
}
