package com.odesk.sms;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring.xml")
public class MessageInsertUtility {

	@Autowired
	 private ApplicationContext appContext;
	
	@Test
	public void insertRecords(){
		JdbcTemplate t = new JdbcTemplate(appContext.getBean(DataSource.class));
		for(int i=0;i<100;i++){
			t.execute("insert into messages (deliveryID,userID,status,credits,`to`,`from`,message,`date`,date2) " +
			"values ('',456,'0',7,'0411222333','0411222333','Blank Test & Message','','')");
		}
	}

}
