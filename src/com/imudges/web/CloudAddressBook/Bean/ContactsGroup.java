package com.imudges.web.CloudAddressBook.Bean;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 分组
 * */
@Table("contacts_group")
public class ContactsGroup {
    @Id
    private int Id;

    @Column
    private String name;//分组名

    @Column("contacts_id")
    private String contactsId;//联系人id

    @Column("user_id")
    private String userId;//对应用户的id

    @Column
    private String remarks;//备注

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactsId() {
        return contactsId;
    }

    public void setContactsId(String contactsId) {
        this.contactsId = contactsId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
