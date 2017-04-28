/*
 * Copyright &copy; <a href="http://www.zsteel.cc">zsteel</a> All rights reserved.
 */

package com.bdfint.backend.modules.sys.service.impl;

import com.bdfint.backend.framework.common.BaseServiceImpl;
import com.bdfint.backend.framework.util.Encodes;
import com.bdfint.backend.framework.util.StringUtils;
import com.bdfint.backend.modules.sys.bean.User;
import com.bdfint.backend.modules.sys.mapper.UserMapper;
import com.bdfint.backend.modules.sys.service.RoleService;
import com.bdfint.backend.modules.sys.service.UserService;
import com.bdfint.backend.modules.sys.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户service实现类
 *
 * @author lufengcheng
 * @date 2016-01-15 09:53:27
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleService roleService;

    /**
     * 根据登录名查询
     *
     * @param loginName 用户名
     * @return User
     */
    @Override
    public User getByLoginName(String loginName) {
        User user = new User();
        user.setLoginName(loginName);
        user = userMapper.selectOne(user);
        user.setRoleList(roleService.getRoleByUserId(user.getId()));
        return user;
    }


    /**
     * 根据角色ID获取用户信息
     *
     * @param roleId 角色ID
     * @return List<User>
     */
    @Override
    public List<User> getUserByRoleId(String roleId) {
        return userMapper.selectUserByRoleId(roleId);
    }

    /**
     * 更新当前用户信息
     *
     * @param currentUser 当前用户
     */
    @Override
    public void updateUserInfo(User currentUser) throws Exception {
        super.update(currentUser);
    }

    /**
     * 根据机构ID获取用户信息
     *
     * @param officeId 机构ID
     * @return List<User>
     */
    @Override
    public List<User> getUserByOfficeId(String officeId) {
        User user = new User();
        user.setOfficeId(officeId);
        return userMapper.select(user);
    }

    /**
     * 更新用户状态
     */
    @Override
    public int updateStatus(String ids, User user) throws Exception {
        String[] split = ids.split(",");
        for (String id : split) {
            user.setId(id);
            super.update(user);
        }
        return ids.length();
    }

    /**
     * 密码初始化
     * @param ids 需要初始化的id
     * @param password 需要初始化的密码
     * @return
     */
    @Override
    public int initPassword(String ids, String password) throws Exception {
        String entryptPassword = Encodes.encryptPassword(password);
        User user;
        String[] split = ids.split(",");
        for (String id : split) {
            user = new User();
            user.setId(id);
            user.setPassword(entryptPassword);
            super.update(user);
        }
        return ids.length();
    }

    /**
     * 保存用户信息
     *
     * @param object User
     * @throws Exception
     */
    @Override
    public String save(User object) throws Exception {
        if (StringUtils.isNoneEmpty(object.getId())) {
            // 如果新密码为空，则不更换密码
            if (StringUtils.isNotBlank(object.getPassword())) {
                object.setPassword(Encodes.encryptPassword(object.getPassword()));
            } else {
                object.setPassword(null);
            }
            super.update(object);
            //删除用户角色关联
            userMapper.deleteUserRoleByUserId(object.getId());
        } else {
            if (StringUtils.isNotBlank(object.getPassword())) {
                object.setPassword(Encodes.encryptPassword(object.getPassword()));
            }else{
                object.setPassword(Encodes.encryptPassword("123456"));
            }
            object.setId(Encodes.uuid());
            super.insert(object);
        }
        //更新用户角色关联
        userMapper.insertUserRole(object);
        // 清除用户角色缓存
        UserUtils.removeCache(UserUtils.CACHE_ROLE_LIST);
        return object.getId();
    }

    /**
     * 删除用户信息（逻辑删除）
     *
     * @param ids 要删除的ID
     * @throws Exception
     */
    @Override
    public int delete(String ids) throws Exception {
        User user;
        String[] split = ids.split(",");
        for (String id : split) {
            user = new User();
            user.setId(id);
            user.setDelFlag(User.DEL_FLAG_DELETE);
            super.update(user);
        }
        // 清除用户角色缓存
        UserUtils.removeCache(UserUtils.CACHE_ROLE_LIST);
        return ids.length();
    }


}