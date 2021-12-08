package pt.isec.pd_g33.shared;

import java.awt.*;
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
    private String toUserUsername;
    private int toGroupId;
    private MenuOption menuOptionSelected;

    private UserData userData;

    // Opcao menu 2 -> Listar utilizadores
    public Data(MenuOption menuOptionSelected){
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
    public Data(MenuOption menuOptionSelected, UserData userData, int userID){
        this.menuOptionSelected = menuOptionSelected;
        this.userData = userData;
        this.toUserId = userID;
    }

    //Opcao menu 3 -> Pesquisar utilizador, 7 -> pendContact
    public Data(MenuOption menuOptionSelected,String username){
        this.menuOptionSelected = menuOptionSelected;
        this.content = username;
    }

    //Opcao menu 6 -> Delete Contact, 11 -> Create Group
    public Data(MenuOption menuOptionSelected, UserData userData, String name){
        this.menuOptionSelected = menuOptionSelected;
        this.userData = userData;
        this.content = name;
    }

    public Data(MenuOption menuOptionSelected,String mensagem, int toGroupID, UserData userData) {
        this.menuOptionSelected = menuOptionSelected;
        this.toGroupId = toGroupID;
        this.userData = userData;
        this.content = mensagem;
    }

    public Data(MenuOption menuOptionSelected,String mensagem, int toGroupID, UserData userData, DataType dataType) {
        this.menuOptionSelected = menuOptionSelected;
        this.toGroupId = toGroupID;
        this.userData = userData;
        this.content = mensagem;
        this.dataType = dataType;
        this.readState = "waiting";
    }

    public Data(MenuOption menuOptionSelected,String mensagem, String toUserUsername, UserData userData, DataType dataType) {
        this.menuOptionSelected = menuOptionSelected;
        this.toUserUsername = toUserUsername;
        this.userData = userData;
        this.content = mensagem;
        this.dataType = dataType;
        this.readState = "waiting";
    }

    public Data(MenuOption menuOptionSelected,String from_username, String to_username) {
        this.menuOptionSelected = menuOptionSelected;
        this.content = from_username;
        this.toUserUsername = to_username;
    }
    
    public Data(MenuOption menuOptionSelected, int toGroupId){
        this.menuOptionSelected = menuOptionSelected;
        this.toGroupId = toGroupId;
    }
    

    // listar historico de msg com grupo
    public Data(MenuOption menuOptionSelected, String from_username, int toGroupId) {
        this.menuOptionSelected = menuOptionSelected;
        this.content = from_username;
        this.toGroupId = toGroupId;
    }

    public MenuOption getMenuOptionSelected() {
        return menuOptionSelected;
    }
    
    public void setMenuOptionSelected(MenuOption menuOptionSelected) {
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

    public String getToUserUsername() {
        return toUserUsername;
    }

    public void setToGroupId(int toGroupId) {
        this.toGroupId = toGroupId;
    }
}
