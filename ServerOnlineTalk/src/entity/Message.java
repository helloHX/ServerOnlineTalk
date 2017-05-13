package entity;

import annotation.Column;
import annotation.Entity;

@Entity(tabName="message")
public class Message {
	@Column(columnName="MessageID",nullable=false,length=255)
	private String messageId;
	@Column(columnName="Message",nullable=false,length=255)
	private String message;
	@Column(columnName="MessageCreateTime",nullable=false,length=255)
	private String messageTime;
	@Column(columnName="FromID",nullable=false,length=255)
	private String formId;
	@Column(columnName="ToID",nullable=false,length=255)
	private String toId;
	@Column(columnName="Status",nullable=false,length=5)
	private int status;
	@Column(columnName="messageType",nullable=false,length=255)
	private String messageType;
	
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessageTime() {
		return messageTime;
	}
	public void setMessageTime(String messageTime) {
		this.messageTime = messageTime;
	}
	public String getFormId() {
		return formId;
	}
	public void setFormId(String formId) {
		this.formId = formId;
	}
	public String getToId() {
		return toId;
	}
	public void setToId(String toId) {
		this.toId = toId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
}
