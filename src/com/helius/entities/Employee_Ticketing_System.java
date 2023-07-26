package com.helius.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Entity
@Table(name = "Employee_Ticketing_System")
//@Audited
public class Employee_Ticketing_System {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	private int employee_ticketing_system_id;

	@Column
	private String employee_id;

	@Column
	private String employee_name;
	@Column
	private String ticket_type;
	@Column
	private String ticket_raised_by;

	@Column
	private String ticket_number;

	@Column
	private String ticket_discription;

	@Column
	private String ticket_assigned_to;

	@Column
	private String ticket_status;

	@Column
	private String ticket_age;

	@Column
	private Timestamp ticket_closure_date;

	@Column
	private String comments;

	@Column
	private Timestamp last_modified_date;

	@Column
	private String last_modified_by;

	@Column
	private Timestamp create_date;

	@Column
	private String created_by;

	@Column
	private String ticket_attachment_path;
	@Column
	private String work_country;

	public int getEmployee_ticketing_system_id() {
		return employee_ticketing_system_id;
	}

	public void setEmployee_ticketing_system_id(int employee_ticketing_system_id) {
		this.employee_ticketing_system_id = employee_ticketing_system_id;
	}

	public String getEmployee_id() {
		return employee_id;
	}

	public void setEmployee_id(String employee_id) {
		this.employee_id = employee_id;
	}

	public String getEmployee_name() {
		return employee_name;
	}

	public void setEmployee_name(String employee_name) {
		this.employee_name = employee_name;
	}

	public String getTicket_type() {
		return ticket_type;
	}

	public void setTicket_type(String ticket_type) {
		this.ticket_type = ticket_type;
	}

	public String getTicket_raised_by() {
		return ticket_raised_by;
	}

	public void setTicket_raised_by(String ticket_raised_by) {
		this.ticket_raised_by = ticket_raised_by;
	}

	public String getTicket_number() {
		return ticket_number;
	}

	public void setTicket_number(String ticket_number) {
		this.ticket_number = ticket_number;
	}

	public String getTicket_discription() {
		return ticket_discription;
	}

	public void setTicket_discription(String ticket_discription) {
		this.ticket_discription = ticket_discription;
	}

	public String getTicket_assigned_to() {
		return ticket_assigned_to;
	}

	public void setTicket_assigned_to(String ticket_assigned_to) {
		this.ticket_assigned_to = ticket_assigned_to;
	}

	public String getTicket_status() {
		return ticket_status;
	}

	public void setTicket_status(String ticket_status) {
		this.ticket_status = ticket_status;
	}

	public String getTicket_age() {
		return ticket_age;
	}

	public void setTicket_age(String ticket_age) {
		this.ticket_age = ticket_age;
	}

	public Timestamp getTicket_closure_date() {
		return ticket_closure_date;
	}

	public void setTicket_closure_date(Timestamp ticket_closure_date) {
		this.ticket_closure_date = ticket_closure_date;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
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

	public String getTicket_attachment_path() {
		return ticket_attachment_path;
	}

	public void setTicket_attachment_path(String ticket_attachment_path) {
		this.ticket_attachment_path = ticket_attachment_path;
	}

	public String getWork_country() {
		return work_country;
	}

	public void setWork_country(String work_country) {
		this.work_country = work_country;
	}

}
