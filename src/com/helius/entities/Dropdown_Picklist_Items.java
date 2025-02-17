package com.helius.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Dropdown_Picklist_Items")
public class Dropdown_Picklist_Items {
	
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int drop_down_id;
	
	@Column
	private String drop_down_type;
	
	@Column
	private String country;
	
	@Column
	private String value;
	
	@Column
	private Timestamp create_date;
	
	@Column
	private String created_by;
	
	@Column
	private Timestamp last_modified_date;
	
	@Column
	private String last_modified_by;

	public int getDrop_down_id() {
		return drop_down_id;
	}

	public void setDrop_down_id(int drop_down_id) {
		this.drop_down_id = drop_down_id;
	}

	public String getDrop_down_type() {
		return drop_down_type;
	}

	public void setDrop_down_type(String drop_down_type) {
		this.drop_down_type = drop_down_type;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Timestamp getCreate_date() {
		return create_date;
	}

	public void setCreate_date(Timestamp create_date) {
		this.create_date = create_date;
	}

	public String getCreated_by() {
		return created_by;
	}

	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

	public Timestamp getLast_modified_date() {
		return last_modified_date;
	}

	public void setLast_modified_date(Timestamp last_modified_date) {
		this.last_modified_date = last_modified_date;
	}

	public String getLast_modified_by() {
		return last_modified_by;
	}

	public void setLast_modified_by(String last_modified_by) {
		this.last_modified_by = last_modified_by;
	}
	
	

}