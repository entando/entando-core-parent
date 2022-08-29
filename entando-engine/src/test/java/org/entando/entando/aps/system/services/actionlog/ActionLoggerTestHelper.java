/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.entando.entando.aps.system.services.actionlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import javax.sql.DataSource;

import org.entando.entando.aps.system.services.actionlog.model.ActionLogRecord;
import org.entando.entando.aps.system.services.actionlog.model.ActionLogRecordSearchBean;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.springframework.context.ApplicationContext;

import com.agiletec.aps.system.common.AbstractDAO;

/**
 * @author E.Santoboni
 */
public class ActionLoggerTestHelper extends AbstractDAO {

	private static final EntLogger _logger =  EntLogFactory.getSanitizedLogger(ActionLoggerTestHelper.class);
	
	public ActionLoggerTestHelper(ApplicationContext applicationContext) {
		DataSource dataSource = (DataSource) applicationContext.getBean("servDataSource");
		this.setDataSource(dataSource);
		
		ActionLogDAO actionLoggerDAO = new ActionLogDAO();
		actionLoggerDAO.setDataSource(dataSource);
		this._actionLoggerDAO = actionLoggerDAO;
	}
	
	public void addActionRecord(ActionLogRecord actionRecord) {
		this._actionLoggerDAO.addActionRecord(actionRecord);
	}
	
	public void cleanRecords() {
		Connection conn = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			this.deleteRecords(conn, DELETE_LOG_RECORD_RELATIONS);
			this.deleteRecords(conn, DELETE_LOG_RECORDS);
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			_logger.error("Error on delete records",  t);
			throw new RuntimeException("Error on delete records", t);
			//processDaoException(t, "Error on delete records" , "cleanRecords");
		} finally {
			closeConnection(conn);
		}
	}
	
	public void deleteRecords(Connection conn, String query) {
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement(query);
			stat.executeUpdate();
		} catch (Throwable t) {
			_logger.error("Error on delete records: {}", query,  t);
			throw new RuntimeException("Error on delete records", t);
		} finally {
			closeDaoResources(null, stat);
		}
	}
	
	public ActionLogRecord createActionRecord(int id, String username, 
			String actionName, String namespace, Date date, String parameter) {
		ActionLogRecord record = new ActionLogRecord();
		record.setId(id);
		record.setUsername(username);
		record.setActionName(actionName);
		record.setNamespace(namespace);
		record.setActionDate(date);
		record.setParameters(parameter);
		return record;
	}
	
	public ActionLogRecordSearchBean createSearchBean(String username, String actionName, 
			String namespace, String params, Date start, Date end) {
		ActionLogRecordSearchBean searchBean = new ActionLogRecordSearchBean();
		searchBean.setUsername(username);
		searchBean.setActionName(actionName);
		searchBean.setNamespace(namespace);
		searchBean.setParams(params);
		searchBean.setStartCreation(start);
		searchBean.setEndCreation(end);
		return searchBean;
	}
	
	private static final String DELETE_LOG_RECORDS = 
		"DELETE from actionlogrecords";
	private static final String DELETE_LOG_RECORD_RELATIONS = 
		"DELETE from actionlogrelations";
	
	private IActionLogDAO _actionLoggerDAO;
	
}