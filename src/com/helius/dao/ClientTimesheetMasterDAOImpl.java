package com.helius.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.entities.ClientDetail;
import com.helius.entities.ClientTimesheetMaster;
import com.helius.entities.TimesheetMaster;
import com.helius.utils.FilecopyStatus;
import com.helius.utils.Utils;

public class ClientTimesheetMasterDAOImpl implements IClientTimesheetMasterDAO {

	private List<String> copied_with_success = new ArrayList<String>();

	private org.hibernate.internal.SessionFactoryImpl sessionFactory;

	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(org.hibernate.internal.SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<ClientTimesheetMaster> getAllTimeshhetmaster() throws Throwable {
		// TODO Auto-generated method stub

		Session session = null;
		java.util.List checklist = null;
		ClientTimesheetMaster timesheet_Master = null;
		List<ClientTimesheetMaster> timesheet_Master_List = new ArrayList<ClientTimesheetMaster>();
		try {
			session = sessionFactory.openSession();
			String checklist_query = "select * from Client_Timesheet_Master";
			checklist = session.createSQLQuery(checklist_query).addEntity(ClientTimesheetMaster.class).list();
			if (!checklist.isEmpty()) {
				for (Object employee_checklist : checklist) {
					timesheet_Master = (ClientTimesheetMaster) employee_checklist;
					timesheet_Master_List.add(timesheet_Master);
				}
			}
		} catch (HibernateException e) {
			e.printStackTrace();
			throw new Throwable("Unable to get Client Timesheet Master Data " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Throwable("Unable to get Client Timesheet Master Data " + e.getMessage());
		} finally {
			session.close();
		}
		return timesheet_Master_List;
	}

}
