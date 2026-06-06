package com.interviewcoach.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interviewcoach.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
