package pt.isec.pd_g33.shared;

public enum MenuOption {
    EDIT_USER,
    LIST_USERS,
    SEARCH_USER,

    // Client
    LIST_CONTACTS,
    ADD_CONTACT,
    DELETE_CONTACT,
    PENDING_CONTACT,
    ACCEPT_CONTACT,
    REJECT_CONTACT,

    SEND_MSG_TO_GROUP,
    SEND_MSG_TO_CONTACT,
    DELETE_MESSAGE,

    LIST_MSG_FILES_CONTACT,
    LIST_MSG_FILES_GROUP,
    LIST_GROUPS,
    LIST_UNSEEN,

    SET_ONLINE,

    // Groups
    CREATE_GROUP,
    JOIN_GROUP,
    RENAME_GROUP,
    DELETE_GROUP,
    MEMBER_ACCEPT,
    MEMBER_REMOVE,
    LEAVE_GROUP,

    // Files
    SEND_FILE_TO_CONTACT,
    SEND_FILE_TO_GROUP,
    REQUEST_FILE_FROM_CONTACT,
    REQUEST_FILE_FROM_GROUP,
    DELETE_FILE,

    EXIT
}
