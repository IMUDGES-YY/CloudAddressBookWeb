package com.imudges.web.CloudAddressBook.Module;

import com.imudges.web.CloudAddressBook.Bean.User;
import com.imudges.web.CloudAddressBook.Util.ConfigReader;
import com.imudges.web.CloudAddressBook.Util.Toolkit;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.JsonFormat;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.UTF8JsonView;
import org.nutz.mvc.view.ViewWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@IocBean
public class AuthorityFilter implements ActionFilter {
    @Inject
    Dao dao;

    @Override
    public View match(ActionContext actionContext) {
        HttpServletRequest request = actionContext.getRequest();
        HttpSession session = request.getSession();

        String ak = request.getParameter("ak");

        User user = dao.fetch(User.class, Cnd.where("ak","=",ak));

        if(ak == null || user == null){
            return new ViewWrapper(new UTF8JsonView(new JsonFormat(true)), Toolkit.getFailResult(-4, new ConfigReader().read("-4"),null));
        } else {
            session.setAttribute("user",user);
        }
        return null;
    }
}
