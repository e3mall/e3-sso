package cn.e3mall.sso.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.sso.service.TokenService;

@Service
public class TokenServiceImpl implements TokenService {
	@Autowired
	private JedisClient jedisClient;

	@Override
	public E3Result getUserByToken(String token) {
		// 从redis集群中获取用户信息
		String string = jedisClient.get("SESSION:" + token);
		if (StringUtils.isBlank(string)) {// 如果无用户信息 返回失败
			return E3Result.build(201, "用户登陆已经过期");
		}
		// 如果有用户信息 更新token的过期时间
		jedisClient.expire("SESSION:" + token, 1800);
		// 返回用户信息
		TbUser user = JsonUtils.jsonToPojo(string, TbUser.class);
		return E3Result.ok(user);
	}

}
