package com.helius.dao;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jpa.criteria.expression.function.CurrentDateFunction;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee_Personal_Details;
import com.helius.entities.Leave_Record_Details;
import com.helius.entities.Leave_Usage_Details;
import com.helius.entities.Timesheet_Automation_Status;
import com.helius.service.EmailService;
import com.helius.utils.ClientDetails;
import com.helius.utils.FilecopyStatus;
import com.helius.utils.LeaveDetails;
import com.helius.utils.RotationalWeekEnds;
import com.helius.utils.TimesheetAutomation;
import com.helius.utils.TimesheetAutomationHolidays;
import com.helius.utils.Utils;
import com.helius.utils.WorkedOnShifts;
import com.helius.utils.WorkingOnPublicHolidays;
import com.helius.utils.WorkingOnWeekEnds;
import com.helius.entities.Employee_Assignment_Details;

public class AutomationTimesheetDAOImpl implements AutomationTimesheetDAO {

	private org.hibernate.internal.SessionFactoryImpl sessionFactory;

	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(org.hibernate.internal.SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Autowired
	private EmailService service;

	InputStream is = null;
	String extension = null;
	private String awsCheck = Utils.awsCheckFlag();
	int total_days_count =0;
	double present_days= 0;
	double total_leave_days= 0;

	private void readinvoice_template() {

		String url = Utils.getProperty("fileLocation") + File.separator + "DAH2timesheet_details" + File.separator
				+ "DAH2" + "_" + "AutomationTimesheet.xlsx";
		
/*		
		 String url = "C:" + File.separator + "Wildfly" + File.separator +
                 "wildfly_10.0.1" + File.separator +
                 "wildfly-10.1.0.Final (1)" + File.separator +
                 "wildfly-10.1.0.Final" + File.separator +
                 "conf" + File.separator + "DAH2" + "_" + "AutomationTimesheet.xlsx";*/
                 
		// Url Path
	//	C:Wildflywildfly_10.0.1wildfly-10.1.0.Final (1)wildfly-10.1.0.Finalconf\hapTesting\DAH2timesheet_details\DAH2_AutomationTimesheet.xlsx
		//Conf path
		// C:\\wildfly-10.1.0.Final\\wildfly-10.1.0.Final\\conf
		File file = null;
		InputStream fi = null;
		byte[] invoice_bytes = null;
		try {
			file = new File(url);

			if (file.exists()) {
				fi = new FileInputStream(url);
				invoice_bytes = IOUtils.toByteArray(fi);

				is = new ByteArrayInputStream(invoice_bytes);
				if (is.markSupported()) {
					is.mark(0);
				}
			}
			extension = FilenameUtils.getExtension(file.getName());
		} catch (Throwable e) {
			e.printStackTrace();

		} finally {
			try {
				fi.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private XSSFWorkbook getXSSFWorkbook() {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			is = null;
		}

		if (is == null) {
			readinvoice_template();
		} else {
			try {
				is.reset();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		XSSFWorkbook workbookinput = null;

		if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xlsm")) {
			try {
				workbookinput = new XSSFWorkbook(is);
			} catch (IOException e) {

				e.printStackTrace();
			}
			/*
			 * XSSFWorkbook workbookoutput = workbookinput; int countSheet =
			 * workbookoutput.getNumberOfSheets(); int i = 0; for (i = 0; i < countSheet;
			 * i++) { XSSFSheet sh = workbookoutput.getSheetAt(i); sh.setFitToPage(true); //
			 * sh.setAutobreaks(true); PrintSetup ps = sh.getPrintSetup();
			 * ps.setFitWidth((short) 1); ps.setFitHeight((short) 0); }
			 * 
			 * FileOutputStream out = new FileOutputStream(url,false);
			 * workbookoutput.write(out); workbookinput.close(); out.close();
			 */
		}

		return workbookinput;
	}

	@Override
	public ResponseEntity<byte[]> createAutomationTimesheet(String clientjson, MultipartHttpServletRequest request)
			throws Throwable, JsonProcessingException {
		// String check = Utils.awsCheckFlag();
		Session session = null;
		Transaction transaction = null;

		SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
		ResponseEntity<byte[]> responseEntity = null;
		ObjectMapper obm = new ObjectMapper();
		try {

			TimesheetAutomation automation = obm.readValue(clientjson, TimesheetAutomation.class);
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			String empid = automation.getEmpId();
			String client = automation.getClient();
			if(client.equalsIgnoreCase("BANK OF JULIUS BAER")) {
				client = "BANK JULIUS BAER";
			}
			String empname = automation.getEmployeeName();
			String workCountry = automation.getWorkCountry();
			String managername = "N/A";

			if (automation.getReportingManagerName() != null && !automation.getReportingManagerName().isEmpty()) {
				managername = automation.getReportingManagerName();
			}

			Date selectedMonth = sdfMonth.parse(automation.getLeaveMonth().toString());
			SimpleDateFormat sdfMonthYear = new SimpleDateFormat("MMM-yy", Locale.US);
			String monthYearString = sdfMonthYear.format(selectedMonth);
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

			// This Commented part will create an XSSFWorkbook for DAH2 with some constant
			// data, Then file will be saved,
			// WE can reuse the same XSSFWorkbook for all DAH2 client, instated of creating
			// new XSSFWorkbook for every employee.

			/*
			 * 
			 * // create a new Excel workbook Workbook workbook1 = new XSSFWorkbook();
			 * 
			 * // create a new sheet in the workbook Sheet sheet1 =
			 * workbook1.createSheet("TimeSheet");
			 * 
			 * // create the header row Row headerRow = sheet1.createRow(0);
			 * 
			 * Row headerRow1 = sheet1.createRow(1);
			 * 
			 * // create the TIME SHEET column Cell headerCell1 = headerRow.createCell(1);
			 * headerCell1.setCellValue("TIME SHEET"); sheet1.addMergedRegion(new
			 * CellRangeAddress(0, 0, 1, 9)); CellRangeAddress mergedRange1 =
			 * sheet1.getMergedRegion(0); for (int r = mergedRange1.getFirstRow(); r <=
			 * mergedRange1.getLastRow(); r++) { Row row = sheet1.getRow(r); if (row ==
			 * null) { row = sheet1.createRow(r); } for (int c =
			 * mergedRange1.getFirstColumn(); c <= mergedRange1.getLastColumn(); c++) { Cell
			 * cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle9(workbook1)); } }
			 * 
			 * // CellStyle headerCellStyle = getHeaderCellStyle(workbook); Cell headerCell2
			 * = headerRow1.createCell(1); headerCell2.setCellValue("EMP_ID"); //
			 * headerCell2.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(1, 1, 1, 2)); CellRangeAddress
			 * mergedRange2 = sheet1.getMergedRegion(1); for (int r =
			 * mergedRange2.getFirstRow(); r <= mergedRange2.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange2.getFirstColumn(); c <= mergedRange2.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * 
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Cell headerCell31 = headerRow1.createCell(3);
			 * headerCell31.setCellValue(empid);
			 * //headerCell3.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(1, 1, 3, 9)); CellRangeAddress
			 * mergedRange3 = sheet1.getMergedRegion(2); for (int r =
			 * mergedRange3.getFirstRow(); r <= mergedRange3.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange3.getFirstColumn(); c <= mergedRange3.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Row headerRow2 = sheet1.createRow(2); // CellStyle
			 * headerCellStyle=getHeaderCellStyle(workbook); Cell headerCell4 =
			 * headerRow2.createCell(1); headerCell4.setCellValue("EMP_NAME"); //
			 * headerCell4.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(2, 2, 1, 2)); CellRangeAddress
			 * mergedRange4 = sheet1.getMergedRegion(3); for (int r =
			 * mergedRange4.getFirstRow(); r <= mergedRange4.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange4.getFirstColumn(); c <= mergedRange4.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Cell headerCell51 = headerRow2.createCell(3);
			 * headerCell51.setCellValue(empname);
			 * //headerCell5.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(2, 2, 3, 9)); CellRangeAddress
			 * mergedRange5 = sheet1.getMergedRegion(4); for (int r =
			 * mergedRange5.getFirstRow(); r <= mergedRange5.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange5.getFirstColumn(); c <= mergedRange5.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Row headerRow3 = sheet1.createRow(3); // CellStyle headerCellStyle
			 * =getHeaderCellStyle(workbook); Cell headerCell6 = headerRow3.createCell(1);
			 * headerCell6.setCellValue("Client");
			 * //headerCell6.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(3, 3, 1, 2)); CellRangeAddress
			 * mergedRange6 = sheet1.getMergedRegion(5); for (int r =
			 * mergedRange6.getFirstRow(); r <= mergedRange6.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange6.getFirstColumn(); c <= mergedRange6.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Cell headerCell71 = headerRow3.createCell(3);
			 * headerCell71.setCellValue(client);
			 * //headerCell7.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(3, 3, 3, 9)); CellRangeAddress
			 * mergedRange7 = sheet1.getMergedRegion(6); for (int r =
			 * mergedRange7.getFirstRow(); r <= mergedRange7.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange7.getFirstColumn(); c <= mergedRange7.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Row headerRow4 = sheet1.createRow(4); // CellStyle headerCellStyle =
			 * getHeaderCellStyle(workbook); Cell headerCell8 = headerRow4.createCell(1);
			 * headerCell8.setCellValue("Manager"); //
			 * headerCell8.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(4, 4, 1, 2)); CellRangeAddress
			 * mergedRange8 = sheet1.getMergedRegion(7); for (int r =
			 * mergedRange8.getFirstRow(); r <= mergedRange8.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange8.getFirstColumn(); c <= mergedRange8.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Cell headerCell91 = headerRow4.createCell(3);
			 * headerCell91.setCellValue(managername); //
			 * headerCell9.setCellStyle(getHeaderCellStyle(workbook));
			 * sheet1.addMergedRegion(new CellRangeAddress(4, 4, 3, 9)); CellRangeAddress
			 * mergedRange9 = sheet1.getMergedRegion(8); for (int r =
			 * mergedRange9.getFirstRow(); r <= mergedRange9.getLastRow(); r++) { Row row =
			 * sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int c
			 * = mergedRange9.getFirstColumn(); c <= mergedRange9.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle8(workbook1)); } }
			 * 
			 * Row headerRow5 = sheet1.createRow(5); Cell headerCell10 =
			 * headerRow5.createCell(1); headerCell10.setCellValue("Month");
			 * sheet1.addMergedRegion(new CellRangeAddress(5, 5, 1, 2)); CellRangeAddress
			 * mergedRange10 = sheet1.getMergedRegion(9); for (int r =
			 * mergedRange10.getFirstRow(); r <= mergedRange10.getLastRow(); r++) { Row row
			 * = sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int
			 * c = mergedRange10.getFirstColumn(); c <= mergedRange10.getLastColumn(); c++)
			 * { Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * CellStyle style = workbook1.createCellStyle();
			 * style.setAlignment(HorizontalAlignment.CENTER);
			 * style.setVerticalAlignment(VerticalAlignment.CENTER);
			 * style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
			 * style.setFillPattern(FillPatternType.SOLID_FOREGROUND); Font font =
			 * workbook1.createFont(); font.setBold(true); style.setFont(font);
			 * cell.setCellStyle(style); } }
			 * 
			 * Cell headerCell112 = headerRow5.createCell(3);
			 * headerCell112.setCellValue(monthYearString); sheet1.addMergedRegion(new
			 * CellRangeAddress(5, 5, 3, 9)); CellRangeAddress mergedRange11 =
			 * sheet1.getMergedRegion(10); for (int r = mergedRange11.getFirstRow(); r <=
			 * mergedRange11.getLastRow(); r++) { Row row = sheet1.getRow(r); if (row ==
			 * null) { row = sheet1.createRow(r); } for (int c =
			 * mergedRange11.getFirstColumn(); c <= mergedRange11.getLastColumn(); c++) {
			 * Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * cell.setCellStyle(getHeaderCellStyle9(workbook1)); } }
			 * 
			 * // Create header row for monthly timesheet Row headerRow8 =
			 * sheet1.createRow(6); Cell headerCell = headerRow8.createCell(1,
			 * CellType.STRING); headerCell.setCellValue("DATES(DD-MM-YYYY)");
			 * sheet1.addMergedRegion(new CellRangeAddress(6, 6, 1, 2));
			 * 
			 * headerCell = headerRow8.createCell(3, CellType.STRING);
			 * headerCell.setCellValue("DAYS"); sheet1.addMergedRegion(new
			 * CellRangeAddress(6, 6, 3, 4));
			 * 
			 * headerCell = headerRow8.createCell(5, CellType.STRING);
			 * headerCell.setCellValue("ATTENDANCE STATUS");
			 * headerCell.setCellStyle(getHeaderCellStyle(workbook1)); headerCell =
			 * headerRow8.createCell(6, CellType.STRING);
			 * headerCell.setCellValue("Full Day / Half Day");
			 * headerCell.setCellStyle(getHeaderCellStyle(workbook1)); headerCell =
			 * headerRow8.createCell(7, CellType.STRING); headerCell.setCellValue("DAY");
			 * headerCell.setCellStyle(getHeaderCellStyle(workbook1)); headerCell =
			 * headerRow8.createCell(8, CellType.STRING); headerCell.setCellValue("HOURS");
			 * headerCell.setCellStyle(getHeaderCellStyle(workbook1)); headerCell =
			 * headerRow8.createCell(9, CellType.STRING); headerCell.setCellValue("AM/PM");
			 * headerCell.setCellStyle(getHeaderCellStyle(workbook1));
			 * 
			 * CellRangeAddress mergedRange12 = sheet1.getMergedRegion(11); CellRangeAddress
			 * mergedRange13 = sheet1.getMergedRegion(12);
			 * 
			 * for (int r = mergedRange12.getFirstRow(); r <= mergedRange12.getLastRow();
			 * r++) { Row row = sheet1.getRow(r); if (row == null) { row =
			 * sheet1.createRow(r); } for (int c = mergedRange12.getFirstColumn(); c <=
			 * mergedRange12.getLastColumn(); c++) { Cell cell = row.getCell(c); if (cell ==
			 * null) { cell = row.createCell(c); }
			 * 
			 * cell.setCellStyle(getHeaderCellStyle(workbook1)); } } for (int r =
			 * mergedRange13.getFirstRow(); r <= mergedRange13.getLastRow(); r++) { Row row
			 * = sheet1.getRow(r); if (row == null) { row = sheet1.createRow(r); } for (int
			 * c = mergedRange13.getFirstColumn(); c <= mergedRange13.getLastColumn(); c++)
			 * { Cell cell = row.getCell(c); if (cell == null) { cell = row.createCell(c); }
			 * 
			 * cell.setCellStyle(getHeaderCellStyle(workbook1)); } }
			 * 
			 * try {
			 * 
			 * String tempfilelocation = Utils.getProperty("fileLocation") + File.separator
			 * + "DAH2timesheet_details"; File fileDir = new File(tempfilelocation); if
			 * (!fileDir.exists()) { boolean iscreated = fileDir.mkdirs(); if (!iscreated) {
			 * throw new Exception("Failed to create Directory"); } }
			 * 
			 * String path = fileDir + File.separator + "DAH2" + "_" +
			 * "AutomationTimesheet.xlsx"; FileOutputStream fileOut = new
			 * FileOutputStream(path); workbook1.write(fileOut); fileOut.close();
			 * 
			 * } catch (Exception e) { e.printStackTrace(); return responseEntity = new
			 * ResponseEntity<byte[]>(HttpStatus.NOT_FOUND); }
			 * 
			 * 
			 */

			Workbook workbook = getXSSFWorkbook();
			Sheet sheet = workbook.getSheet("TimeSheet");

			Row headerRow11 = sheet.getRow(1);
			Cell headerCell3 = headerRow11.getCell(3);
			headerCell3.setCellValue(empid);
			Row headerRow21 = sheet.getRow(2);
			Cell headerCell5 = headerRow21.getCell(3);
			headerCell5.setCellValue(empname);
			Row headerRow31 = sheet.getRow(3);
			Cell headerCell7 = headerRow31.getCell(3);
			headerCell7.setCellValue(client);
			Row headerRow41 = sheet.getRow(4);
			Cell headerCell9 = headerRow41.getCell(3);
			headerCell9.setCellValue(managername);
			Row headerRow51 = sheet.getRow(5);
			Cell headerCell11 = headerRow51.getCell(3);
			headerCell11.setCellValue(monthYearString);
			Row headerRow81 = sheet.getRow(6);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(selectedMonth);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			Date startDayOfMonth = calendar.getTime();
			int firstDay = calendar.get(Calendar.DAY_OF_MONTH);
			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date endDayOfMonth = calendar.getTime();
			int lastDay = calendar.get(Calendar.DAY_OF_MONTH);
			LocalDate localDate = selectedMonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate firstDayOfMonth = localDate.withDayOfMonth(1);

			LocalDate empstartdate = null;
			LocalDate empendDate = null;
			try {

				String empdetails_query = "SELECT * FROM Employee_Personal_Details WHERE employee_id =:empid";
				Query emplist = session.createSQLQuery(empdetails_query).addEntity(Employee_Personal_Details.class)
						.setParameter("empid", empid);

				List<Object> results = emplist.list();

				for (Object checklist : results) {
					Employee_Personal_Details items = (Employee_Personal_Details) checklist;
					Timestamp joingdate = items.getActual_date_of_joining();
					Timestamp reliving = items.getRelieving_date();

					LocalDateTime joiningDateTime = joingdate.toLocalDateTime();
					empstartdate = joiningDateTime.toLocalDate();

					if (reliving != null) {
						LocalDateTime relivingDateTime = reliving.toLocalDateTime();
						empendDate = relivingDateTime.toLocalDate();
						if (firstDayOfMonth.isAfter(empendDate)) {
							throw new Exception("Custom Exception: Start date is after end date");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();

				return responseEntity = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
			}
			if (empendDate == null) {
				LocalDate endLocalDate = endDayOfMonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				empendDate = endLocalDate;
			}

			String workingonRotationalshift = automation.getWorkingOnRotationalshifts();
			List<WorkingOnPublicHolidays> workingOnPublicHolidays = automation.getWorkingOnPH();
			List<WorkingOnWeekEnds> onWeekEnds = automation.getWorkingOnWeekEnds();
			List<LeaveDetails> leaveDetails = automation.getLeaveDetails();
			List<WorkedOnShifts> onShifts = automation.getOnShifts();
			List<String> publicholiday = new ArrayList<>();
			List<String> PHday = new ArrayList<>();
			String client_id = automation.getClientId();
			try {
				String checklist_query = " SELECT client_id,holiday_name,holiday_date FROM Holiday_Master WHERE client_id =:client_id  AND DATE(holiday_date) \r\n"
						+ "  BETWEEN STR_TO_DATE(:fromdate,'%Y-%m-%d') AND :thrudate";

				Query emplist = session.createSQLQuery(checklist_query)
						.setResultTransformer(Transformers.aliasToBean(TimesheetAutomationHolidays.class))
						.setParameter("client_id", client_id).setParameter("fromdate", startDayOfMonth)
						.setParameter("thrudate", endDayOfMonth);

				List<Object> results = emplist.list();
				
				if(!results.isEmpty()) {
					for (Object checklist : results) {
						TimesheetAutomationHolidays items = (TimesheetAutomationHolidays) checklist;
						publicholiday.add(items.getHoliday_date().toString());
						PHday.add(items.getHoliday_name());
					}
				} /*
					 * else { if(workCountry.equalsIgnoreCase("India")) { client_id = "226"; String
					 * holidayIndiaQuery =
					 * " SELECT client_id,holiday_name,holiday_date FROM Holiday_Master WHERE client_id =:client_id  AND DATE(holiday_date) \r\n"
					 * + "  BETWEEN STR_TO_DATE(:fromdate,'%Y-%m-%d') AND :thrudate";
					 * 
					 * Query holidaysIndialist = session.createSQLQuery(holidayIndiaQuery)
					 * .setResultTransformer(Transformers.aliasToBean(TimesheetAutomationHolidays.
					 * class)) .setParameter("client_id", client_id).setParameter("fromdate",
					 * startDayOfMonth) .setParameter("thrudate", endDayOfMonth);
					 * 
					 * List<Object> indianHolidays = holidaysIndialist.list();
					 * 
					 * for (Object checklist : indianHolidays) { TimesheetAutomationHolidays items =
					 * (TimesheetAutomationHolidays) checklist;
					 * publicholiday.add(items.getHoliday_date().toString());
					 * PHday.add(items.getHoliday_name()); }
					 * 
					 * 
					 * }else if(workCountry.equalsIgnoreCase("Singapore")) {
					 * 
					 * 
					 * client_id = "225"; String holidaySingaporeQuery =
					 * " SELECT client_id,holiday_name,holiday_date FROM Holiday_Master WHERE client_id =:client_id  AND DATE(holiday_date) \r\n"
					 * + "  BETWEEN STR_TO_DATE(:fromdate,'%Y-%m-%d') AND :thrudate";
					 * 
					 * Query holidaysSingaporelist = session.createSQLQuery(holidaySingaporeQuery)
					 * .setResultTransformer(Transformers.aliasToBean(TimesheetAutomationHolidays.
					 * class)) .setParameter("client_id", client_id).setParameter("fromdate",
					 * startDayOfMonth) .setParameter("thrudate", endDayOfMonth);
					 * 
					 * List<Object> SingaporeHolidays = holidaysSingaporelist.list();
					 * 
					 * for (Object checklist : SingaporeHolidays) { TimesheetAutomationHolidays
					 * items = (TimesheetAutomationHolidays) checklist;
					 * publicholiday.add(items.getHoliday_date().toString());
					 * PHday.add(items.getHoliday_name()); }
					 * 
					 * } }
					 */
				

			} catch (Exception e) {
				e.printStackTrace();

				return responseEntity = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
			}

			List<LocalDate> publicHolidayDates = publicholiday.stream()
					.map(ss -> LocalDate.parse(ss, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS][.S]")))
					.collect(Collectors.toList());

			int rowIndex = 7;
			int workingDaysCount = 0;
			double dayscount = 0;
			double hourscount = 0;
			double annulavecount = 0;
			double csleavecount = 0;
			double hospCount = 0;
			double cclCount = 0;
			double mateCount= 0;
			double oilCount= 0;
			double nslCount= 0;
			double nplCount= 0;
			double compassCount= 0;
			double paternCount= 0;
			double bereavCount= 0;
			double ecclCount= 0;
			double emlCount= 0;
			double compoffCount= 0;
			double pholidatcount = 0;

		//	if (workingonRotationalshift.equalsIgnoreCase("NO")) {
				for (int i = firstDay - 1; i < lastDay; i++) {

					LocalDate date = firstDayOfMonth.plusDays(i);
					int newrowindex = rowIndex++;
					Row row = sheet.createRow(newrowindex);
					Cell cell = row.createCell(1, CellType.STRING);

					sheet.addMergedRegion(new CellRangeAddress(newrowindex, newrowindex, 1, 2));
					CellRangeAddress mergedRange14 = sheet.getMergedRegion(13);
					cell.setCellValue(date.format(dateFormatter));
					// String currentdate = date.format(dateFormatter);
					for (int r = mergedRange14.getFirstRow(); r <= mergedRange14.getLastRow(); r++) {
						Row row1 = sheet.getRow(r);
						if (row1 == null) {
							row1 = sheet.createRow(r);
						}
						for (int c = mergedRange14.getFirstColumn(); c <= mergedRange14.getLastColumn(); c++) {
							Cell cell1 = row.getCell(c);
							if (cell1 == null) {
								cell1 = row.createCell(c);
							}

							if (publicHolidayDates.contains(date)) {
								cell1.setCellStyle(getHeaderCellStyle5(workbook));

							} else {
								cell1.setCellStyle(getHeaderCellStyle2(workbook));
							}
						}
					}
					cell = row.createCell(3, CellType.STRING);
					sheet.addMergedRegion(new CellRangeAddress(newrowindex, newrowindex, 3, 4));
					CellRangeAddress mergedRange15 = sheet.getMergedRegion(14);

					cell.setCellValue(date.format(dayFormatter));
					String day = date.format(dayFormatter);
					for (int r = mergedRange15.getFirstRow(); r <= mergedRange15.getLastRow(); r++) {
						Row row1 = sheet.getRow(r);
						if (row1 == null) {
							row1 = sheet.createRow(r);
						}
						for (int c = mergedRange15.getFirstColumn(); c <= mergedRange15.getLastColumn(); c++) {
							Cell cell1 = row.getCell(c);
							if (cell1 == null) {
								cell1 = row.createCell(c);
							}
							if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")) {
								cell1.setCellStyle(getHeaderCellStyle3(workbook));

							} else {
								cell1.setCellStyle(getHeaderCellStyle2(workbook));
							}
						}
					}

					cell = row.createCell(5, CellType.STRING);
					// cell.setCellValue("");
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					cell = row.createCell(6, CellType.STRING);
					// cell.setCellValue("");
					cell.setCellStyle(getHeaderCellStyle2(workbook));
					cell = row.createCell(7, CellType.STRING);
					cell.setCellValue(0);
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					cell = row.createCell(8, CellType.STRING);
					cell.setCellValue(0);
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					cell = row.createCell(9, CellType.STRING);
					// cell.setCellValue("");
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
							|| publicHolidayDates.contains(date)) {
						cell = row.createCell(5, CellType.STRING);
						if (publicHolidayDates.contains(date)) {
							cell.setCellValue("PH");
							cell.setCellStyle(getHeaderCellStyle6(workbook));
							pholidatcount = pholidatcount + 1;
						} else {
							cell.setCellStyle(getHeaderCellStyle6(workbook));
						}
						cell = row.createCell(6, CellType.STRING);
						cell.setCellStyle(getHeaderCellStyle6(workbook));

						cell = row.createCell(7, CellType.STRING);
						cell.setCellValue(0);
						cell.setCellStyle(getHeaderCellStyle2(workbook));

						cell = row.createCell(8, CellType.STRING);
						cell.setCellValue(0);
						cell.setCellStyle(getHeaderCellStyle2(workbook));

						cell = row.createCell(9, CellType.STRING);
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						workingDaysCount = workingDaysCount - 1;
					}

					if (date.isEqual(empstartdate) || date.isEqual(empendDate)
							|| (date.isAfter(empstartdate) && date.isBefore(empendDate))) {

						cell = row.createCell(5, CellType.STRING);
						cell.setCellValue("PRESENT");
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(6, CellType.STRING);
						cell.setCellValue("FULL DAY");
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(7, CellType.STRING);
						cell.setCellValue(1);
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(8, CellType.STRING);
						cell.setCellValue(8);
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(9, CellType.STRING);
						// cell.setCellValue("AM");
						cell.setCellStyle(getHeaderCellStyle2(workbook));

						if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
								|| publicHolidayDates.contains(date)) {
							cell = row.createCell(5, CellType.STRING);
							if (publicHolidayDates.contains(date)) {
								cell.setCellValue("PH");
								cell.setCellStyle(getHeaderCellStyle6(workbook));
							} else {
								cell.setCellStyle(getHeaderCellStyle6(workbook));
							}

							cell = row.createCell(6, CellType.STRING);
							cell.setCellStyle(getHeaderCellStyle6(workbook));

							cell = row.createCell(7, CellType.STRING);
							cell.setCellValue(0);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(8, CellType.STRING);
							cell.setCellValue(0);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(9, CellType.STRING);
							cell.setCellStyle(getHeaderCellStyle2(workbook));
						}
					}

					for (LeaveDetails details : leaveDetails) {

						LocalDate startDate = details.getStartdate().toLocalDateTime().toLocalDate();
						LocalDate endDate = details.getEnddate().toLocalDateTime().toLocalDate();
						LocalDate currentDate = date;

						if (details.getType_of_leave().equalsIgnoreCase("Annual Leave")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									// If the current date matches the leave period, set the cell value as LEAVE
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("ANNU");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										annulavecount = annulavecount + 1;

									} else {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("ANNU");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										annulavecount = annulavecount + 0.5;
									}
								}

							}

						} else if (details.getType_of_leave().equalsIgnoreCase("Casual/Sick Leave") ||details.getType_of_leave().equalsIgnoreCase("Sick Leave") ) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}

									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										if(workCountry.equalsIgnoreCase("Singapore")) {
											cell.setCellValue("SL");
										}else {
											cell.setCellValue("CL");
										}
									
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										csleavecount = csleavecount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										if(workCountry.equalsIgnoreCase("Singapore")) {
											cell.setCellValue("SL");
										}else {
											cell.setCellValue("CL");
										}
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										csleavecount = csleavecount + 0.5;
									}
								}

							}
						} else if (details.getType_of_leave().equalsIgnoreCase("HOSPITALISATION")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}

									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("HOSP.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										hospCount = hospCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("HOSP.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										hospCount = hospCount + 0.5;
									}

								}

							}
						} else if (details.getType_of_leave().equalsIgnoreCase("CHILD CARE LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("CCL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cclCount = cclCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("CCL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cclCount = cclCount + 0.5;
									}
								}

							}
						}
						else if (details.getType_of_leave().equalsIgnoreCase("MATERNITY LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("MATE.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										mateCount = mateCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("MATE.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										mateCount = mateCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("OFF IN LIEU")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("OIL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										oilCount = oilCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("OIL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										oilCount = oilCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("NO PAY LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("NPL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										nplCount = nplCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("NPL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										nplCount = nplCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("NATIONAL SERVICE LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("NSL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										nslCount = nslCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("NSL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										nslCount = nslCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("COMPASSIONATE LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("COMPASS.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										compassCount = compassCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("COMPASS.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										compassCount = compassCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("PATERNITY LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("PATERN.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										paternCount = paternCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("PATERN.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										paternCount = paternCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("BEREAVEMENT LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("BEREAV.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										bereavCount = bereavCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("BEREAV.L");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										bereavCount = bereavCount + 0.5;
									}
								}

							}
						}
						else if (details.getType_of_leave().equalsIgnoreCase("ENHANCED CHILD CARE LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("ECCL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										ecclCount = ecclCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("ECCL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										ecclCount = ecclCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("EXTENDED MATERNITY LEAVE")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("EXTENDED MATERNITY LEAVE ");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										emlCount = emlCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("EXTENDED MATERNITY LEAVE");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										emlCount = emlCount + 0.5;
									}
								}

							}
						}else if (details.getType_of_leave().equalsIgnoreCase("COMPENSATION OFF")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday")
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
										// pholidatcount = pholidatcount + 1;
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									// cell.setCellValue("");
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("FULL DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("COMP.OFF");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										//cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										compoffCount = compoffCount + 1;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("COMP.OFF");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getLeaveday());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										compoffCount = compoffCount + 0.5;
									}
								}

							}
						}
						
						
						
						
						
						
						
					}
					
					// This code is for Working on Shifts 
					
					/*
					 * for (WorkedOnShifts shift : onShifts) { LocalDate currentDate = date;
					 * LocalDate shiftStartDate =
					 * shift.getStartDate().toLocalDateTime().toLocalDate(); LocalDate shiftEndDate
					 * = shift.getEndDate().toLocalDateTime().toLocalDate(); if
					 * (shift.getShiftName().equalsIgnoreCase("Morning Shift")) { if
					 * (currentDate.isEqual(shiftStartDate) || currentDate.isEqual(shiftEndDate) ||
					 * (currentDate.isAfter(shiftStartDate) && currentDate.isBefore(shiftEndDate)))
					 * {
					 * 
					 * if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday") ||
					 * publicHolidayDates.contains(date)) { } else { cell = row.createCell(6,
					 * CellType.STRING); cell.setCellValue("S1(Morning Shift)");
					 * cell.setCellStyle(getHeaderCellStyle2(workbook)); } } } else if
					 * (shift.getShiftName().equalsIgnoreCase("Afternoon Shift")) { if
					 * (currentDate.isEqual(shiftStartDate) || currentDate.isEqual(shiftEndDate) ||
					 * (currentDate.isAfter(shiftStartDate) && currentDate.isBefore(shiftEndDate)))
					 * {
					 * 
					 * if (day.equalsIgnoreCase("Sunday") || day.equalsIgnoreCase("Saturday") ||
					 * publicHolidayDates.contains(date)) { } else { cell = row.createCell(6,
					 * CellType.STRING); cell.setCellValue("S2(Afternoon Shift)");
					 * cell.setCellStyle(getHeaderCellStyle2(workbook)); } } } else if
					 * (shift.getShiftName().equalsIgnoreCase("Weekend / Holiday / Night Shift")) {
					 * if (currentDate.isEqual(shiftStartDate) || currentDate.isEqual(shiftEndDate)
					 * || (currentDate.isAfter(shiftStartDate) &&
					 * currentDate.isBefore(shiftEndDate))) { if (day.equalsIgnoreCase("Sunday") ||
					 * day.equalsIgnoreCase("Saturday") || publicHolidayDates.contains(date)) { }
					 * else { cell = row.createCell(6, CellType.STRING);
					 * cell.setCellValue("S3(Weekend / Holiday / Night Shift)");
					 * cell.setCellStyle(getHeaderCellStyle2(workbook)); } } } }
					 */
					
					
					
					// This code is for Working on Public Holidays
					
					/*
					 * for (WorkingOnPublicHolidays onPublicHolidays : workingOnPublicHolidays) {
					 * LocalDate workingonPH =
					 * onPublicHolidays.getWorkedonPublicHoliday().toLocalDateTime() .toLocalDate();
					 * 
					 * if (date.isEqual(workingonPH)) { cell = row.createCell(5, CellType.STRING);
					 * cell.setCellValue("PRESENT");
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(6, CellType.STRING); cell.setCellValue("FULL DAY");
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(7, CellType.STRING); cell.setCellValue(1);
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(8, CellType.STRING); cell.setCellValue(8);
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(9, CellType.STRING);
					 * cell.setCellStyle(getHeaderCellStyle2(workbook)); }
					 * 
					 * }
					 */
					
					
					
					
					//This code is for Working on Week Ends  
					
					/*
					 * for (WorkingOnWeekEnds workingOnWeekEnds : onWeekEnds) { LocalDate
					 * workingonweekend = workingOnWeekEnds.getWorkOnWeekEnd().toLocalDateTime()
					 * .toLocalDate();
					 * 
					 * if (date.isEqual(workingonweekend)) { cell = row.createCell(5,
					 * CellType.STRING); cell.setCellValue("PRESENT");
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(6, CellType.STRING); cell.setCellValue("FULL DAY");
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(7, CellType.STRING); cell.setCellValue(1);
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(8, CellType.STRING); cell.setCellValue(8);
					 * cell.setCellStyle(getHeaderCellStyle2(workbook));
					 * 
					 * cell = row.createCell(9, CellType.STRING);
					 * cell.setCellStyle(getHeaderCellStyle2(workbook)); }
					 * 
					 * }
					 */

					cell = row.getCell(7);
					dayscount = dayscount + cell.getNumericCellValue();
					cell = row.getCell(8);
					hourscount = hourscount + cell.getNumericCellValue();
					workingDaysCount = workingDaysCount + 1;
					
				}
				
				//present_days = dayscount;
				//total_days_count = workingDaysCount;
				//total_leave_days = annulavecount + csleavecount +compensatorycount +weekoffcount ;
				
				
				
				// This below commented code is for timesheet Automation for working on Rotaional weeekends
				// i.e. week ends are not saturday & sunday 

			/*} else if (workingonRotationalshift.equalsIgnoreCase("YES")) {
				RotationalWeekEnds rotationalWeekEnds = automation.getRotationalWeekEnds();
				String weekEndOne = rotationalWeekEnds.getWeekEndOne();
				String weekEndTwo = rotationalWeekEnds.getWeekEndtwo();

				for (int i = firstDay - 1; i < lastDay; i++) {

					LocalDate date = firstDayOfMonth.plusDays(i);
					int newrowindex = rowIndex++;
					Row row = sheet.createRow(newrowindex);
					Cell cell = row.createCell(1, CellType.STRING);

					sheet.addMergedRegion(new CellRangeAddress(newrowindex, newrowindex, 1, 2));
					CellRangeAddress mergedRange14 = sheet.getMergedRegion(13);
					cell.setCellValue(date.format(dateFormatter));
					// String currentdate = date.format(dateFormatter);
					for (int r = mergedRange14.getFirstRow(); r <= mergedRange14.getLastRow(); r++) {
						Row row1 = sheet.getRow(r);
						if (row1 == null) {
							row1 = sheet.createRow(r);
						}
						for (int c = mergedRange14.getFirstColumn(); c <= mergedRange14.getLastColumn(); c++) {
							Cell cell1 = row.getCell(c);
							if (cell1 == null) {
								cell1 = row.createCell(c);
							}

							if (publicHolidayDates.contains(date)) {
								cell1.setCellStyle(getHeaderCellStyle5(workbook));

							} else {
								cell1.setCellStyle(getHeaderCellStyle2(workbook));
							}
						}
					}
					cell = row.createCell(3, CellType.STRING);
					sheet.addMergedRegion(new CellRangeAddress(newrowindex, newrowindex, 3, 4));
					CellRangeAddress mergedRange15 = sheet.getMergedRegion(14);

					cell.setCellValue(date.format(dayFormatter));
					String day = date.format(dayFormatter);
					for (int r = mergedRange15.getFirstRow(); r <= mergedRange15.getLastRow(); r++) {
						Row row1 = sheet.getRow(r);
						if (row1 == null) {
							row1 = sheet.createRow(r);
						}
						for (int c = mergedRange15.getFirstColumn(); c <= mergedRange15.getLastColumn(); c++) {
							Cell cell1 = row.getCell(c);
							if (cell1 == null) {
								cell1 = row.createCell(c);
							}
							if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)) {
								cell1.setCellStyle(getHeaderCellStyle3(workbook));

							} else {
								cell1.setCellStyle(getHeaderCellStyle2(workbook));
							}
						}
					}

					cell = row.createCell(5, CellType.STRING);
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					cell = row.createCell(6, CellType.STRING);
					cell.setCellStyle(getHeaderCellStyle2(workbook));
					cell = row.createCell(7, CellType.STRING);
					cell.setCellValue(0);
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					cell = row.createCell(8, CellType.STRING);
					cell.setCellValue(0);
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					cell = row.createCell(9, CellType.STRING);
					cell.setCellStyle(getHeaderCellStyle2(workbook));

					if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
							|| publicHolidayDates.contains(date)) {
						cell = row.createCell(5, CellType.STRING);
						if (publicHolidayDates.contains(date)) {
							cell.setCellValue("PH");
							cell.setCellStyle(getHeaderCellStyle6(workbook));
							pholidatcount = pholidatcount + 1;
						} else {
							cell.setCellStyle(getHeaderCellStyle6(workbook));
						}
						cell = row.createCell(6, CellType.STRING);
						cell.setCellStyle(getHeaderCellStyle6(workbook));

						cell = row.createCell(7, CellType.STRING);
						cell.setCellValue(0);
						cell.setCellStyle(getHeaderCellStyle2(workbook));

						cell = row.createCell(8, CellType.STRING);
						cell.setCellValue(0);
						cell.setCellStyle(getHeaderCellStyle2(workbook));

						cell = row.createCell(9, CellType.STRING);
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						workingDaysCount = workingDaysCount - 1;
					}

					if (date.isEqual(startDate1) || date.isEqual(endDate1)
							|| (date.isAfter(startDate1) && date.isBefore(endDate1))) {

						cell = row.createCell(5, CellType.STRING);
						cell.setCellValue("PRESENT");
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(6, CellType.STRING);
						cell.setCellValue("FULL DAY");
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(7, CellType.STRING);
						cell.setCellValue(1);
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(8, CellType.STRING);
						cell.setCellValue(8);
						cell.setCellStyle(getHeaderCellStyle2(workbook));
						cell = row.createCell(9, CellType.STRING);
						cell.setCellStyle(getHeaderCellStyle2(workbook));

						if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
								|| publicHolidayDates.contains(date)) {
							cell = row.createCell(5, CellType.STRING);
							if (publicHolidayDates.contains(date)) {
								cell.setCellValue("PH");
								cell.setCellStyle(getHeaderCellStyle6(workbook));
							} else {
								cell.setCellStyle(getHeaderCellStyle6(workbook));
							}

							cell = row.createCell(6, CellType.STRING);
							cell.setCellStyle(getHeaderCellStyle6(workbook));

							cell = row.createCell(7, CellType.STRING);
							cell.setCellValue(0);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(8, CellType.STRING);
							cell.setCellValue(0);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(9, CellType.STRING);
							cell.setCellStyle(getHeaderCellStyle2(workbook));
						}
					}

					for (LeaveDetails details : leaveDetails) {

						LocalDate startDate = details.getStartdate().toLocalDateTime().toLocalDate();
						LocalDate endDate = details.getEnddate().toLocalDateTime().toLocalDate();
						LocalDate currentDate = date;

						if (details.getType_of_leave().equalsIgnoreCase("Annual Leave")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									// If the current date matches the leave period, set the cell value as LEAVE
									if (details.getLeaveday().equalsIgnoreCase("HALF DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("ANNU");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										annulavecount = annulavecount + 0.5;

									} else {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("ANNU");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										annulavecount = annulavecount + 1;
									}
								}

							}

						} else if (details.getType_of_leave().equalsIgnoreCase("Casual/Sick Leave")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}

									cell = row.createCell(6, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("HALF DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("CL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										csleavecount = csleavecount + 0.5;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("CL");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										csleavecount = csleavecount + 1;
									}
								}
							}
						} else if (details.getType_of_leave().equalsIgnoreCase("Compensatory Off")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}

									cell = row.createCell(6, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("HALF DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("COMP");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										compensatorycount = compensatorycount + 0.5;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("COMP");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										compensatorycount = compensatorycount + 1;
									}
								}
							}
						} else if (details.getType_of_leave().equalsIgnoreCase("Week OFF")) {
							if (currentDate.isEqual(startDate) || currentDate.isEqual(endDate)
									|| (currentDate.isAfter(startDate) && currentDate.isBefore(endDate))) {

								if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
										|| publicHolidayDates.contains(date)) {
									cell = row.createCell(5, CellType.STRING);
									if (publicHolidayDates.contains(date)) {
										cell.setCellValue("PH");
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									} else {
										cell.setCellStyle(getHeaderCellStyle6(workbook));
									}
									cell = row.createCell(6, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle6(workbook));

									cell = row.createCell(7, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(8, CellType.STRING);
									cell.setCellValue(0);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

									cell = row.createCell(9, CellType.STRING);
									cell.setCellStyle(getHeaderCellStyle2(workbook));

								} else {
									if (details.getLeaveday().equalsIgnoreCase("HALF DAY")) {
										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("OFF");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("HALF DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0.5);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(4);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellValue(details.getAmpm());
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										weekoffcount = weekoffcount + 0.5;

									} else {

										cell = row.createCell(5, CellType.STRING);
										cell.setCellValue("OFF");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(6, CellType.STRING);
										cell.setCellValue("FULL DAY");
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(7, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(8, CellType.STRING);
										cell.setCellValue(0);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										cell = row.createCell(9, CellType.STRING);
										cell.setCellStyle(getHeaderCellStyle2(workbook));

										weekoffcount = weekoffcount + 1;
									}
								}
							}
						}
					}
					for (WorkedOnShifts shift : onShifts) {
						LocalDate currentDate = date;
						LocalDate shiftStartDate = shift.getStartDate().toLocalDateTime().toLocalDate();
						LocalDate shiftEndDate = shift.getEndDate().toLocalDateTime().toLocalDate();
						if (shift.getShiftName().equalsIgnoreCase("Morning Shift")) {
							if (currentDate.isEqual(shiftStartDate) || currentDate.isEqual(shiftEndDate)
									|| (currentDate.isAfter(shiftStartDate) && currentDate.isBefore(shiftEndDate))) {

								if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
										|| publicHolidayDates.contains(date)) {
								} else {
									cell = row.createCell(6, CellType.STRING);
									cell.setCellValue("S1(Morning Shift)");
									cell.setCellStyle(getHeaderCellStyle2(workbook));
								}
							}
						} else if (shift.getShiftName().equalsIgnoreCase("Afternoon Shift")) {
							if (currentDate.isEqual(shiftStartDate) || currentDate.isEqual(shiftEndDate)
									|| (currentDate.isAfter(shiftStartDate) && currentDate.isBefore(shiftEndDate))) {

								if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
										|| publicHolidayDates.contains(date)) {
								} else {
									cell = row.createCell(6, CellType.STRING);
									cell.setCellValue("S2(Afternoon Shift)");
									cell.setCellStyle(getHeaderCellStyle2(workbook));
								}
							}
						} else if (shift.getShiftName().equalsIgnoreCase("Weekend / Holiday / Night Shift")) {
							if (currentDate.isEqual(shiftStartDate) || currentDate.isEqual(shiftEndDate)
									|| (currentDate.isAfter(shiftStartDate) && currentDate.isBefore(shiftEndDate))) {

								if (day.equalsIgnoreCase(weekEndOne) || day.equalsIgnoreCase(weekEndTwo)
										|| publicHolidayDates.contains(date)) {
								} else {
									cell = row.createCell(6, CellType.STRING);
									cell.setCellValue("S3(Weekend / Holiday / Night Shift)");
									cell.setCellStyle(getHeaderCellStyle2(workbook));
								}
							}
						}
					}

					for (WorkingOnPublicHolidays onPublicHolidays : workingOnPublicHolidays) {
						LocalDate workingonPH = onPublicHolidays.getWorkedonPublicHoliday().toLocalDateTime().toLocalDate();

						if (date.isEqual(workingonPH)) {
							cell = row.createCell(5, CellType.STRING);
							cell.setCellValue("PRESENT");
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(6, CellType.STRING);
							cell.setCellValue("FULL DAY");
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(7, CellType.STRING);
							cell.setCellValue(1);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(8, CellType.STRING);
							cell.setCellValue(8);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(9, CellType.STRING);
							cell.setCellStyle(getHeaderCellStyle2(workbook));
						}
					}

					for (WorkingOnWeekEnds workingOnWeekEnds : onWeekEnds) {
						LocalDate workingonweekend = workingOnWeekEnds.getWorkOnWeekEnd().toLocalDateTime()
								.toLocalDate();

						if (date.isEqual(workingonweekend)) {
							cell = row.createCell(5, CellType.STRING);
							cell.setCellValue("PRESENT");
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(6, CellType.STRING);
							cell.setCellValue("FULL DAY");
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(7, CellType.STRING);
							cell.setCellValue(1);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(8, CellType.STRING);
							cell.setCellValue(8);
							cell.setCellStyle(getHeaderCellStyle2(workbook));

							cell = row.createCell(9, CellType.STRING);
							cell.setCellStyle(getHeaderCellStyle2(workbook));
						}
					}
					cell = row.getCell(7);
					dayscount = dayscount + cell.getNumericCellValue();
					cell = row.getCell(8);
					hourscount = hourscount + cell.getNumericCellValue();
					workingDaysCount = workingDaysCount + 1;
				}
			}*/

				 
				
			description(sheet, workbook, headerRow11, headerRow21, headerRow31, headerRow41, headerRow51, headerRow81,
					annulavecount, csleavecount, pholidatcount, hospCount , cclCount ,mateCount,oilCount,nslCount,nplCount,
					compassCount, paternCount, bereavCount,ecclCount,emlCount,compoffCount,dayscount,workCountry);
			totalWorkingDays(sheet, workbook, workingDaysCount, dayscount, hourscount);
			noteMessage(sheet, workbook);
			remarks(sheet, workbook, leaveDetails);
			shiftDetails(sheet, workbook);
			publicHolidays(sheet, workbook, publicholiday, PHday);

			// Resize columns to fit content
			for (int j = 0; j < 14; j++) {
				sheet.autoSizeColumn(j);
			}

			
			// write the workbook to a server
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				if ("no".equalsIgnoreCase(awsCheck)) {
					String tempfilelocation = Utils.getProperty("fileLocation") + File.separator + "timesheet_details" + File.separator + monthYearString;
					File fileDir = new File(tempfilelocation);
					if (!fileDir.exists()) {
						boolean iscreated = fileDir.mkdirs();
						if (!iscreated) {
							throw new Exception("Failed to create Directory");
						}
					}

					String path = fileDir + File.separator + empname + "_" + client + "_" + "AutomationTimesheet.xlsx";
					FileOutputStream fileOut = new FileOutputStream(path);
					workbook.write(fileOut);
					fileOut.close();
				}
				if ("yes".equalsIgnoreCase(awsCheck)) {
					// windows path
					// String path1 = "timesheet_details"+"/" + monthYearString ;
					String path1 = "timesheet_details" + File.separator + monthYearString;

					File fileDir = new File(path1);
					if (!fileDir.exists()) {
						boolean iscreated = fileDir.mkdirs();
						if (!iscreated) {
							throw new Exception("Failed to create Directory");
						}
					}
					// linux path
					String path = "timesheet_details" + File.separator + monthYearString + File.separator + empname + "_"
							+ client + "_" + "AutomationTimesheet.xlsx";
					// String path = "timesheet_details"+"/" + monthYearString + "/"+ empname + "_" +
					// client + "_" + "AutomationTimesheet.xlsx";

					workbook.write(bos);
					System.out.println("bos" + bos.toByteArray().length);
					Utils.convertXlsxAndPdfFileToByte(bos.toByteArray(), path);
					bos.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
				Map<String, String> errorDetails = new HashMap<>();
				errorDetails.put("message", "Failed to Generate Automation Timesheet File :Failed To Save File");
				String json = new ObjectMapper().writeValueAsString(errorDetails);
				return responseEntity = ResponseEntity.<byte[]>status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(json.getBytes());
			}
			// To get Automation timesheet from server

			byte[] files = null;
			FileInputStream fi = null;
			String clientfilelocation = null;
			try {
				if ("no".equalsIgnoreCase(awsCheck)) {
					clientfilelocation = Utils.getProperty("fileLocation") + File.separator + "timesheet_details"
							+ File.separator + empname + "_" + client + "_" + "AutomationTimesheet.xlsx";
					fi = new FileInputStream(clientfilelocation);
					files = IOUtils.toByteArray(fi);
					fi.close();
				}
				if ("yes".equalsIgnoreCase(awsCheck)) {
					clientfilelocation = "timesheet_details" + File.separator + monthYearString + File.separator + empname
							+ "_" + client + "_" + "AutomationTimesheet.xlsx";

					// clientfilelocation ="timesheet_details" + "/" + monthYearString +"/" +empname +
					// "_" + client + "_" +
					// "AutomationTimesheet.xlsx";
					files = Utils.downloadFileByAWSS3Bucket(clientfilelocation);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Map<String, String> errorDetails = new HashMap<>();
				errorDetails.put("message",
						"Failed to Generate Automation Timesheet File :Failed to Get File From Server ");
				String json = new ObjectMapper().writeValueAsString(errorDetails);
				return responseEntity = ResponseEntity.<byte[]>status(HttpStatus.NOT_FOUND).body(json.getBytes());
			}
			responseEntity = new ResponseEntity<byte[]>(files, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			// return responseEntity = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
			Map<String, String> errorDetails = new HashMap<>();
			errorDetails.put("message", "Failed to Generate Automation Timesheet File,Contact Your Manager");
			String json = new ObjectMapper().writeValueAsString(errorDetails);
			return responseEntity = ResponseEntity.<byte[]>status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(json.getBytes());

		} finally {
			session.close();
		}
		return responseEntity;
	}

	private static CellStyle getHeaderCellStyle(Workbook workbook) {
		CellStyle style = getHeaderCellStylecenter(workbook);
		style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		Font font = workbook.createFont();
		/*
		 * font.setBold(true); style.setFont(font);
		 */
		return style;
	}

	private static CellStyle getHeaderCellStyle2(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);

		return style;
	}

	private static CellStyle getHeaderCellStyle10(Workbook workbook) {
		CellStyle style = getHeaderCellStyle7(workbook);

		return style;
	}

	private static CellStyle getHeaderCellStylecenter(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		return style;
	}

	private static CellStyle getHeaderCellStyle3(Workbook workbook) {
		CellStyle style = getHeaderCellStylecenter(workbook);
		style.setFillForegroundColor(IndexedColors.ROSE.getIndex());

		return style;
	}

	private static CellStyle getHeaderCellStyle4(Workbook workbook) {
		CellStyle style = getHeaderCellStylecenter(workbook);
		style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());

		return style;
	}

	private static CellStyle getHeaderCellStyle5(Workbook workbook) {
		CellStyle style = getHeaderCellStylecenter(workbook);
		style.setFillForegroundColor(IndexedColors.LIME.getIndex());

		return style;
	}

	private static CellStyle getHeaderCellStyle6(Workbook workbook) {
		CellStyle style = getHeaderCellStylecenter(workbook);
		style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());

		return style;
	}

	private static CellStyle getHeaderCellStyle7(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());

		return style;
	}

	private static CellStyle getHeaderCellStyle8(Workbook workbook) {
		CellStyle style = getHeaderCellStylecenter(workbook);
		style.setFillForegroundColor(IndexedColors.TAN.getIndex());
		Font font = workbook.createFont();
		/*
		 * font.setBold(true); style.setFont(font);
		 */

		return style;
	}

	private static CellStyle getHeaderCellStyle9(Workbook workbook) {
		CellStyle style = getHeaderCellStylecenter(workbook);
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);

		return style;
	}

	private static void shiftDetails(Sheet sheet, Workbook workbook) {

		Row headerRow11 = sheet.getRow(18);
		if (headerRow11 == null) {
			headerRow11 = sheet.createRow(18);
		}
		Cell headerCell40 = headerRow11.createCell(11, CellType.STRING);
		headerCell40.setCellValue("Shift Details");
		// headerCell40.setCellStyle(getHeaderCellStyle2(workbook));
		sheet.addMergedRegion(new CellRangeAddress(18, 18, 11, 13));
		int lastMergedRegionIndex = sheet.getNumMergedRegions() - 1;
		CellRangeAddress mergedRange16 = sheet.getMergedRegion(lastMergedRegionIndex);
		for (int r = mergedRange16.getFirstRow(); r <= mergedRange16.getLastRow(); r++) {
			Row row = sheet.getRow(r);
			if (row == null) {
				row = sheet.createRow(r);
			}
			for (int c = mergedRange16.getFirstColumn(); c <= mergedRange16.getLastColumn(); c++) {
				Cell cell = row.getCell(c);
				if (cell == null) {
					cell = row.createCell(c);
				}

				cell.setCellStyle(getHeaderCellStyle(workbook));
			}
		}
		Row headerRow12 = sheet.getRow(19);
		if (headerRow12 == null) {
			headerRow12 = sheet.createRow(19);
		}
		Cell headerCell41 = headerRow12.createCell(11, CellType.STRING);
		headerCell41.setCellValue("Shift Codes");
		headerCell41.setCellStyle(getHeaderCellStyle2(workbook));

		Cell headerCell42 = headerRow12.createCell(12, CellType.STRING);
		headerCell42.setCellValue("Shift Details");
		headerCell42.setCellStyle(getHeaderCellStyle2(workbook));

		Cell headerCell43 = headerRow12.createCell(13, CellType.STRING);
		headerCell43.setCellValue("No Of Shifts");
		headerCell43.setCellStyle(getHeaderCellStyle2(workbook));

		Row headerRow13 = sheet.getRow(20);
		if (headerRow13 == null) {
			headerRow13 = sheet.createRow(20);
		}
		Cell headerCell44 = headerRow13.createCell(11, CellType.STRING);
		headerCell44.setCellValue("S1");
		headerCell44.setCellStyle(getHeaderCellStyle2(workbook));

		Cell headerCell45 = headerRow13.createCell(12, CellType.STRING);
		headerCell45.setCellValue("Morning Shift");
		headerCell45.setCellStyle(getHeaderCellStyle2(workbook));

		Cell headerCell46 = headerRow13.createCell(13, CellType.STRING);
		// headerCell43.setCellValue("No Of Shifts");
		headerCell46.setCellStyle(getHeaderCellStyle2(workbook));

		Row headerRow14 = sheet.getRow(21);
		if (headerRow14 == null) {
			headerRow14 = sheet.createRow(21);
		}
		Cell headerCell47 = headerRow14.createCell(11, CellType.STRING);
		headerCell47.setCellValue("S2");
		headerCell47.setCellStyle(getHeaderCellStyle2(workbook));

		Cell headerCell48 = headerRow14.createCell(12, CellType.STRING);
		headerCell48.setCellValue("Afternoon Shift");
		headerCell48.setCellStyle(getHeaderCellStyle2(workbook));

		Cell headerCell49 = headerRow14.createCell(13, CellType.STRING);
		// headerCell43.setCellValue("No Of Shifts");
		headerCell49.setCellStyle(getHeaderCellStyle2(workbook));

		Row headerRow15 = sheet.getRow(22);
		if (headerRow15 == null) {
			headerRow15 = sheet.createRow(22);
		}
		Cell headerCell50 = headerRow15.createCell(11, CellType.STRING);
		headerCell50.setCellValue("S3");
		headerCell50.setCellStyle(getHeaderCellStyle2(workbook));

		Cell headerCell51 = headerRow15.createCell(12, CellType.STRING);
		headerCell51.setCellValue("Weekend / Holiday / Night Shift");
		sheet.addMergedRegion(new CellRangeAddress(22, 22, 12, 13));
		int lastMergedRegionIndex1 = sheet.getNumMergedRegions() - 1;
		CellRangeAddress mergedRange17 = sheet.getMergedRegion(lastMergedRegionIndex1);
		for (int r = mergedRange17.getFirstRow(); r <= mergedRange17.getLastRow(); r++) {
			Row row = sheet.getRow(r);
			if (row == null) {
				row = sheet.createRow(r);
			}
			for (int c = mergedRange17.getFirstColumn(); c <= mergedRange17.getLastColumn(); c++) {
				Cell cell = row.getCell(c);
				if (cell == null) {
					cell = row.createCell(c);
				}

				cell.setCellStyle(getHeaderCellStyle2(workbook));
			}
		}

	}

	private static void totalWorkingDays(Sheet sheet, Workbook workbook, int workingDaysCount, double dayscount,
			double hourscount) {
		int lastrow = sheet.getLastRowNum() + 2;

		Row lastworkinhdayRow = sheet.createRow(lastrow);
		Cell headerCell56 = lastworkinhdayRow.createCell(1, CellType.STRING);
		headerCell56.setCellValue("Total working Days");
		sheet.addMergedRegion(new CellRangeAddress(lastrow, lastrow, 1, 2));
		int lastMergedRegionIndex = sheet.getNumMergedRegions() - 1;
		CellRangeAddress lwdmergedRange = sheet.getMergedRegion(lastMergedRegionIndex);
		for (int r = lwdmergedRange.getFirstRow(); r <= lwdmergedRange.getLastRow(); r++) {
			Row row = sheet.getRow(r);
			if (row == null) {
				row = sheet.createRow(r);
			}
			for (int c = lwdmergedRange.getFirstColumn(); c <= lwdmergedRange.getLastColumn(); c++) {
				Cell cell = row.getCell(c);
				if (cell == null) {
					cell = row.createCell(c);
				}

				cell.setCellStyle(getHeaderCellStyle4(workbook));
			}
		}
		Cell headerCell57 = lastworkinhdayRow.createCell(3, CellType.STRING);
		headerCell57.setCellValue(workingDaysCount);
		sheet.addMergedRegion(new CellRangeAddress(lastrow, lastrow, 3, 4));
		int lastMergedRegionIndex1 = sheet.getNumMergedRegions() - 1;
		CellRangeAddress lwdmergedRange2 = sheet.getMergedRegion(lastMergedRegionIndex1);
		for (int r = lwdmergedRange2.getFirstRow(); r <= lwdmergedRange2.getLastRow(); r++) {
			Row row = sheet.getRow(r);
			if (row == null) {
				row = sheet.createRow(r);
			}
			for (int c = lwdmergedRange2.getFirstColumn(); c <= lwdmergedRange2.getLastColumn(); c++) {
				Cell cell = row.getCell(c);
				if (cell == null) {
					cell = row.createCell(c);
				}

				cell.setCellStyle(getHeaderCellStyle4(workbook));
			}
		}

		Cell headerCell58 = lastworkinhdayRow.createCell(5, CellType.STRING);
		headerCell58.setCellValue("");
		sheet.addMergedRegion(new CellRangeAddress(lastrow, lastrow, 5, 6));
		int lastMergedRegionIndex2 = sheet.getNumMergedRegions() - 1;
		CellRangeAddress lwdmergedRange3 = sheet.getMergedRegion(lastMergedRegionIndex2);
		for (int r = lwdmergedRange3.getFirstRow(); r <= lwdmergedRange3.getLastRow(); r++) {
			Row row = sheet.getRow(r);
			if (row == null) {
				row = sheet.createRow(r);
			}
			for (int c = lwdmergedRange3.getFirstColumn(); c <= lwdmergedRange3.getLastColumn(); c++) {
				Cell cell = row.getCell(c);
				if (cell == null) {
					cell = row.createCell(c);
				}

				cell.setCellStyle(getHeaderCellStyle4(workbook));
			}
		}

		Cell headerCell59 = lastworkinhdayRow.createCell(7, CellType.STRING);
		headerCell59.setCellValue(dayscount);
		headerCell59.setCellStyle(getHeaderCellStyle4(workbook));
		Cell headerCell60 = lastworkinhdayRow.createCell(8, CellType.STRING);
		headerCell60.setCellValue(hourscount);
		headerCell60.setCellStyle(getHeaderCellStyle4(workbook));
		Cell headerCell61 = lastworkinhdayRow.createCell(9, CellType.STRING);
		// value
		headerCell61.setCellStyle(getHeaderCellStyle4(workbook));

	}

	private static void noteMessage(Sheet sheet, Workbook workbook) {
		int lastrow = sheet.getLastRowNum() + 2;
		Row lastworkinhdayRow = sheet.createRow(lastrow);
		Cell headerCell62 = lastworkinhdayRow.createCell(1, CellType.STRING);
		headerCell62.setCellValue(
				"Note: For half day leave, kindly mention 0.5 in DAY and AM or PM in AM/PM column.");
		sheet.addMergedRegion(new CellRangeAddress(lastrow, lastrow, 1, 9));
		int lastMergedRegionIndex = sheet.getNumMergedRegions() - 1;
		CellRangeAddress lwdmergedRange = sheet.getMergedRegion(lastMergedRegionIndex);
		for (int r = lwdmergedRange.getFirstRow(); r <= lwdmergedRange.getLastRow(); r++) {
			Row row = sheet.getRow(r);
			if (row == null) {
				row = sheet.createRow(r);
			}
			for (int c = lwdmergedRange.getFirstColumn(); c <= lwdmergedRange.getLastColumn(); c++) {
				Cell cell = row.getCell(c);
				if (cell == null) {
					cell = row.createCell(c);
				}

				cell.setCellStyle(getHeaderCellStyle9(workbook));
			}
		}

	}

	private static void remarks(Sheet sheet, Workbook workbook, List<LeaveDetails> leaveDetails) {
		int lastrow = sheet.getLastRowNum() + 2;
		Row remarkrow = sheet.createRow(lastrow);
		Cell headerCell63 = remarkrow.createCell(1, CellType.STRING);
		headerCell63.setCellValue("REMARKS:");
		sheet.addMergedRegion(new CellRangeAddress(lastrow, lastrow, 1, 9));
		int lastMergedRegionIndex = sheet.getNumMergedRegions() - 1;
		CellRangeAddress lwdmergedRange = sheet.getMergedRegion(lastMergedRegionIndex);
		for (int r = lwdmergedRange.getFirstRow(); r <= lwdmergedRange.getLastRow(); r++) {
			Row row = sheet.getRow(r);
			if (row == null) {
				row = sheet.createRow(r);
			}
			for (int c = lwdmergedRange.getFirstColumn(); c <= lwdmergedRange.getLastColumn(); c++) {
				Cell cell = row.getCell(c);
				if (cell == null) {
					cell = row.createCell(c);
				}

				cell.setCellStyle(getHeaderCellStyle7(workbook));
			}
		}
		
		int i =1;
		if(leaveDetails!= null && !leaveDetails.isEmpty()) {
			for( LeaveDetails details : leaveDetails) {
				String remarks = details.getRemarks();
				if(remarks != null && !remarks.isEmpty()) {
					// Remark 1
					Row remark1 = sheet.createRow(lastrow + i);
					Cell headerCell64 = remark1.createCell(1, CellType.STRING);
					headerCell64.setCellValue(i +") " + remarks);
					sheet.addMergedRegion(new CellRangeAddress(lastrow + i, lastrow + i, 1, 9));
					i++;
					int reamekMergedRegionIndex1 = sheet.getNumMergedRegions() - 1;
					CellRangeAddress reamrkwdmergedRange1 = sheet.getMergedRegion(reamekMergedRegionIndex1);
					for (int r = reamrkwdmergedRange1.getFirstRow(); r <= reamrkwdmergedRange1.getLastRow(); r++) {
						Row row = sheet.getRow(r);
						if (row == null) {
							row = sheet.createRow(r);
						}
						for (int c = reamrkwdmergedRange1.getFirstColumn(); c <= reamrkwdmergedRange1.getLastColumn(); c++) {
							Cell cell = row.getCell(c);
							if (cell == null) {
								cell = row.createCell(c);
							}

							cell.setCellStyle(getHeaderCellStyle7(workbook));
						}
					}
				}
			}
		}

		/*
		 * // Remark 2 Row remark2 = sheet.createRow(lastrow + 2); Cell headerCell65 =
		 * remark2.createCell(1, CellType.STRING); headerCell65.setCellValue("2)");
		 * sheet.addMergedRegion(new CellRangeAddress(lastrow + 2, lastrow + 2, 1, 9));
		 * int reamekMergedRegionIndex2 = sheet.getNumMergedRegions() - 1;
		 * CellRangeAddress reamrkwdmergedRange2 =
		 * sheet.getMergedRegion(reamekMergedRegionIndex2); for (int r =
		 * reamrkwdmergedRange2.getFirstRow(); r <= reamrkwdmergedRange2.getLastRow();
		 * r++) { Row row = sheet.getRow(r); if (row == null) { row =
		 * sheet.createRow(r); } for (int c = reamrkwdmergedRange2.getFirstColumn(); c
		 * <= reamrkwdmergedRange2.getLastColumn(); c++) { Cell cell = row.getCell(c);
		 * if (cell == null) { cell = row.createCell(c); }
		 * 
		 * cell.setCellStyle(getHeaderCellStyle7(workbook)); } } // Remark 3 Row remark3
		 * = sheet.createRow(lastrow + 3); Cell headerCell66 = remark3.createCell(1,
		 * CellType.STRING); headerCell66.setCellValue("2)"); sheet.addMergedRegion(new
		 * CellRangeAddress(lastrow + 3, lastrow + 3, 1, 9)); int
		 * reamekMergedRegionIndex3 = sheet.getNumMergedRegions() - 1; CellRangeAddress
		 * reamrkwdmergedRange3 = sheet.getMergedRegion(reamekMergedRegionIndex3); for
		 * (int r = reamrkwdmergedRange3.getFirstRow(); r <=
		 * reamrkwdmergedRange3.getLastRow(); r++) { Row row = sheet.getRow(r); if (row
		 * == null) { row = sheet.createRow(r); } for (int c =
		 * reamrkwdmergedRange3.getFirstColumn(); c <=
		 * reamrkwdmergedRange3.getLastColumn(); c++) { Cell cell = row.getCell(c); if
		 * (cell == null) { cell = row.createCell(c); }
		 * 
		 * cell.setCellStyle(getHeaderCellStyle7(workbook)); } }
		 */

	}

	// DESCRIPTION
	private static void description(Sheet sheet, Workbook workbook, Row headerRow1, Row headerRow2, Row headerRow3,
			Row headerRow4, Row headerRow5, Row headerRow8, double annulavecount, double csleavecount,
			double pholidatcount,double hospCount ,double cclCount ,double mateCount,
			double oilCount,double nslCount,double nplCount,double compassCount,double paternCount,
			double bereavCount, double ecclCount,double emlCount,double compoffCount, double dayscount,String workCountry) {

		Cell headerCell19 = headerRow1.createCell(11);
		headerCell19.setCellValue("TIMESHEET SUMMARY");
		headerCell19.setCellStyle(getHeaderCellStyle(workbook));

		Cell headerCell20 = headerRow1.createCell(12);
		headerCell20.setCellValue("LEAVE CODE");
		headerCell20.setCellStyle(getHeaderCellStyle(workbook));

		Cell headerCell21 = headerRow1.createCell(13);
		headerCell21.setCellValue("LEAVES TAKEN");
		headerCell21.setCellStyle(getHeaderCellStyle(workbook));

		Cell headerCell25 = headerRow2.createCell(11);
		headerCell25.setCellValue("ANNUAL LEAVE");
		headerCell25.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell26 = headerRow2.createCell(12);
		headerCell26.setCellValue("ANNU");
		headerCell26.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell27 = headerRow2.createCell(13);
		headerCell27.setCellValue(annulavecount);
		headerCell27.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell28 = headerRow4.createCell(11);
		headerCell28.setCellValue("HOSPITALISATION");
		headerCell28.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell29 = headerRow4.createCell(12);
		headerCell29.setCellValue("HOSP.L");
		headerCell29.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell30 = headerRow4.createCell(13);
		headerCell30.setCellValue(hospCount);
		headerCell30.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell31 = headerRow3.createCell(11);
		if(workCountry != null) {
			if(workCountry.equalsIgnoreCase("Singapore")) {
				headerCell31.setCellValue("SICK LEAVE");
			}else {
				headerCell31.setCellValue("CASUAL/SICK LEAVE");	
			}
		}else {
			headerCell31.setCellValue("CASUAL/SICK LEAVE");	
		}
		
	
		headerCell31.setCellStyle(getHeaderCellStyle10(workbook));
		Cell headerCell32 = headerRow3.createCell(12);
		if(workCountry != null) {
			if(workCountry.equalsIgnoreCase("Singapore")) {
				headerCell32.setCellValue("SL");
			}else {
				headerCell32.setCellValue("CL");
			}
		}else {
			headerCell32.setCellValue("CL");
		}
	
		headerCell32.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell33 = headerRow3.createCell(13);
		headerCell33.setCellValue(csleavecount);
		headerCell33.setCellStyle(getHeaderCellStyle10(workbook));
		
		
		
		Cell headerCell31c = headerRow5.createCell(11);
		headerCell31c.setCellValue("CHILD CARE LEAVE ");
		headerCell31c.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell32c = headerRow5.createCell(12);
		headerCell32c.setCellValue("CCL");
		headerCell32c.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell33c = headerRow5.createCell(13);
		headerCell33c.setCellValue(cclCount);
		headerCell33c.setCellStyle(getHeaderCellStyle10(workbook));
		
		
		

		Cell headerCell34 = headerRow8.createCell(11);
		headerCell34.setCellValue("MATERNITY LEAVE");
		headerCell34.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell35 = headerRow8.createCell(12);
		headerCell35.setCellValue("MATE.L");
		headerCell35.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell36 = headerRow8.createCell(13);
		headerCell36.setCellValue(mateCount);
		headerCell36.setCellStyle(getHeaderCellStyle10(workbook));

		Row headerRow7 = sheet.getRow(7);
		if (headerRow7 == null) {
			headerRow7 = sheet.createRow(7);
		}
		Cell headerCell37 = headerRow7.createCell(11);
		headerCell37.setCellValue("OFF IN LIEU ");
		headerCell37.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell38 = headerRow7.createCell(12);
		headerCell38.setCellValue("OIL");
		headerCell38.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell39 = headerRow7.createCell(13);
		headerCell39.setCellValue(oilCount);
		headerCell39.setCellStyle(getHeaderCellStyle10(workbook));

		Row headerRow81 = sheet.getRow(8);
		if (headerRow81 == null) {
			headerRow81 = sheet.createRow(8);
		}
		Cell headerCell40 = headerRow81.createCell(11);
		headerCell40.setCellValue("NATIONAL SERVICE LEAVE ");
		headerCell40.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell41 = headerRow81.createCell(12);
		headerCell41.setCellValue("NSL");
		headerCell41.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell42 = headerRow81.createCell(13);
		headerCell42.setCellValue(nslCount);
		headerCell42.setCellStyle(getHeaderCellStyle10(workbook));
		
		
		Row headerRow82 = sheet.getRow(9);
		if (headerRow82 == null) {
			headerRow82 = sheet.createRow(9);
		}
		Cell headerCell43 = headerRow82.createCell(11);
		headerCell43.setCellValue("NO PAY LEAVE ");
		headerCell43.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell44 = headerRow82.createCell(12);
		headerCell44.setCellValue("NPL");
		headerCell44.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell45 = headerRow82.createCell(13);
		headerCell45.setCellValue(nplCount);
		headerCell45.setCellStyle(getHeaderCellStyle10(workbook));
		
		
		Row headerRow83 = sheet.getRow(10);
		if (headerRow83 == null) {
			headerRow83 = sheet.createRow(10);
		}
		Cell headerCell46 = headerRow83.createCell(11);
		headerCell46.setCellValue("COMPASSIONATE LEAVE ");
		headerCell46.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell47 = headerRow83.createCell(12);
		headerCell47.setCellValue("COMPASS.L");
		headerCell47.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell48 = headerRow83.createCell(13);
		headerCell48.setCellValue(compassCount);
		headerCell48.setCellStyle(getHeaderCellStyle10(workbook));

		
		Row headerRow84 = sheet.getRow(11);
		if (headerRow84 == null) {
			headerRow84 = sheet.createRow(11);
		}
		Cell headerCell49 = headerRow84.createCell(11);
		headerCell49.setCellValue("PATERNITY LEAVE ");
		headerCell49.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell50 = headerRow84.createCell(12);
		headerCell50.setCellValue("PATERN.L");
		headerCell50.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell51 = headerRow84.createCell(13);
		headerCell51.setCellValue(paternCount);
		headerCell51.setCellStyle(getHeaderCellStyle10(workbook));
		
		Row headerRow85 = sheet.getRow(12);
		if (headerRow85 == null) {
			headerRow85 = sheet.createRow(12);
		}
		Cell headerCell52 = headerRow85.createCell(11);
		headerCell52.setCellValue("BEREAVEMENT LEAVE");
		headerCell52.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell53 = headerRow85.createCell(12);
		headerCell53.setCellValue("BEREAV.L");
		headerCell53.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell54 = headerRow85.createCell(13);
		headerCell54.setCellValue(bereavCount);
		headerCell54.setCellStyle(getHeaderCellStyle10(workbook));

		
		Row headerRow86 = sheet.getRow(13);
		if (headerRow86 == null) {
			headerRow86 = sheet.createRow(13);
		}
		Cell headerCell55 = headerRow86.createCell(11);
		headerCell55.setCellValue("ENHANCED CHILD CARE LEAVE ");
		headerCell55.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell56 = headerRow86.createCell(12);
		headerCell56.setCellValue("ECCL");
		headerCell56.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell57 = headerRow86.createCell(13);
		headerCell57.setCellValue(ecclCount);
		headerCell57.setCellStyle(getHeaderCellStyle10(workbook));
		
		Row headerRow87 = sheet.getRow(14);
		if (headerRow87 == null) {
			headerRow87 = sheet.createRow(14);
		}
		Cell headerCell58 = headerRow87.createCell(11);
		headerCell58.setCellValue("EXTENDED MATERNITY LEAVE");
		headerCell58.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell59 = headerRow87.createCell(12);
		headerCell59.setCellValue("EXTENDED MATERNITY LEAVE ");
		headerCell59.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell60 = headerRow87.createCell(13);
		headerCell60.setCellValue(emlCount);
		headerCell60.setCellStyle(getHeaderCellStyle10(workbook));
		
		Row headerRow88 = sheet.getRow(15);
		if (headerRow88 == null) {
			headerRow88 = sheet.createRow(15);
		}
		Cell headerCell61 = headerRow88.createCell(11);
		headerCell61.setCellValue("COMPENSATION OFF");
		headerCell61.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell62 = headerRow88.createCell(12);
		headerCell62.setCellValue("COMP.OFF");
		headerCell62.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell63 = headerRow88.createCell(13);
		headerCell63.setCellValue(compoffCount);
		headerCell63.setCellStyle(getHeaderCellStyle10(workbook));
		
		Row headerRow89 = sheet.getRow(16);
		if (headerRow89 == null) {
			headerRow89 = sheet.createRow(16);
		}
		Cell headerCell64 = headerRow89.createCell(11);
		headerCell64.setCellValue("PUBLIC HOLIDAY");
		headerCell64.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell65 = headerRow89.createCell(12);
		headerCell65.setCellValue("PH");
		headerCell65.setCellStyle(getHeaderCellStyle10(workbook));

		Cell headerCell66 = headerRow89.createCell(13);
		headerCell66.setCellValue(pholidatcount);
		headerCell66.setCellStyle(getHeaderCellStyle10(workbook));



	}

	private static void publicHolidays(Sheet sheet, Workbook workbook, List<String> publicholiday,
			List<String> holidatType) throws ParseException {

		Map<List<String>, List<String>> map = new HashMap<>();
		map.put(publicholiday, holidatType);

		List<String> firstKey = map.keySet().iterator().next();

		if (!firstKey.isEmpty()) {
			String firstDate = firstKey.get(0);
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(firstDate);

			String outputDate = new SimpleDateFormat("MMM-yy").format(date);

			Row headerRow21 = sheet.getRow(25);
			if (headerRow21 == null) {
				headerRow21 = sheet.createRow(25);
			}
			Cell headerCell52 = headerRow21.createCell(11, CellType.STRING);
			headerCell52.setCellValue("PUBLIC HOLIDAYS");
			sheet.addMergedRegion(new CellRangeAddress(25, 25, 11, 13));
			int lastMergedRegionIndex1 = sheet.getNumMergedRegions() - 1;
			CellRangeAddress mergedRange17 = sheet.getMergedRegion(lastMergedRegionIndex1);
			for (int r = mergedRange17.getFirstRow(); r <= mergedRange17.getLastRow(); r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					row = sheet.createRow(r);
				}
				for (int c = mergedRange17.getFirstColumn(); c <= mergedRange17.getLastColumn(); c++) {
					Cell cell = row.getCell(c);
					if (cell == null) {
						cell = row.createCell(c);
					}

					cell.setCellStyle(getHeaderCellStyle4(workbook));
				}
			}

			Row headerRow22 = sheet.getRow(26);
			if (headerRow22 == null) {
				headerRow22 = sheet.createRow(26);
			}
			Cell headerCell53 = headerRow22.createCell(11, CellType.STRING);
			headerCell53.setCellValue("MONTH");
			headerCell53.setCellStyle(getHeaderCellStyle(workbook));
			Cell headerCell54 = headerRow22.createCell(12, CellType.STRING);
			headerCell54.setCellValue("HOLIDAY");
			headerCell54.setCellStyle(getHeaderCellStyle(workbook));
			Cell headerCell55 = headerRow22.createCell(13, CellType.STRING);
			headerCell55.setCellValue("DATE");
			headerCell55.setCellStyle(getHeaderCellStyle(workbook));

			int rowIndex = 27;
			for (List<String> keylist : map.keySet()) {
				List<String> keys = keylist;
				List<String> values = map.get(keylist);
				for (int i = 0; i < keys.size(); i++) {
					String keydate = keys.get(i);
					Date datekey = new SimpleDateFormat("yyyy-MM-dd").parse(keydate);
					String key = new SimpleDateFormat("dd-MM-yyyy").format(datekey);
					String value = values.get(i);

					int newrowindex = rowIndex++;
					Row row = sheet.getRow(newrowindex);
					if (row == null) {
						row = sheet.createRow(newrowindex);
					}
					Cell cell1 = row.createCell(11, CellType.STRING);
					cell1.setCellValue(outputDate);
					cell1.setCellStyle(getHeaderCellStyle2(workbook));
					Cell cell2 = row.createCell(12, CellType.STRING);
					cell2.setCellValue(value);
					cell2.setCellStyle(getHeaderCellStyle2(workbook));
					Cell cell3 = row.createCell(13, CellType.STRING);
					cell3.setCellValue(key);
					cell3.setCellStyle(getHeaderCellStyle2(workbook));
				}
			}
		}

	}

	@Override
	public List<String> sendTimesheetAutomationmail(String json, MultipartHttpServletRequest request) throws Throwable {

		List<String> list = new ArrayList<>();
		ObjectMapper obm = new ObjectMapper();
		ResponseEntity<byte[]> responseEntity = null;
		SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
		Session session = null;
		Session session1 = null;
		Session session2 = null;
		Session session3 = null;
		List<String> copied_with_success = new ArrayList<String>();
		Transaction transaction = null;

	try {
		TimesheetAutomation automation = obm.readValue(json, TimesheetAutomation.class);
		
		String empName = automation.getEmployeeName();
		String managerName = automation.getReportingManagerName();
		String empid = automation.getEmpId();
		String client = automation.getClient();
		if(client.equalsIgnoreCase("DAH2")) {
			managerName = "Padma";
		}
		if(client.equalsIgnoreCase("BANK OF JULIUS BAER")) {
			client = "BANK JULIUS BAER";
		}
		String clientId= automation.getClientId();
		String workcountry = automation.getWorkCountry();
		String contactname = null;
		String contactEmailID = null;
		String contactNumber = null;
		String clientWorkCountry = null;
		Date selectedMonth = sdfMonth.parse(automation.getLeaveMonth().toString());
		SimpleDateFormat sdfMonthYear = new SimpleDateFormat("MMM-yy", Locale.US);
		String monthYearString = sdfMonthYear.format(selectedMonth);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		 java.util.Date selectedDate = sdf.parse(automation.getLeaveMonth().toString());
		 Timestamp leaveMonths = new Timestamp(selectedDate.getTime());

		String to = automation.getReportingManagermailID();
		String[] cc = { automation.getEmpmailid()};
		
		
		//To avoid Duplicate TimeShhet Creation for same employee with same month
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String query = "SELECT * FROM `Timesheet_Automation_Status` WHERE employee_id =:employee_id  AND \r\n"
					+ "client_id =:client_id AND timesheet_month =:timesheet_month ";
			List<Timesheet_Automation_Status> tslist = session.createSQLQuery(query)
					.addEntity(Timesheet_Automation_Status.class)
					.setParameter("employee_id", empid)
					.setParameter("client_id", clientId)
					.setParameter("timesheet_month", leaveMonths)
					.list();
			if(tslist != null && !tslist.isEmpty()) {
				throw new Exception("Duplicate Entry: "+empName+" has Already Submitted timesheet for "+monthYearString);
			}
			
		} catch (Exception e) {
			if (transaction != null) {
	            transaction.rollback();
	        }
			e.printStackTrace();
			throw new Throwable(e.getMessage(), e);
		}
		
		//To get client workcountry from client details based on client id 
		/*
		 * try { session3 = sessionFactory.openSession(); transaction =
		 * session3.beginTransaction(); String query =
		 * "SELECT client_id,client_name,client_short_name,client_country FROM `client_details` WHERE client_id =:client_id"
		 * ; List<ClientDetails> clientdetails = session3.createSQLQuery(query)
		 * .setResultTransformer(Transformers.aliasToBean(ClientDetails.class))
		 * .setParameter("client_id", clientId) .list(); if(clientdetails != null &&
		 * !clientdetails.isEmpty()) { for(ClientDetails details :clientdetails ) {
		 * clientWorkCountry = details.getClient_country(); } }
		 * 
		 * } catch (Exception e) { if (transaction != null) { transaction.rollback(); }
		 * e.printStackTrace(); throw new Throwable(e.getMessage(), e); }
		 */
		
		
		if(workcountry != null) {
			
			if(workcountry.equalsIgnoreCase("India")) {
				String additionalEmail = "hrIndia@helius-tech.com"; 
		        String[] ccWithAdditionalEmail = Arrays.copyOf(cc, cc.length + 1);
		        ccWithAdditionalEmail[cc.length] = additionalEmail;
		        cc = ccWithAdditionalEmail;
		        contactname ="Padma";
		        contactEmailID = "padma@helius-tech.com";
		        contactNumber ="+91-7331144355";
		        
			}else if(workcountry.equalsIgnoreCase("Singapore")) {
				String additionalEmail = "timesheet@helius-tech.com"; 
		        String[] ccWithAdditionalEmail = Arrays.copyOf(cc, cc.length + 1);
		        ccWithAdditionalEmail[cc.length] = additionalEmail;
		        cc = ccWithAdditionalEmail;
		        contactname = "Ramu";
		        contactEmailID = "ramu@helius-tech.com";
		        contactNumber ="+91-7093904212";
			}
			
		}
		
		/*
		 * if(clientId.equalsIgnoreCase("381")) { String additionalEmail =
		 * "abhishek_rastogi@singlife.com"; String[] ccWithAdditionalEmail =
		 * Arrays.copyOf(cc, cc.length + 1); ccWithAdditionalEmail[cc.length] =
		 * additionalEmail; cc = ccWithAdditionalEmail; }
		 */
		
		//for Bank Of Singapore Client
		if (clientId.equalsIgnoreCase("300")) {
		    String clientmail = null;
		    String query = "SELECT * FROM Employee_Assignment_Details WHERE employee_id =:employee_id";
		    List<Employee_Assignment_Details> clientlist = session.createSQLQuery(query)
		            .addEntity(Employee_Assignment_Details.class)
		            .setParameter("employee_id", empid)
		            .list();

		    if (clientlist != null && !clientlist.isEmpty()) {
		        clientmail = clientlist.get(0).getClient_email_id();
		    }

		    if (clientmail != null) {
		        String additionalEmail = clientmail;
		        String[] ccWithAdditionalEmails = Arrays.copyOf(cc, cc.length + 1);
		        ccWithAdditionalEmails[cc.length] = additionalEmail;
		        cc = ccWithAdditionalEmails;
		    }
		}
		
		//To Cretate Timesheet with Given JSON data
		try {
			responseEntity =  createAutomationTimesheet(json, request);
			 if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
			        throw new Exception("Timesheet creation failed with given Data");
			    }
		} catch (Exception e) {
			e.printStackTrace();
			throw new Throwable(e.getMessage(), e);
		}
		
		//list.add("Total Working Days :"+Integer.toString(total_days_count));
		//list.add("Total Days Worked :"+Double.toString(present_days));
		//list.add("Total Leaves Taken :"+Double.toString(total_leave_days));
		
		list.add("Timesheet Automation Process is completed successfuly for "+empName );
		

		// To get Automation Timesheet from server

		byte[] files = null;
		FileInputStream fi = null;
		String clientfilelocation = null;
		List<String> urlList = new ArrayList<String>();

		try {
			if ("no".equalsIgnoreCase(awsCheck)) {
				clientfilelocation = Utils.getProperty("fileLocation") + File.separator + "timesheet_details"
						+ File.separator + monthYearString +File.separator  + empName + "_" + client + "_" + "AutomationTimesheet.xlsx";
				fi = new FileInputStream(clientfilelocation);
				files = IOUtils.toByteArray(fi);
				fi.close();
			}
			if ("yes".equalsIgnoreCase(awsCheck)) {
				clientfilelocation = "timesheet_details" + File.separator + monthYearString + File.separator +empName + "_" + client + "_"
						+ "AutomationTimesheet.xlsx";
				 //clientfilelocation = "timesheet_details" +"/"+ monthYearString+"/"+ empName + "_" + client + "_" +
				 //"AutomationTimesheet.xlsx";
				files = Utils.downloadFileByAWSS3Bucket(clientfilelocation);
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			transaction.rollback();
			throw new Throwable("Unable to get Automation Timesheet from server " + e1.getMessage(), e1);
		} catch (IOException e) {
			e.printStackTrace();

		}

		String filelocation = clientfilelocation;
		urlList.add(filelocation);
		
		
		/*
			//To save/upadte Leave Details Into DB
			try {
				session1= sessionFactory.openSession();
				transaction = session1.beginTransaction();
				Map<String, MultipartFile> files12 = request.getFileMap();

				List<Leave_Record_Details> leave_Record_Details = new ArrayList<>();
				List<LeaveDetails> leaveDetails = automation.getLeaveDetails();
				Map<String, String> templateFilenames = new HashMap<String, String>();

				for (LeaveDetails leaveDetails2 : leaveDetails) {
					Leave_Record_Details record_Details = new Leave_Record_Details();
					record_Details.setEmployee_id(automation.getEmpId());
					record_Details.setLeave_record_details_id(leaveDetails2.getLeave_record_details_id());
					record_Details.setClient_id(Integer.parseInt(automation.getClientId()));
					record_Details.setType_of_leave(leaveDetails2.getType_of_leave());
					record_Details.setAmpm(leaveDetails2.getAmpm());
					record_Details.setRemarks(leaveDetails2.getRemarks());
					record_Details.setLeaveMonth(automation.getLeaveMonth());
					record_Details.setStartdate(leaveDetails2.getStartdate());
					record_Details.setEnddate(leaveDetails2.getEnddate());
					record_Details.setLeaves_used(leaveDetails2.getLeaves_used());
					record_Details.setLeaveRecordPath(leaveDetails2.getLeaveRecordPath());
					record_Details.setCreated_by(leaveDetails2.getCreated_by());

					leave_Record_Details.add(record_Details);
				}

				LocalDate previousMonth = null;
				boolean isleaveupdated = false;
				if (automation.getLeaveDetails() != null) {
					Iterator<Leave_Record_Details> itr = leave_Record_Details.iterator();

					while (itr.hasNext()) {
						Leave_Record_Details leaverecord = itr.next();
						java.util.Date selectedMonth1 = sdfMonth.parse(leaverecord.getLeaveMonth().toString());
						leaverecord.setLeaveMonth(new Timestamp(selectedMonth1.getTime()));
						Timestamp lmonth = new Timestamp(selectedMonth1.getTime());
						int recordDetailId = leaverecord.getLeave_record_details_id();
						String leaveRecordQuery = "SELECT a.employee_id,a.type_of_leave, SUM(a.leaves_used) AS total FROM  Leave_Record_Details a WHERE "
								+ "DATE(leaveMonth) = '" + lmonth + "' AND type_of_leave = '"
								+ leaverecord.getType_of_leave() + "' " + "AND employee_id= '"
								+ leaverecord.getEmployee_id() + "' AND client_id='" + leaverecord.getClient_id() + "'";
						List<Object[]> recordList = session1.createSQLQuery(leaveRecordQuery).list();
						float totalleaveUsed = 0;
						if (recordList != null) {
							for (Object[] obj : recordList) {
								if (obj[0] != null && obj[1] != null && obj[2] != null) {
									totalleaveUsed = Float.parseFloat(obj[2].toString());
								}

							}
						}
						if (leaverecord.getLeave_record_details_id() == 0) {
							if (leaverecord.getLeaveRecordPath() != null) {
								if (files12.values().size() > 0) {

									String url = leaverecord.getEmployee_id() + "_" + client + "_"
											+ leaverecord.getLeaveRecordPath();
									templateFilenames.put(leaverecord.getLeaveRecordPath(), url);
									leaverecord.setLeaveRecordPath(url);
								}
							}

							session1.save(leaverecord);
						} else {
							if (leaverecord.getLeaveRecordPath() != null) {
								if (files12.values().size() > 0) {
									String url = leaverecord.getEmployee_id() + "_" + client + "_"
											+ leaverecord.getLeaveRecordPath();
									templateFilenames.put(leaverecord.getLeaveRecordPath(), url);
									leaverecord.setLeaveRecordPath(url);
								}
							}
							session1.evict(leaverecord);
							session1.merge(leaverecord);
						}
						Timestamp leaveMonth = new Timestamp(selectedMonth1.getTime());
						LocalDateTime localDateTime = leaveMonth.toLocalDateTime();
						previousMonth = localDateTime.toLocalDate();
						LocalDate currenMonth = previousMonth.plusMonths(1);
						LocalDate localdatenowMonth = LocalDate.now().with(firstDayOfMonth());

						if (currenMonth.isEqual(localdatenowMonth) || previousMonth.isEqual(localdatenowMonth)) {
							calcLeaveUsageBasedOnLeaveRecord(session1, leaverecord, totalleaveUsed, recordDetailId);
							isleaveupdated = true;
						}
					}
					if (files12.values().size() > 0) {
						FilecopyStatus status = Utils.copyFiles(request, templateFilenames, "leaverecords");
						copied_with_success = status.getCopied_with_success();
					}
				}

				transaction.commit();

			} catch (Exception e) {
				e.printStackTrace();
				if (transaction != null) {
		            transaction.rollback();
		        }

				throw new Throwable("Unable to save leave record data into DB  " + e.getMessage(), e);
			}
		*/
	
		
		
		// save leave attachemant into  server
			
		try {
			Map<String, MultipartFile> files12 = request.getFileMap();
			List<LeaveDetails> leaveDetails = automation.getLeaveDetails();
			Map<String, String> templateFilenames = new HashMap<String, String>();

			for (LeaveDetails details : leaveDetails) {

				if (details.getLeaveRecordPath() != null && !details.getLeaveRecordPath().isEmpty()) {
					if (files12.values().size() > 0) {
						String url = empName + "_" + client + "_" + monthYearString + "_" + details.getLeaveRecordPath();
						templateFilenames.put(details.getLeaveRecordPath(), url);
						FilecopyStatus status = Utils.copyFiles(request, templateFilenames,
								"timesheetLeaveAttachaments");
						copied_with_success = status.getCopied_with_success();

					}

				}

			}

		} catch (Exception e) {
			e.printStackTrace();

			throw new Throwable("Unable to save leave Attachament file into server " + e.getMessage(), e);
		}
		
		
			// To get leave Attachaments From Server
			for (LeaveDetails leaveDetails2 : automation.getLeaveDetails()) {
				if (leaveDetails2.getLeaveRecordPath() != null) {

					byte[] file1 = null;
					FileInputStream fi1 = null;
					String clientfilelocation1 = null;
					try {
						if ("no".equalsIgnoreCase(awsCheck)) {
							clientfilelocation1 = Utils.getProperty("fileLocation") + File.separator + "timesheetLeaveAttachaments"
									+ File.separator + empName + "_" + client + "_" + monthYearString +"_"+ leaveDetails2.getLeaveRecordPath();
							fi1 = new FileInputStream(clientfilelocation1);
							file1 = IOUtils.toByteArray(fi1);
							fi1.close();
						}
						if ("yes".equalsIgnoreCase(awsCheck)) {
							clientfilelocation1 = "timesheetLeaveAttachaments" + File.separator + empName + "_" + client + "_"+ monthYearString +"_"
								+ leaveDetails2.getLeaveRecordPath();
						 //clientfilelocation1 = "timesheetLeaveAttachaments"+ "/" + empName + "_" + client + "_"+ monthYearString +"_" +
							//leaveDetails2.getLeaveRecordPath();
							file1 = Utils.downloadFileByAWSS3Bucket(clientfilelocation1);
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
						transaction.rollback();
						throw new Throwable("Unable to get leave Attachment's from server" + e1.getMessage(), e1);
					} catch (IOException e) {
						e.printStackTrace();
						throw e;

					}
					String filelocation1 = clientfilelocation1;
					urlList.add(filelocation1);
				}
			}
			

			//To save Timesheet data Into DB
			try {
				
				session2 = sessionFactory.openSession();
				transaction = session2.beginTransaction();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				if(automation != null) {
					Timesheet_Automation_Status automation_Status = new Timesheet_Automation_Status();
					
					automation_Status.setEmployee_id(empid);
					automation_Status.setEmployee_name(empName);
					automation_Status.setClient_id(automation.getClientId());
					automation_Status.setClient_name(client);
					automation_Status.setTimesheet_email(automation.getEmpmailid());
					automation_Status.setTimesheet_month(leaveMonths);
					automation_Status.setTimesheet_upload_path(clientfilelocation);
					automation_Status.setSubmited_date(timestamp);
					
				/*	if(clientfilelocation.contains("\\")){
					String clientFileLocation = clientfilelocation.replace("\\", "/");
					automation_Status.setTimesheet_upload_path(clientFileLocation);
					}else{
						automation_Status.setTimesheet_upload_path(clientfilelocation);
					}
					System.out.println("clientfilelocation: "+clientfilelocation);
					automation_Status.setSubmited_date(timestamp);*/
					
					if(automation_Status != null ) {
						session2.save(automation_Status);
						//transaction.commit();	
					}
				}				
			/*} catch (Exception e) {
				e.printStackTrace();
				if (transaction != null) {
		            transaction.rollback();
		        }

				throw new Throwable("Unable to save Automation Timesheet Data into DB  " + e.getMessage(), e);
			}
			*/
			
			//To send Mail to corresponding person 
			String subject = "Helius Timesheet Approval Request - " + monthYearString +" - "+empName;

			String text = "THIS IS A SYSTEM GENERATED EMAIL AND PLEASE USE REPLY ALL WHILE RESPONDING TO THE EMAIL" + "\n\n"

					+ "Dear " + managerName + ",\n\n" 
					+ "This timesheet is for " + empName +" working through Helius Technologies in your team with "+client +"\n\n"
					
					+"This timesheet  has been generated through our internal automated system based"
					+ " on the attendance and leave inputs given by the employee in our self service "
					+ "portal, along with required evidence attachments (where required). This currently works"
					+ " for only India based employees and shortly will be expanded to all employees"+ "\n\n"
					
					
					+"If you are ready to APPROVE the timesheet, please state APPROVED and  press REPLY ALL to this email."+"\n\n"
					
					+"In case you are queries and hence cannot Approve, please state REJECTED and press REPLY ALL to this email."
					+ " We would be grateful if you can state the reasons for the rejection as well, so that the employee"
					+ " can immediately rectify/ clarify as needed" +"\n\n"
					
					+"In case of queries or clarifications, please contact "+contactname+" on "+contactEmailID +" or "+ contactNumber
					+ "\n\n" + "Kind regards," + "\n"
					+ "Helius - Time sheet processing team" ;

			service.sendMessageWithAttachmentForTimesheet(to, cc, subject, text, urlList, client);
			
			  if (transaction != null && !transaction.wasCommitted() && !transaction.wasRolledBack()) {
			        transaction.commit();
			    }	

			} catch (Exception e) {
				e.printStackTrace();
				if (transaction != null && !transaction.wasCommitted()) {
		            transaction.rollback();
		        }

				throw new Throwable("Unable to save Automation Timesheet Data into DB  " + e.getMessage(), e);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			transaction.rollback();
			throw new Throwable(e.getMessage(), e);

		} finally {
			if (session != null && session.isOpen()) {
	            session.close();
	        }
	        if (session1 != null && session1.isOpen()) {
	            session1.close();
	        }
	        if (session2 != null && session2.isOpen()) {
	            session2.close();
	        }
	        if (session3 != null && session3.isOpen()) {
	            session3.close();
	        }
		}
		return list;
	}

	private void calcLeaveUsageBasedOnLeaveRecord(Session session, Leave_Record_Details leaverecord,
			float totalLeaveUsed, int recordDetailId) throws Throwable {

		String employeeId = leaverecord.getEmployee_id();
		Timestamp leaveMonth = leaverecord.getLeaveMonth();
		String type_of_leave = leaverecord.getType_of_leave();
		int clientId = leaverecord.getClient_id();
		float leaveUsed = leaverecord.getLeaves_used();
		LocalDateTime localDateTime = leaveMonth.toLocalDateTime();
		LocalDate previousMonth = localDateTime.toLocalDate();
		LocalDate currenMonth = previousMonth.plusMonths(1);

		try {

			String leaveusageQuery = "SELECT * FROM  Leave_Usage_Details WHERE DATE(usageMonth)= :leaveMonth  AND type_of_leave= :typeofLeave AND employee_id= :empId ";
			List<Leave_Usage_Details> leaveusage = session.createSQLQuery(leaveusageQuery)
					.addEntity(Leave_Usage_Details.class).setParameter("leaveMonth", leaveMonth)
					.setParameter("typeofLeave", type_of_leave).setParameter("empId", employeeId).list();
			// upadate leavehistory in leave usage Details
			float previousMonthLeaveUsed = 0;
			if (leaveusage != null && !leaveusage.isEmpty()) {
				for (Leave_Usage_Details usageDetails : leaveusage) {
					if (usageDetails.getType_of_leave().equalsIgnoreCase(type_of_leave)
							&& usageDetails.getClient_id() == clientId
							&& usageDetails.getEmployee_id().equals(employeeId)) {
						if (recordDetailId == 0) {
							previousMonthLeaveUsed = totalLeaveUsed + leaveUsed;
							usageDetails.setLeaves_used(previousMonthLeaveUsed);
							session.saveOrUpdate(usageDetails);
						} else {
							previousMonthLeaveUsed = leaveUsed;
							usageDetails.setLeaves_used(previousMonthLeaveUsed);
							session.saveOrUpdate(usageDetails);
						}
					}
				}
			}
			// calculate and update Leave usage opening balance
			LocalDate currentMonthDate = LocalDate.now().with(firstDayOfMonth());
			if (currenMonth.isEqual(currentMonthDate)) {
				Timestamp usageMonth = Timestamp.valueOf(currenMonth.atStartOfDay());
				String usageQuery = "SELECT * FROM  Leave_Usage_Details WHERE DATE(usageMonth)= :leaveMonth  AND type_of_leave= :typeofLeave AND employee_id= :empId ";
				List<Leave_Usage_Details> usageDetails = session.createSQLQuery(usageQuery)
						.addEntity(Leave_Usage_Details.class).setParameter("leaveMonth", usageMonth)
						.setParameter("typeofLeave", type_of_leave).setParameter("empId", employeeId).list();
				if (usageDetails != null && !usageDetails.isEmpty()) {
					for (Leave_Usage_Details calcusage : usageDetails) {
						if (recordDetailId == 0) {
							float leaveAccrual = calcusage.getLeaves_accrued();
							calcusage.setLeaves_accrued(leaveAccrual - leaveUsed);
							session.saveOrUpdate(calcusage);
						} else {
							float leaveAccrual = calcusage.getLeaves_accrued();
							float accrual = leaveAccrual + totalLeaveUsed;
							calcusage.setLeaves_accrued(accrual - leaveUsed);
							session.saveOrUpdate(calcusage);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public ResponseEntity<byte[]> getTimesheet(String timesheetMonth,String empId,String EmpName,String Client ) throws Throwable {

		ResponseEntity<byte[]> responseEntity = null;
		SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
		Session session = null;
		Transaction transaction = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdfday.parse(timesheetMonth));
		int givenDate = cal.get(Calendar.DAY_OF_MONTH);

		if (!(givenDate >= 25 && givenDate <= 31)) {
			cal.add(Calendar.MONTH, -1);
		}
		Date selectedMonth = cal.getTime();
		SimpleDateFormat sdfMonthYear = new SimpleDateFormat("MMM-yy", Locale.US);
		String monthYearString = sdfMonthYear.format(selectedMonth);
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			Workbook workbook = getXSSFWorkbook();
			Sheet sheet = workbook.getSheet("TimeSheet");

			Row headerRow11 = sheet.getRow(1);
			Cell headerCell3 = headerRow11.getCell(3);
			//headerCell3.setCellValue("");
			headerCell3.setCellValue(empId);
			Row headerRow21 = sheet.getRow(2);
			Cell headerCell5 = headerRow21.getCell(3);
			//headerCell5.setCellValue("");
			headerCell5.setCellValue(EmpName);
			Row headerRow31 = sheet.getRow(3);
			Cell headerCell7 = headerRow31.getCell(3);
			//headerCell7.setCellValue("");
			headerCell7.setCellValue(Client);
			Row headerRow41 = sheet.getRow(4);
			Cell headerCell9 = headerRow41.getCell(3);
			headerCell9.setCellValue("");
			Row headerRow51 = sheet.getRow(5);
			Cell headerCell11 = headerRow51.getCell(3);
			headerCell11.setCellValue(monthYearString);
			Row headerRow81 = sheet.getRow(6);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(selectedMonth);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			Date startDayOfMonth = calendar.getTime();
			int firstDay = calendar.get(Calendar.DAY_OF_MONTH);
			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date endDayOfMonth = calendar.getTime();
			int lastDay = calendar.get(Calendar.DAY_OF_MONTH);

			LocalDate localDate = selectedMonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate firstDayOfMonth = localDate.withDayOfMonth(1);

			List<String> publicholiday = new ArrayList<>();
			List<String> PHday = new ArrayList<>();
			String client_id = "258";
			try {

				String checklist_query = " SELECT client_id,holiday_name,holiday_date FROM Holiday_Master WHERE client_id =:client_id  AND DATE(holiday_date) \r\n"
						+ "  BETWEEN STR_TO_DATE(:fromdate,'%Y-%m-%d') AND :thrudate";

				Query emplist = session.createSQLQuery(checklist_query)
						.setResultTransformer(Transformers.aliasToBean(TimesheetAutomationHolidays.class))
						.setParameter("client_id", client_id).setParameter("fromdate", startDayOfMonth)
						.setParameter("thrudate", endDayOfMonth);

				List<Object> results = emplist.list();

				for (Object checklist : results) {
					TimesheetAutomationHolidays items = (TimesheetAutomationHolidays) checklist;
					publicholiday.add(items.getHoliday_date().toString());
					PHday.add(items.getHoliday_name());
				}

			} catch (Exception e) {
				e.printStackTrace();

				return responseEntity = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
			}

			List<LocalDate> publicHolidayDates = publicholiday.stream()
					.map(ss -> LocalDate.parse(ss, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS][.S]")))
					.collect(Collectors.toList());

			int rowIndex = 7;
			for (int i = firstDay - 1; i < lastDay; i++) {
				LocalDate date = firstDayOfMonth.plusDays(i);
				int newrowindex = rowIndex++;
				Row row = sheet.createRow(newrowindex);
				Cell cell = row.createCell(1, CellType.STRING);

				sheet.addMergedRegion(new CellRangeAddress(newrowindex, newrowindex, 1, 2));
				CellRangeAddress mergedRange14 = sheet.getMergedRegion(13);
				cell.setCellValue(date.format(dateFormatter));
				for (int r = mergedRange14.getFirstRow(); r <= mergedRange14.getLastRow(); r++) {
					Row row1 = sheet.getRow(r);
					if (row1 == null) {
						row1 = sheet.createRow(r);
					}
					for (int c = mergedRange14.getFirstColumn(); c <= mergedRange14.getLastColumn(); c++) {
						Cell cell1 = row.getCell(c);
						if (cell1 == null) {
							cell1 = row.createCell(c);
						}

						if (publicHolidayDates.contains(date)) {
							cell1.setCellStyle(getHeaderCellStyle5(workbook));

						} else {
							cell1.setCellStyle(getHeaderCellStyle2(workbook));
						}
					}
				}
				cell = row.createCell(3, CellType.STRING);
				sheet.addMergedRegion(new CellRangeAddress(newrowindex, newrowindex, 3, 4));
				CellRangeAddress mergedRange15 = sheet.getMergedRegion(14);

				cell.setCellValue(date.format(dayFormatter));
				String day = date.format(dayFormatter);
				for (int r = mergedRange15.getFirstRow(); r <= mergedRange15.getLastRow(); r++) {
					Row row1 = sheet.getRow(r);
					if (row1 == null) {
						row1 = sheet.createRow(r);
					}
					for (int c = mergedRange15.getFirstColumn(); c <= mergedRange15.getLastColumn(); c++) {
						Cell cell1 = row.getCell(c);
						if (cell1 == null) {
							cell1 = row.createCell(c);
						}
						cell1.setCellStyle(getHeaderCellStyle2(workbook));

					}
				}
				cell = row.createCell(5, CellType.STRING);
				cell.setCellStyle(getHeaderCellStyle2(workbook));

				cell = row.createCell(6, CellType.STRING);
				cell.setCellStyle(getHeaderCellStyle2(workbook));
				cell = row.createCell(7, CellType.STRING);
				cell.setCellValue(0);
				cell.setCellStyle(getHeaderCellStyle2(workbook));

				cell = row.createCell(8, CellType.STRING);
				cell.setCellValue(0);
				cell.setCellStyle(getHeaderCellStyle2(workbook));

				cell = row.createCell(9, CellType.STRING);
				cell.setCellStyle(getHeaderCellStyle2(workbook));

				if (publicHolidayDates.contains(date)) {
					cell = row.createCell(5, CellType.STRING);
					cell.setCellValue("PH");
					cell.setCellStyle(getHeaderCellStyle6(workbook));
					cell = row.createCell(6, CellType.STRING);
					cell.setCellStyle(getHeaderCellStyle6(workbook));

				}
			}

			description(sheet, workbook, headerRow11, headerRow21, headerRow31, headerRow41, headerRow51, headerRow81,
					0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0,0,null);
			totalWorkingDays(sheet, workbook, 0, 0, 0);
			noteMessage(sheet, workbook);
			remarks(sheet, workbook, null);
			shiftDetails(sheet, workbook);
			publicHolidays(sheet, workbook, publicholiday, PHday);

			// Resize columns to fit content
			for (int j = 0; j < 14; j++) {
				sheet.autoSizeColumn(j);
			}

			byte[] workbookBytes;
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				workbook.write(outputStream);
				workbookBytes = outputStream.toByteArray();
				outputStream.close();
			} catch (IOException e) {
				throw new Throwable("Error converting Workbook to byte array", e);
			}
			

			// Set the response headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", "timesheet.xlsx");

			responseEntity = new ResponseEntity<>(workbookBytes, headers, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> errorDetails = new HashMap<>();
			errorDetails.put("message", "Failed to Generate Timesheet File");
			String json = new ObjectMapper().writeValueAsString(errorDetails);
			return responseEntity = ResponseEntity.<byte[]>status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(json.getBytes());

		} finally {
			session.close();
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<byte[]> downloadsTimesheet(String empId, String clientId, String timesheetMonth) throws Throwable {
		
		// To get Automation timesheet from server

					byte[] files = null;
					FileInputStream fi = null;
					String clientfilelocation = null;
					ResponseEntity<byte[]> responseEntity = null;
					SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
					Date selectedMonth = sdfMonth.parse(timesheetMonth);
					SimpleDateFormat sdfMonthYear = new SimpleDateFormat("MMM-yy", Locale.US);
					String monthYearString = sdfMonthYear.format(selectedMonth);
					try {
						if ("no".equalsIgnoreCase(awsCheck)) {
							clientfilelocation = Utils.getProperty("fileLocation") + File.separator + "timesheet_details"+File.separator + monthYearString 
									+ File.separator + empId + "_" + clientId + "_" + "AutomationTimesheet.xlsx";
							fi = new FileInputStream(clientfilelocation);
							files = IOUtils.toByteArray(fi);
							fi.close();
						}
						if ("yes".equalsIgnoreCase(awsCheck)) {
							clientfilelocation = "timesheet_details" + File.separator + monthYearString +File.separator  + empId + "_" + clientId + "_"
									+ "AutomationTimesheet.xlsx";
							//clientfilelocation ="timesheet_details" + "/" +monthYearString +"/" + empId + "_" + clientId + "_" +
							// "AutomationTimesheet.xlsx";
							files = Utils.downloadFileByAWSS3Bucket(clientfilelocation);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, String> errorDetails = new HashMap<>();
						errorDetails.put("message",
								"Failed to Download  Automation Timesheet File :Failed to Get File From Server : "+e.getMessage());
						String json = new ObjectMapper().writeValueAsString(errorDetails);
						return responseEntity = ResponseEntity.<byte[]>status(HttpStatus.NOT_FOUND).body(json.getBytes());
					}
					
		return responseEntity = new ResponseEntity<byte[]>(files, HttpStatus.OK);
	}
	
	
	
	// This method is to retrive Client Public Holidays by Month 

	/*
	 * @Override public Map<String, String> getPublicHolidays(String month, String
	 * clientId) throws Throwable {
	 * 
	 * SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd"); Session session
	 * = null; Transaction transaction = null; Calendar cal =
	 * Calendar.getInstance(); cal.setTime(sdfday.parse(month)); Date selectedMonth
	 * = cal.getTime(); Calendar calendar = Calendar.getInstance();
	 * calendar.setTime(selectedMonth); calendar.set(Calendar.DAY_OF_MONTH, 1); Date
	 * startDayOfMonth = calendar.getTime(); calendar.set(Calendar.DAY_OF_MONTH,
	 * calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); Date endDayOfMonth =
	 * calendar.getTime(); Map<String, String> map = new HashMap<>(); try { session
	 * = sessionFactory.openSession(); transaction = session.beginTransaction();
	 * 
	 * String client_id = clientId;
	 * 
	 * try {
	 * 
	 * String checklist_query =
	 * " SELECT client_id,holiday_name,holiday_date FROM Holiday_Master WHERE client_id =:client_id  AND DATE(holiday_date) \r\n"
	 * + "  BETWEEN STR_TO_DATE(:fromdate,'%Y-%m-%d') AND :thrudate";
	 * 
	 * Query emplist = session.createSQLQuery(checklist_query)
	 * .setResultTransformer(Transformers.aliasToBean(TimesheetAutomationHolidays.
	 * class)) .setParameter("client_id", client_id).setParameter("fromdate",
	 * startDayOfMonth) .setParameter("thrudate", endDayOfMonth);
	 * 
	 * List<Object> results = emplist.list();
	 * 
	 * for (Object checklist : results) { TimesheetAutomationHolidays items =
	 * (TimesheetAutomationHolidays) checklist;
	 * map.put(items.getHoliday_date().toString(), items.getHoliday_name()); } }
	 * catch (Exception e) { throw new
	 * Throwable("Exception while fetching Client Public Holidays :" +
	 * e.getMessage()); } } catch (Exception e) { e.printStackTrace(); throw new
	 * Throwable("Failed to get Client Public Holidays:" + e.getMessage()); }
	 * 
	 * return map; }
	 */
}
