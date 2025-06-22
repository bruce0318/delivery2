package com.example.blindclient.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class Guardian {
    @JSONField(name = "id", ordinal = 1)
    private Integer id;

    @JSONField(name = "name", ordinal = 2)
    private String name;

    @JSONField(name = "username", ordinal = 3)
    private String userName; // telephone number

    @JSONField(name = "role", ordinal = 4)
    private String role = null;

    @JSONField(name = "userrole", ordinal = 5)
    private String userRole = "guardian";

    @JSONField(name = "password", ordinal = 6)
    private String password;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
