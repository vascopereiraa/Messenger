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

    public Data(MenuOption menuOptionSelected, String mensagem, int toGroupID, UserData userData) {
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

    public Data(MenuOption menuOptionSelected, String from_username, String to_username) {
        this.menuOptionSelected = menuOptionSelected;
        this.content = from_username;
        this.toUserUsername = to_username;
    }

    // Ficheiros contactos
    public Data(MenuOption menuOptionSelected, String content, String toUserUsername, String IP, int toUserId, UserData userData){
        this.menuOptionSelected = menuOptionSelected;
        this.content = content; // filename
        this.toUserUsername = toUserUsername; // Utilizador Destino
        this.toUserId = toUserId; // Porto
        this.readState = IP; // IP
        this.userData = userData; // Dados do user de origem
        this.dataType = DataType.File;
    }

    // Ficheiros grupos
    public Data(MenuOption menuOptionSelected, String content, int toGroupId, String IP, int toUserId, UserData userData){
        this.menuOptionSelected = menuOptionSelected;
        this.content = content; // filename
        this.toGroupId = toGroupId; // Grupo Destino
        this.toUserId = toUserId; // Porto
        this.readState = IP; // IP
        this.userData = userData; // Dados do user de origem
        this.dataType = DataType.File;
    }

    // listar historico de msg com grupo
    public Data(MenuOption menuOptionSelected, String from_username, int toGroupId) {
        this.menuOptionSelected = menuOptionSelected;
        this.content = from_username;
        this.toGroupId = toGroupId;
    }

    public Data(MenuOption menuOption, UserData userData, DataType dataType) {
        this.menuOptionSelected = menuOption;
        this.userData = userData;
        this.dataType = dataType;
    }


    public MenuOption getMenuOptionSelected() {
        return menuOptionSelected;
    }
    
    public String getContent() {
        return content;
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

    public UserData getUserData() {
        return userData;
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

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Data{" +
                "content='" + content + '\'' +
                ", readState='" + readState + '\'' +
                ", dataType=" + dataType +
                ", sentDate=" + sentDate +
                ", toUserId=" + toUserId +
                ", toUserUsername='" + toUserUsername + '\'' +
                ", toGroupId=" + toGroupId +
                ", menuOptionSelected=" + menuOptionSelected +
                ", userData=" + userData +
                '}';
    }
}
