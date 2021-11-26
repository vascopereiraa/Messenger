package pt.isec.pd_g33.shared;

import java.io.Serial;
import java.io.Serializable;

public class Contact implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private String toUserId;
    private String fromUserId;
    private String requestState = "Pending";

    public Contact(String fromUserId, String toUserId){
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getRequestState() {
        return requestState;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public void setRequestState(String requestState) {
        this.requestState = requestState;
    }
}
