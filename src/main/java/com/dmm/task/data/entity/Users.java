package com.dmm.task.data.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString(exclude = "password") // 自動生成されるtoStringにpasswordを出力しない
public class Users {
	@Id
	public String userName;
	public String password;
	public String name;
	public String roleName;
	
	 private Integer id; // IDフィールドを追加
	    
	    public Integer getId() {
	        return id; // idフィールドを返す
	    }
}