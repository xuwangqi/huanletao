package cn.huanletao.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huanletao.common.pojo.User;
import com.huanletao.common.utils.CookieUtils;
import com.huanletao.common.vo.EasyUIResult;
import com.huanletao.common.vo.SysResult;
import com.netflix.eureka.cluster.HttpReplicationClient;

import ch.qos.logback.core.joran.conditional.ElseAction;
import cn.huanletao.service.UserService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

@RestController
@RequestMapping("/user/manage")
public class UserController {
	@Autowired
	private UserService userService;

//	@RequestMapping("cluster")
//	public void setAndGet(String key,String value){
//	cluster.set(key, value);
//	System.out.println(cluster.get(key)); 
//	}
	//查找用户名判断用户名是否存在能否注册
	@RequestMapping("/checkUserName")
	public SysResult checkUsername(String userName)
	{
		
		int exists=userService.checkUserName(userName); 
		if(exists==0)//用户名不存在在数据库
		{
			return SysResult.ok();
		}
		else //用户名存在在数据库
		{
			return SysResult.build(201, "用户名不可用", null);
		}
	}
	//查找用户名判断手机号是否存在能否注册
	@RequestMapping("/checkUserPhone")
	public SysResult checkUserPhone(String userPhone)
	{
		System.out.println("手机号验证进入了");
		int exists=userService.checkUserPhone(userPhone); 
		if(exists==0)//用户名不存在在数据库
		{
			return SysResult.ok();
		}
		else //用户名存在在数据库
		{
			return SysResult.build(201, "手机号不可用", null);
		}
	}
    //手机获取验证码
    /**
     * 获取验证码方法---发送验证码
     * @param userPhone 手机号
     * @return    应该是返回验证码是否发送成功做后台处理
     */
    @RequestMapping("getPhoneYANzhengma")
    public SysResult getPhoneYANzhengma(String userPhone)
    {
       int isSendSeccuss= userService.getPhoneYANzhengma(userPhone);
        if(isSendSeccuss==1) 
        {
        	return  SysResult.build(200, "验证码发送成功", "");
        }
        else 
        {
			return SysResult.build(500, "验证码发送失败，估计没钱了", "");
		}
    }
    /**
     * 注册
     * @param userPhone
     * @return
     */
    @RequestMapping("register")
    public SysResult register(User user,String yanzhengma)
    {
    	int is_success= userService.isRegisterSuccess(user,yanzhengma);
    	if(   is_success==1)
    	{
    		return SysResult.build(201, "验证码不正确", "");
    	}
    	else if(  is_success==2)
    	{
    		return SysResult.build(500, "数据插入失败", "");
    	}
    	else if(is_success==3)
    	{
    		return SysResult.build(403, "先获取到注册码或时间超时再次获取验证码", "");
    	}
    	else
    	 return SysResult.build(200, "注册成功", "");
    }
    //登录
    //插入数据用的
    //public void updateUser(long userId, String time)
 /*   @RequestMapping("updateUser")
    public void updateUser(Long userId,String time)
    {
    	userService.updateUser(userId,time); //记录到日活跃量和总活跃量
    }*/
    /**
     * 用户名账号登录
     * @param userName
     * @param userPassword
     * @return
     */
    @RequestMapping("userPsLogin")
    public SysResult userPsLogin(String userName,String userPassword,HttpServletRequest req,HttpServletResponse res)
    {
    	User exitUser= userService.checkUsernameAndPassword(userName,userPassword);
    	if(   exitUser!=null) //能返回查询用户代表用户信息正确
    	{
    		//将用户信息存放到redis上，返回的是一个String 类型的ticket 将它放置在Cookie中返回方便下次登录
            String ticket=userService.loginInfoSaveInRedis(exitUser);
            //判断失败和成功
            if("".equals(ticket)) {
                 //没有正确生成redis的key，说明登录失败
                 return SysResult.build(201, "没有生成对应的登录ticket", null);
            }else
            {
            	userService.updateUser(exitUser.getID(),""); //记录到日活跃量和总活跃量
            	
                 //正确存储了数据到redis并且登录成功
            	 Date lastLoginTime=new Date();
            	 userService.setLastLoginTime(exitUser.getUserId(),lastLoginTime);
                 //cookie存储一个值，EM_TICKET
                 CookieUtils.setCookie(req,  res,"EASYBUY_TICKET",ticket);
                 return SysResult.build(200, "登录成功", "");
            }
    		
    	}
    	else
    	{
    		return SysResult.build(401, "用户名或密码错误", "");
    	}
    	
    }
  //登录状态获取---就是第二次登录的时候获取redis中的属性值 通过文档就绪事件访问
    @RequestMapping("query/{ticket}")
    public SysResult queryTicket(@PathVariable
    String ticket){
    System.out.println(ticket);
    String userJson=userService.queryUserJson(ticket);
    if(userJson==""){//超时30分钟
    return SysResult.build(201, "用户超时，重新登录", null);
    }else{
    //登录状态可用
    return SysResult.build(200,
    "登录状态可用", userJson);
    }
    }
    @RequestMapping("userPhoneLogin")
    public SysResult userPhoneLogin(String userPhone,String yanzhengma,HttpServletRequest req,HttpServletResponse res)
    {
    	int is_success= userService.isPhoneLoginSuccess(userPhone,yanzhengma, req, res);
    	if(   is_success==1)
    	{
    		return SysResult.build(401, "验证码不正确", "");
    	}
    	else if(is_success==2)
    	{
    		return SysResult.build(402, "号码未注册", "");
    	}
    	else if(is_success==3)
    	{
    		return SysResult.build(403, "先获取到注册码或时间超时再次获取验证码", "");
    	}
    	else
    	{
    		
    		return SysResult.build(200, "登录成功", "");
    	}
    	 
    }
    
