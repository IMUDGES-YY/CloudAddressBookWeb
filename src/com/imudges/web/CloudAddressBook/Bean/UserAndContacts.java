package com.imudges.web.CloudAddressBook.Bean;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("user_and_contacts")
public class UserAndContacts {

    @Id
    private int Id;

    @Column
    private String name;

    @Column
    private String phone;//电话号

    @Column
    private String userId;//用户id

    @Column
    private String address;//地址

    @Column
    private String remarks;//备注

    @Column("contacts_group")
    private String group;//群组

    @Column
    private int state;//状态 0 正常，-1 被删除

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
