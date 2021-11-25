package pt.isec.pd_g33.shared;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class Data implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private String content;
    private String readState;
    private DataType dataType;
    private Date sentDate;
    private int toUserId;
    private int toGroupId;
    private int menuOptionSelected;

    private UserData senderData;

    public Data(String content, DataType dataType){
        this.content = content;
        this.dataType = dataType;
    }

    public Data(int menuOptionSelected){
        this.menuOptionSelected = menuOptionSelected;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReadState() {
        return readState;
    }

    public void setReadState(String readState) {
        this.readState = readState;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public UserData getSenderData() {
        return senderData;
    }

    public void setFromUserId(UserData senderData) {
        this.senderData = senderData;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public int getToGroupId() {
        return toGroupId;
    }

    public void setToGroupId(int toGroupId) {
        this.toGroupId = toGroupId;
    }
}
