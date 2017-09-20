package com.imudges.web.CloudAddressBook.Bean;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 联系人和分组
 * */
@Table("user_and_group")
public class UserAndGroup {

    @Id
    private int Id;

    @Column("contacts_id")
    private String contactsId;

    @Column("group_id")
    private String groupId;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getContactsId() {
        return contactsId;
    }

    public void setContactsId(String contactsId) {
        this.contactsId = contactsId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
