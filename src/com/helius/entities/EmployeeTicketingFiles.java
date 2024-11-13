package com.helius.entities;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "EmployeeTicketingFiles")
public class EmployeeTicketingFiles {
	
	@Id 
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int File_id;
	@Column
	private String ticket_number;
	@Column
	private String ticket_path;
	@Column
	@CreationTimestamp
	private Timestamp created_date;
	@Column
	private String created_by;
	@Column
	private String last_modified_by;
	@Column 
	@UpdateTimestamp
	private Timestamp last_modified_date;
	
	@Column
	private String employee_id;
	
	public String getEmployee_id() {
		return employee_id;
	}
	public void setEmployee_id(String employee_id) {
		this.employee_id = employee_id;
	}
	public int getFile_id() {
		return File_id;
	}
	public void setFile_id(int file_id) {
		File_id = file_id;
	}
	
	public String getTicket_number() {
		return ticket_number;
	}
	public void setTicket_number(String ticket_number) {
		this.ticket_number = ticket_number;
	}
	public String getTicket_path() {
		return ticket_path;
	}
	public void setTicket_path(String ticket_path) {
		this.ticket_path = ticket_path;
	}
	public Timestamp getCreated_date() {
		return created_date;
	}
	public void setCreated_date(Timestamp created_date) {
		this.created_date = created_date;
	}
	public String getCreated_by() {
		return created_by;
	}
	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}
	public String getLast_modified_by() {
		return last_modified_by;
	}
	public void setLast_modified_by(String last_modified_by) {
		this.last_modified_by = last_modified_by;
	}
	public Timestamp getLast_modified_date() {
		return last_modified_date;
	}
	public void setLast_modified_date(Timestamp last_modified_date) {
		this.last_modified_date = last_modified_date;
	}

}
