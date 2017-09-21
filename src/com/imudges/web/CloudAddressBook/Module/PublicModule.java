package com.imudges.web.CloudAddressBook.Module;

import com.imudges.web.CloudAddressBook.Bean.*;
import com.imudges.web.CloudAddressBook.Util.ConfigReader;
import com.imudges.web.CloudAddressBook.Util.SendMessage;
import com.imudges.web.CloudAddressBook.Util.Toolkit;
import com.sun.corba.se.impl.oa.toa.TOA;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

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
     * 添加联系人，如果此用户没有该分组，则会自动新建分组
     * */
    @At("/add_contacts")
    @Ok("json")
    @Fail("http:500")
    public Object addContacts(@Param("phone")String phone,
                              @Param("name")String name,
                              @Param("address")String address,
                              @Param("remarks")String remarks,
                              @Param("group")String group,
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

            if(Toolkit.checkStr(group,1)){

                ContactsGroup contactsGroup = dao.fetch(ContactsGroup.class,Cnd.where("userId","=",user.getId()).and("name","=",group));

                if(contactsGroup == null){
                    //新建分组
                    contactsGroup = new ContactsGroup();
                    contactsGroup.setName(group);
                    contactsGroup.setUserId(user.getId() + "");
                    dao.insert(contactsGroup);
                }

                userAndContacts.setGroupId(contactsGroup.getId() + "");
                dao.insert(userAndContacts);
            } else {}
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

    /**
     * 获取分组信息
     * */
    @At("/get_group")
    @Ok("json")
    @Fail("http:500")
    public Object getGroup(HttpSession session){
        User user = (User) session.getAttribute("user");

        List<ContactsGroup> contactsGroups = dao.query(ContactsGroup.class,Cnd.where("userId","=",user.getId()));
        if(contactsGroups.size() == 0){
            return Toolkit.getSuccessResult("没有分组信息",null);
        } else {
            return Toolkit.getSuccessResult("有分组信息",contactsGroups);
        }
    }


    /**
     * 获取联系人（当前用户的所有联系人）
     * */
    @At("/get_contacts")
    @Ok("json")
    @Fail("http:500")
    public Object getContacts(HttpSession session){
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return Toolkit.getFailResult(-4,new ConfigReader().read("-4"),null);
        }

        List<UserAndContacts> list = dao.query(UserAndContacts.class,Cnd.where("userId","=",user.getId()).and("state","=","0"));

        return Toolkit.getSuccessResult("查询成功",list);
    }


    /**
     * 获取某一分组的所有联系人
     * */
    @At("/get_group_contacts")
    @Ok("json")
    @Fail("http:500")
    public Object getGroupContacts(HttpSession session,
                                   @Param("group")String group){
        User user = (User) session.getAttribute("user");


        ContactsGroup contactsGroups = dao.fetch(ContactsGroup.class, Cnd.where("userId","=",user.getId()).and("name","=",group));
        if(contactsGroups == null){
            return Toolkit.getSuccessResult("该用户没有此分组",null);
        }

        List<UserAndContacts> userAndContacts = dao.query(UserAndContacts.class,Cnd.where("groupId","=",contactsGroups.getId()).and("state","=","0"));

        if(userAndContacts == null){
            return Toolkit.getSuccessResult("该分组内没有联系人",null);
        } else {
            return Toolkit.getSuccessResult("查询成功",userAndContacts);
        }
    }

    /**
     * 修改联系人信息
     * */
    @At("/change_contacts")
    @Ok("json")
    @Fail("http:500")
    public Object changeContacts(@Param("phone")String phone,
                                 @Param("new_phone")String newPhone,
                                 @Param("new_name")String name,
                                 @Param("new_address")String newAddress,
                                 @Param("new_remarks")String newRemarks,
                                 HttpSession session){
        //判断参数合法性，手机号和姓名必须有
        if(!Toolkit.checkStr(newPhone,1) || !Toolkit.checkStr(name,1)){
            return Toolkit.getFailResult(-1,new ConfigReader().read("-1"),null);
        }

        //判断手机号合法性
        if(!Toolkit.checkStr(phone,1) || !Toolkit.isChinaPhoneLegal(phone)){
            return Toolkit.getFailResult(-11,new ConfigReader().read("-11"),null);
        }

        User user = (User) session.getAttribute("user");
        if(user == null){
            return Toolkit.getFailResult(-4,new ConfigReader().read("-4"),null);
        }

        //判断是否存在
        UserAndContacts userAndContacts = dao.fetch(UserAndContacts.class,Cnd.where("userId","=",user.getId()).and("phone","=",phone));
        if(userAndContacts != null){
            //修改信息
            userAndContacts.setPhone(newPhone);
            userAndContacts.setName(name);
            userAndContacts.setRemarks(newRemarks);
            userAndContacts.setAddress(newAddress);
            dao.update(userAndContacts);
            return Toolkit.getSuccessResult("修改成功",null);
        } else {
            return Toolkit.getFailResult(-11,new ConfigReader().read("-11"),null);
        }
    }

    //TODO
    /**
     * 修改联系人所在分组
     * */


    /**
     * 删除联系人
     * */
    @At("/delete_contracts")
    @Ok("json")
    @Fail("http:500")
    public Object deleteContracts(HttpSession session,
                                  @Param("phone")String phone){
        User user = (User) session.getAttribute("user");
        if(user == null){
            return Toolkit.getFailResult(-4,new ConfigReader().read("-4"),null);
        }

        //判断手机号合法性
        if(!Toolkit.checkStr(phone,1) || !Toolkit.isChinaPhoneLegal(phone)){
            return Toolkit.getFailResult(-11,new ConfigReader().read("-11"),null);
        }

        UserAndContacts userAndContacts = dao.fetch(UserAndContacts.class,Cnd.where("phone","=",phone).and("state","=",0).desc("id"));
        if(userAndContacts == null){
            return Toolkit.getFailResult(-12,new ConfigReader().read("-12"),null);
        }

        userAndContacts.setState(-1);
        dao.update(userAndContacts);

        return Toolkit.getSuccessResult("删除成功",null);
    }

    /**
     * 新建分组
     * */
    @At("/add_group")
    @Ok("json")
    @Fail("http:500")
    public Object addGroup(@Param("name")String name,
                           @Param("remarks")String remarks,
                           HttpSession session){
        User user = (User) session.getAttribute("user");

        if(!Toolkit.checkStr(name,1)){
            return Toolkit.getFailResult(-1,new ConfigReader().read("-1"),null);
        }

        ContactsGroup c = dao.fetch(ContactsGroup.class,Cnd.where("name","=",name));
        if(c != null){
            return Toolkit.getFailResult(-13,new ConfigReader().read("-13"),null);
        }

        ContactsGroup contactsGroup = new ContactsGroup();
        contactsGroup.setName(name);
        contactsGroup.setRemarks(remarks);
        contactsGroup.setUserId(user.getId() + "");
        dao.insert(contactsGroup);
        return Toolkit.getSuccessResult("新建成功",null);
    }
}
