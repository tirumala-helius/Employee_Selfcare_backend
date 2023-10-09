package com.helius.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "Employee_Ticketing_System_Ticket_Types")
//@Audited
public class Employee_Ticketing_System_Ticket_Types {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int employee_ticketing_system_ticket_types_id;
	@Column
	private String ticket_type;

	@Column
	private String india_helius_email_id;

	@Column
	private String singapore_helius_email_id;

	@Column
	@UpdateTimestamp
	private Timestamp last_modified_date;

	@Column
	private String last_modified_by;

	@Column
	@CreationTimestamp
	private Timestamp create_date;

	@Column
	private String created_by;
	
	@Column
	private String india_spoc_name;
	
	@Column
	private String singapore_spoc_name;
	
	@Column
	private String cc_helius_email_id;
	
	@Column
	private String ticket_query;
	
	/*@Column
	private String india_spoc;
	
	@Column
	private String singapore_spoc;*/

	public String getTicket_query() {
		return ticket_query;
	}

	public void setTicket_query(String ticket_query) {
		this.ticket_query = ticket_query;
	}

	public String getCc_helius_email_id() {
		return cc_helius_email_id;
	}

	public void setCc_helius_email_id(String cc_helius_email_id) {
		this.cc_helius_email_id = cc_helius_email_id;
	}

	public String getIndia_spoc_name() {
		return india_spoc_name;
	}

	public void setIndia_spoc_name(String india_spoc_name) {
		this.india_spoc_name = india_spoc_name;
	}

	public String getSingapore_spoc_name() {
		return singapore_spoc_name;
	}

	public void setSingapore_spoc_name(String singapore_spoc_name) {
		this.singapore_spoc_name = singapore_spoc_name;
	}

	public int getEmployee_ticketing_system_ticket_types_id() {
		return employee_ticketing_system_ticket_types_id;
	}

	public void setEmployee_ticketing_system_ticket_types_id(int employee_ticketing_system_ticket_types_id) {
		this.employee_ticketing_system_ticket_types_id = employee_ticketing_system_ticket_types_id;
	}

	public String getTicket_type() {
		return ticket_type;
	}

	public void setTicket_type(String ticket_type) {
		this.ticket_type = ticket_type;
	}

	public String getIndia_helius_email_id() {
		return india_helius_email_id;
	}

	public void setIndia_helius_email_id(String india_helius_email_id) {
		this.india_helius_email_id = india_helius_email_id;
	}

	public String getSingapore_helius_email_id() {
		return singapore_helius_email_id;
	}

	public void setSingapore_helius_email_id(String singapore_helius_email_id) {
		this.singapore_helius_email_id = singapore_helius_email_id;
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

}
