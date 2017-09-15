package com.imudges.web.CloudAddressBook.Module;

import com.imudges.web.CloudAddressBook.Bean.SMSLog;
import com.imudges.web.CloudAddressBook.Bean.User;
import com.imudges.web.CloudAddressBook.Bean.UserAndContacts;
import com.imudges.web.CloudAddressBook.Util.ConfigReader;
import com.imudges.web.CloudAddressBook.Util.SendMessage;
import com.imudges.web.CloudAddressBook.Util.Toolkit;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IocBean
@Fail("http:500")
@Filters(@By(type = AuthorityFilter.class,args = {"ioc:authorityFilter"}))
public class PublicModule {
    @Inject
    Dao dao;

    @Filters
    @At("/login")
    @Ok("json:{locked:'password|id'}")
    @Fail("http:403")
    public Object login(@Param("username")String username,
                        @Param("password")String password,
                        @Param("imei")String imei,
                        @Param("ts")Long ts,
                        HttpServletRequest request){
        boolean loginFlag = true;

        if(ts == null){
            return Toolkit.getFailResult(-1,new ConfigReader().read("-1"),null);
        }

        //请求超时判断，默认时间为120s
//        long sendTime = ts;
//        long nowTime = System.currentTimeMillis();
//        if((nowTime - sendTime > 120 * 1000)){
//            return Toolkit.getFailResult(-2,new ConfigReader().read("-2"),null);
//        }

        //TODO 如果正式上线,需要这条判断
//        if(!sk.equals(MD5.encryptTimeStamp(ts + ""))){
//            resultMap.put("ret",-3);
//            resultMap.put("msg",new ConfigReader().read("-3"));
//            return resultMap;
//        }
        User user = dao.fetch(User.class, Cnd.where("username","=",username).and("password","=",password));
        if(user == null){
            loginFlag = false;
        }

        if(!loginFlag){
            user = dao.fetch(User.class,Cnd.where("phone","=",username).and("password","=",password));
            if(user != null){
                loginFlag = true;
            }
        }

        if(loginFlag){
            user.setAk(Toolkit.getAccessKey());
            dao.update(user);
        }
        if (user == null) {
            return Toolkit.getFailResult(-3,new ConfigReader().read("-3"),null);
        }
        //TODO 参数重放
        return Toolkit.getSuccessResult("登陆成功",user);
    }

    @At("/check_login_status")
    @Ok("json:{locked:'password|id'}")
    @Fail("http:500")
    @Filters
    public Object checkLoginStatus(@Param("ak")String ak){
        boolean resTag = false;
        if(!Toolkit.checkStr(ak,1)){
            return Toolkit.getFailResult(-1,new ConfigReader().read("-1"),null);
        }
        User user = dao.fetch(User.class,Cnd.where("ak","=",ak));
        if(user != null){
            resTag = true;
        } else {
            resTag = false;
        }

        if(resTag){
            return Toolkit.getSuccessResult("登录状态有效",user);
        } else {
            return Toolkit.getFailResult(-4,new ConfigReader().read("-4"),null);
        }
    }

    @At("/register_by_phone")
    @Ok("json:{locked:'password|id'}")
    @Fail("http:500")
    @Filters
    public Object registerByPhone(@Param("phone")String phone,
                                  @Param("code")String code,
                                  @Param("password")String password){
        int checkCode = 0;//-1 验证码已被使用，-2 用户已存在，-3 参数错误
        //判断参数合法性
        if(Toolkit.checkStr(phone,1)
                &&Toolkit.checkStr(code,1)
                &&Toolkit.checkStr(password,1)){
            Date date = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
            SMSLog smsLog = dao.fetch(SMSLog.class,Cnd.where("add_time",">",date)
                    .and("phone","=",phone)
                    .and("used","=",0)
                    .desc("id"));
            if(smsLog == null){
                checkCode = -1;
            } else {
                smsLog.setUsed(1);
                dao.update(smsLog);
            }
        } else {
            checkCode = -3;
        }

        //执行注册流程
        if(checkCode == 0){
            User user = dao.fetch(User.class,Cnd.where("phone","=",phone));
            if(user != null){
                checkCode = -2;
            } else {
                User newUser = new User();
                newUser.setPhone(phone);
                newUser.setPassword(password);
                newUser.setAk(Toolkit.getAccessKey());
                dao.insert(newUser);
                return Toolkit.getSuccessResult("注册成功！",newUser);
            }
        }
        Map<String,Object> result = new HashMap<>();
        switch (checkCode){
            case -1:
                result = Toolkit.getFailResult(-5,new ConfigReader().read("-5"),null);
                break;
            case -2:
                result = Toolkit.getFailResult(-6,new ConfigReader().read("-6"),null);
                break;
            case -3:
                result = Toolkit.getFailResult(-1,new ConfigReader().read("-1"),null);
                break;
            default:
                break;
        }
        return  result;
    }

