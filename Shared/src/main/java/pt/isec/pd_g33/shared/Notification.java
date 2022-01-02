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
    private final DataType dataType;
    private String content;
    private String ip;
    private int porto;
    private int fileID;
    private boolean updateFiles = false;

    public Notification(String fromUsername, String toUsername, DataType dataType) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.dataType = dataType;
        this.content = "";
    }

    public Notification(String fromUsername, String toUsername,int toGroupId,String toGroupName, DataType dataType) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.dataType = dataType;
        this.toGroupId = toGroupId;
        this.toGroupName = toGroupName;
        this.content = "";
    }
    // Ficheiros para contactos
    public Notification(String fromUsername, String toUsername,int fileID, DataType dataType, String filename,
                        String ip,int porto) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.dataType = dataType;
        this.content = filename;
        this.ip = ip;
        this.porto = porto;
        this.fileID = fileID;
    }
    // Ficheiros para grupos
    public Notification(String fromUsername, int toGroupId, String toGroupName, String toUsername,
                        DataType dataType, String filename, String ip, int porto, int fileID) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.toGroupId = toGroupId;
        this.toGroupName = toGroupName;
        this.dataType = dataType;
        this.content = filename;
        this.ip = ip;
        this.porto = porto;
        this.fileID = fileID;
    }

    public Notification(String fromUsername, String toUsername, DataType dataType,int toGroupId , String toGroupName, String aceiteRejeitado) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.toGroupId = toGroupId;
        this.toGroupName = toGroupName;
        this.dataType = dataType;
        this.content = aceiteRejeitado;
    }

    public Notification(String content,int porto, DataType dataType) {
        this.content = content;
        this.dataType = dataType;
        this.porto = porto;
    }

    public Notification(String content, DataType dataType) {
        this.content = content;
        this.dataType = dataType;
    }

    public int getFileID() {
        return fileID;
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

    public boolean isUpdateFiles() {
        return updateFiles;
    }

    public void setUpdateFiles(boolean updateFiles) {
        this.updateFiles = updateFiles;
    }

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

