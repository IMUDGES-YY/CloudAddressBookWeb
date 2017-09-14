package com.imudges.web.CloudAddressBook;

import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.Ioc;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

public class MainSetup implements Setup{
    public static Ioc ioc;
    @Override
    public void init(NutConfig nutConfig) {
        MainSetup.ioc = nutConfig.getIoc();
        Dao dao = ioc.get(Dao.class);
        Daos.createTablesInPackage(dao,"com.imudges.web.CloudAddressBook",false);



//        //添加一个管理员
//        if (dao.count(User.class) == 0) {
//            User user = new User();
//            user.setEmail("yangyang@imudges.com");
//            user.setPassword("123");
//            user.setSex(1);
//            user.setPhoneNum("18647705052");
//            dao.insert(user);
//        }
    }

    @Override
    public void destroy(NutConfig nutConfig) {

    }
}
