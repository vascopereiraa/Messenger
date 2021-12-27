package pt.isec.pd_g33.shared;

import java.io.Serial;
import java.io.Serializable;

public class Notification implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private String toUsername;
    private String fromUsername;
    private int toGroupId;
    private String toGroupName;
    private DataType dataType;
    private String content;
    private String ip;
    private int porto;

    public Notification(String fromUsername, String toUsername, DataType dataType) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.dataType = dataType;
    }

    public Notification(String fromUsername, String toUsername, DataType dataType, String filename,String ip,int porto) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.dataType = dataType;
        this.content = filename;
        this.ip = ip;
        this.porto = porto;
    }

    public Notification(String fromUsername, int toGroupId,String toGroupName , String toUsername, DataType dataType, String filename, String ip, int porto) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.toGroupId = toGroupId;
        this.toGroupName = toGroupName;
        this.dataType = dataType;
        this.content = filename;
        this.ip = ip;
        this.porto = porto;
    }

    public Notification(String fromUsername, String toUsername, DataType dataType,int toGroupId , String toGroupName, String aceiteRejeitado) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.toGroupId = toGroupId;
        this.toGroupName = toGroupName;
        this.dataType = dataType;
        this.content = aceiteRejeitado;
    }


    public String getToUsername() {
        return toUsername;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getIp() {
        return ip;
    }

    public int getPorto() {
        return porto;
    }

    public String getContent() {
        return content;
    }

    public String getToGroupName() {
        return toGroupName;
    }

    public int getToGroupId() { return toGroupId; }

    @Override
    public String toString() {
        return "Notification{" +
                "toUsername='" + toUsername + '\'' +
                ", fromUsername='" + fromUsername + '\'' +
                ", toGroupId=" + toGroupId +
                ", toGroupName='" + toGroupName + '\'' +
                ", dataType=" + dataType +
                ", content='" + content + '\'' +
                ", ip='" + ip + '\'' +
                ", porto=" + porto +
                '}';
    }
}

