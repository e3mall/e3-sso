package cn.e3mall.sso.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.mapper.TbUserMapper;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.pojo.TbUserExample;
import cn.e3mall.pojo.TbUserExample.Criteria;
import cn.e3mall.sso.service.RegisterService;

@Service
public class RegisterServiceImpl implements RegisterService {
	@Autowired
	private TbUserMapper tbUserMapper;
	
	@Override
	public E3Result checkData(String param, int type) {
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		//根据不同的type生成不同的查询条件
		if(type == 1){
			criteria.andUsernameEqualTo(param);
		}else if(type == 2){
			criteria.andPhoneEqualTo(param);
		}else if(type == 3){
			criteria.andEmailEqualTo(param);
		}else{
			return E3Result.build(400, "数据类型错误");
		}
		List<TbUser> list = tbUserMapper.selectByExample(example);
		if(list == null || list.isEmpty()){
			return E3Result.ok(true);
		}
		return E3Result.ok(false);
	}

	@Override
	public E3Result register(TbUser user) {
		// 数据校验
		if("".equals(user.getUsername()) || "".equals(user.getPassword()) || "".equals(user.getPhone())){
			return E3Result.build(400, "用户数据不完整,注册失败");
		}
		
		user.setCreated(new Date());
		user.setUpdated(new Date());
		//对密码进行md5加密
		String md5Pass = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
		user.setPassword(md5Pass);
		tbUserMapper.insert(user); 
		
		return E3Result.ok();
	}

}