    //登出 ---删除userid.lok 和ticket
    @RequestMapping("logout")
    public SysResult logout(String ticket,HttpServletRequest req,HttpServletResponse res)
    {
    	int is_success= userService.outLogin(ticket);
    	if(   is_success==0)
    	{
    		 CookieUtils.deleteCookie(req,  res,"EASYBUY_TICKET");
    		return SysResult.build(200, "退出成功", "");
    	}
    
    	else
    	 return SysResult.build(201, "退出失败", "");
    }
    
   //用户登录总人数监测
    @RequestMapping("getTotalUserCount")
    public long getTotalUserCount()
    {
    	
    	 return userService.getTotalUserCount();
    };
    
    //当天用户的登录人数
    @RequestMapping("getActiveUserCountInOneDay")
    public Long getActiveUserCountInOneDay(String time)
    {
    	return userService.getActiveInOneDayCount(time);
    };
    //返回在那天登录用户的信息
    @RequestMapping("getActiveInOneDay")
    public SysResult getActiveInOneDay(String time)
    {
    	String barry= userService.getActiveInOneDay(time);
    
    	if(barry==null)
    	{
    		return SysResult.build(201,"没有这个时间key", "");
    	}
    	List<User>data=userService.getUserByIDs(barry);
    	 return SysResult.build(200, "成功", data);
    };
    //一周连续登录的用户的人数
    @RequestMapping("getActiveUserCountByDay")
    public Long  getActiveUserCountByDay(int daynum)
    {
    	return  userService.getActiveUserCount(daynum);
    }
  //一周连续登录的用户List
    @RequestMapping("getActiveUserByDay")
    public SysResult  getActiveUserByDay(int daynum)
    {
    	long count=userService.getActiveUserCount(daynum);
    	String LastDaybearry= userService.getActiveUserByDay(daynum);
        
    	if(LastDaybearry==null)
    	{
    		return SysResult.build(201,"没有这个时间key", "");
    	}
    	List<User>data=userService.getUserByIDs(LastDaybearry);
    	 return SysResult.build(200, "成功", data);
    }
    
    
    //后台代码
    //分页显示所有用户
    @RequestMapping("queryUserByPage")
    public EasyUIResult  queryUserByPage(Integer page,Integer rows)
    {
    EasyUIResult result= userService.queryUserByPage(page, rows);
        return result;
    }
}
