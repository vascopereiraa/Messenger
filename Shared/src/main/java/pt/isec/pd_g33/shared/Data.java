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

    private UserData userData;

    // Opcao menu 2 -> Listar utilizadores e +
    public Data(int menuOptionSelected){
        this.menuOptionSelected = menuOptionSelected;
    }

    // Envio de mensagens informativas server -> cliente
    public Data(String content){
        this.content = content;
    }

    // Usado para login e registo
    public Data(String content,UserData userData){
        this.content = content;
        this.userData = userData;
    }
    // Opcao menu 1 -> Alterar dados
    public Data(int menuOptionSelected, UserData userData, int userID){
        this.menuOptionSelected = menuOptionSelected;
        this.userData = userData;
        this.toUserId = userID;
    }

    //Opcao menu 3 -> Pesquisar utilizador
    public Data(int menuOptionSelected,String username ){
        this.menuOptionSelected = menuOptionSelected;
        this.content = username;
    }

    public int getMenuOptionSelected() {
        return menuOptionSelected;
    }

    public void setMenuOptionSelected(int menuOptionSelected) {
        this.menuOptionSelected = menuOptionSelected;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
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

    public UserData getUserData() {
        return userData;
    }

    public void setFromUserId(UserData senderData) {
        this.userData = senderData;
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
