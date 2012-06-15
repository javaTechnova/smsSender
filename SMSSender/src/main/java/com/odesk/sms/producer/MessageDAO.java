package com.odesk.sms.producer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.odesk.sms.Message;

/**
 * Single threaded poller for retrieving messages from database
 * and populating the internal queue
 * @author jayant
 *
 */
@Component("messageDao")
@Transactional
public class MessageDAO {

	private JdbcTemplate jdbcTemplate;
	
	static private final Log log = LogFactory.getLog(MessageDAO.class); 
	
	private final String QUERY_SQL = "select `smsID`," +
									 "`deliveryID`,`userID`,`status`,`to`,`from`,`credits`,`message`,`date`,`date2` from messages " +
									 "where smsID > ? and status =?;"; 
	
	@Autowired
	public void setDataSource(DataSource source){
		this.jdbcTemplate = new JdbcTemplate(source);
	}
	
	// last sms fetched from database,updated after each poll
	private int lastIdFetched;
	
	// number of messages to fetch in single poll
	@Value("${message.fetch.size}")
	private int messageFetchSize;
	
	// message with status to poll
	@Value("${message.status}")
	private String messageStatus;
	
	// called by spring bean processor when beans are initialized
	public void init(){
		jdbcTemplate.setMaxRows(messageFetchSize);
		// fetch lastMessageId		
		
		lastIdFetched = jdbcTemplate.queryForInt("select min(smsID) from messages where status='0'");
		if(lastIdFetched > 0){
			--lastIdFetched;
		}
		log.info("init() - Last Message Id to pull messages from ="+lastIdFetched);
	}
	
	/**
	 * Update the message status and delivery information 
	 * upon successful submission to 5 cents
	 * @param message
	 */
	public void updateMessage(Message message){
		log.debug("Persisting message: status="+message.getStatus()+" and deliveryId="+message.getDeliveryId());
		int update= jdbcTemplate.update("update messages set status=?,deliveryID=? where smsID=?"
					,new Object[]{message.getStatus(),message.getDeliveryId(),message.getId()}
					,new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.INTEGER});
		if(update!=1){
			log.warn("Message not updated, please review !!!");
		}
		log.debug("Message successfully updated");
	}
	
	/**
	 * Fetches fixed number of messages on every poll 
	 * @return
	 */
	public List<Message> fetchMessages(){
		
			List<Message> messages =  jdbcTemplate.query(new PreparedStatementCreator(){

				@Override
				public PreparedStatement createPreparedStatement(Connection conn)
						throws SQLException {
					PreparedStatement ps =
		                conn.prepareStatement(QUERY_SQL);
					ps.setInt(1, lastIdFetched);
					ps.setString(2,messageStatus);
					return ps;
				}
				
			},new RowMapper<Message>(){

				@Override
				public Message mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					Message msg = new Message();
					msg.setId(rs.getInt("smsID"));
					msg.setTo(rs.getString("to"));
					msg.setFrom(rs.getString("from"));
					msg.setDeliveryId(rs.getString("deliveryID"));
					msg.setUserId(rs.getInt("userId"));
					msg.setCredits(rs.getInt("credits"));
					msg.setStatus(rs.getString("status"));
					msg.setMessage(rs.getString("message"));
					msg.setDate(rs.getString("date"));
					msg.setDate2(rs.getString("date2"));
					msg.setInTime(System.currentTimeMillis());
					return msg;
				}
				
			});
			// storing the lastId fetched
			if(messages.size() > 0){
				lastIdFetched = messages.get(messages.size()-1).getId();
				log.info("Number of Messages retrieved ="+messages.size()
						+ " Last Message Id fetched from database ="+lastIdFetched);
			}else{
				log.info("Scheduled Poll did not fetch any new messages, last fetched ID= "+lastIdFetched);
			}
			
		return messages;
	}
	
	public void setLastIdFetched(int lastIdFetched) {
		this.lastIdFetched = lastIdFetched;
	}

	public void setMessageFetchSize(int messageFetchSize) {
		this.messageFetchSize = messageFetchSize;
	}

	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}
}
