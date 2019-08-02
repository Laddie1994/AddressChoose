package com.example.chooselibrary.module;

public class AddressEntity {
    /*** id*/
    public int id;
    /*** 地址编号*/
    public String code;
    /*** 中文名*/
    public String name;
    /*** 父id*/
    public int parentId;

    @Override
    public String toString() {
        return "AddressEntity{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