    @Filters
    @At("/send_sms")
    @Ok("json")
    public Object sendSMS(@Param("phone")String phone,
                          HttpServletRequest request){
        String ip = request.getRemoteAddr();
        boolean checkTag = true;

        Date date = new Date(System.currentTimeMillis() - 60 * 1000);
        //判断一分钟以内此IP有没有发送过，有则拒绝发送
        SMSLog smsLog = dao.fetch(SMSLog.class,Cnd.where("phone","=",phone).and("add_time",">",date));
        if(smsLog != null || phone == null || !Toolkit.isChinaPhoneLegal(phone)){
            checkTag = false;
        }
        //发送验证码
        if(checkTag){
            String checkCode = new SendMessage().sendMessage(phone);
            smsLog = new SMSLog();
            smsLog.setCode(checkCode);
            smsLog.setUsed(0);
            smsLog.setIp(ip);
            smsLog.setAddTime(new Date(System.currentTimeMillis()));
            smsLog.setPhone(phone);
            dao.insert(smsLog);
            return Toolkit.getSuccessResult("发送成功",null);
        } else {
            return Toolkit.getFailResult(-7,new ConfigReader().read("-7"),null);
        }
    }

    @Filters
    @At("/check_sms")
    @Ok("json")
    public Object checkSMS(@Param("phone")String phone,
                           @Param("code")String code){
        boolean checkTag = true;

        Date date = new Date(System.currentTimeMillis() - 10 * 60 * 1000);
        SMSLog smsLog = dao.fetch(SMSLog.class,Cnd.where("add_time",">",date)
                .and("phone","=",phone)
                .and("used","=",0)
                .and("code","=",code)
                .desc("id"));
        if(smsLog == null){
            checkTag = false;
        }
        if(checkTag){
            smsLog.setUsed(1);
            dao.update(smsLog);
            return Toolkit.getSuccessResult("验证码正确",null);
        } else {
            return Toolkit.getFailResult(-5,new ConfigReader().read("-5"),null);
        }
    }

    /**
     * 忘记密码
     * */
    @Filters
    @At("/change_password_by_code")
    @Ok("json")
    public Object changPassword(@Param("phone")String phone,
                                @Param("code")String code,
                                @Param("password")String password){

        boolean checkTag = true;
        Date date = new Date(System.currentTimeMillis() - 10 * 60 * 1000);
        SMSLog smsLog = dao.fetch(SMSLog.class,Cnd.where("add_time",">",date)
                .and("phone","=",phone)
                .and("code","=",code)
                .and("used","=",0)
                .desc("id"));
        if(smsLog == null){
            checkTag = false;
        }
        if(checkTag){
            smsLog.setUsed(1);
            dao.update(smsLog);
            User user = dao.fetch(User.class,Cnd.where("phone","=",phone));
            user.setPassword(password);
            dao.update(user);
            return Toolkit.getSuccessResult("修改成功",null);
        } else {
            return Toolkit.getFailResult(-5,new ConfigReader().read("-5"),null);
        }
    }

    /**
     * 修改密码
     * */
    @At("/change_password")
    @Ok("json")
    @Fail("http:500")
    @Filters
    public Object changePassword(@Param("ak")String ak,
                                 @Param("phone")String phone,
                                 @Param("password")String password,
                                 @Param("oldPassword")String oldPassword){
        boolean checkTag = true;
        User user = dao.fetch(User.class,Cnd.where("ak","=",ak)
                .and("phone","=",phone)
                .and("password","=",oldPassword));
        if(user == null){
            checkTag = false;
        }

        if(checkTag){
            user.setPassword(password);
            dao.update(user);
            return Toolkit.getSuccessResult("修改成功",null);
        } else {
            return Toolkit.getFailResult(-8,new ConfigReader().read("-8"),null);
        }
    }

    /**
     * 添加联系人
     * */
    @At("/add_contacts")
    @Ok("json")
    @Fail("http:500")
    public Object addContacts(@Param("phone")String phone,
                              @Param("name")String name,
                              @Param("address")String address,
                              @Param("remarks")String remarks,
                              HttpSession session){
        NutMap result = null;
        int errorCode = 0;// -1为手机号不合法 -2为姓名为空
        //检查姓名是否合法
        if(!Toolkit.checkStr(name,1)){
            errorCode = -2;
        }
        //检查电话号码是否合法
        if(!Toolkit.isChinaPhoneLegal(phone)){
            errorCode = -1;
        }

        User user = (User) session.getAttribute("user");
        if(user == null){
            return Toolkit.getFailResult(-4,new ConfigReader().read("-4"),null);
        }
        if(errorCode == 0){
            UserAndContacts userAndContacts = new UserAndContacts();
            userAndContacts.setName(name);
            userAndContacts.setAddress(address);
            userAndContacts.setPhone(phone);
            userAndContacts.setRemarks(remarks);
            userAndContacts.setUserId(user.getId() + "");
            dao.insert(userAndContacts);
        }

        switch (errorCode){
            case 0:
                result = Toolkit.getSuccessResult("添加成功",null);
                break;
            case -1:
                result = Toolkit.getFailResult(-9, new ConfigReader().read("-9"),null);
                break;
            case -2:
                result = Toolkit.getFailResult(-10, new ConfigReader().read("-10"),null);
                break;
            default:
                result = Toolkit.getSuccessResult("添加成功",null);
                    break;
        }
        return result;
    }

}
