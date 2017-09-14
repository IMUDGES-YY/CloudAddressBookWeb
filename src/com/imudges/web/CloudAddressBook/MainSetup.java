package com.imudges.web.CloudAddressBook;

import com.imudges.web.CloudAddressBook.Bean.User;
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



        //添加一个用户
        if (dao.count(User.class) == 0) {
            User user = new User();
            user.setPassword("123");
            user.setPhone("18647705052");
            user.setUsername("admin");
            dao.insert(user);
        }
    }

    @Override
    public void destroy(NutConfig nutConfig) {

    }
}
