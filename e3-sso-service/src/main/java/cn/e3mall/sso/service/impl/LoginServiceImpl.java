package cn.e3mall.sso.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbUserMapper;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.pojo.TbUserExample;
import cn.e3mall.pojo.TbUserExample.Criteria;
import cn.e3mall.sso.service.LoginService;

@Service
public class LoginServiceImpl implements LoginService {
	@Value("${SESSION_EXPIRE}")
	private Integer SESSION_EXPIRE;
	@Autowired
	private TbUserMapper tbUserMapper;
	@Autowired
	private JedisClient jedisClient;

	@Override
	public E3Result login(String username, String password) {
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		criteria.andPasswordEqualTo(DigestUtils.md5DigestAsHex(password.getBytes()));
		List<TbUser> list = tbUserMapper.selectByExample(example);
		if (list == null || list.isEmpty()) {
			return E3Result.build(400, "用户名或密码错误");
		}

		// 登陆成功 取用户信息
		TbUser user = list.get(0);
		user.setPassword(password);
		// 生成Token
		String token = UUID.randomUUID().toString();
		// 把token:user写入redis数据库
		// 写入缓存
		try {
			jedisClient.set("SESSION:"+token, JsonUtils.objectToJson(user));
			// 设置过期时间
			jedisClient.expire("SESSION:"+token, SESSION_EXPIRE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 返回token
		return E3Result.ok(token);
	}

}
