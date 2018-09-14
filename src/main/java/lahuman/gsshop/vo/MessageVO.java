package lahuman.gsshop.vo;

public class MessageVO {
    private MessageType type;
    private String message;
    private String userName;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        ADD_BLACK,
        REMOVE_BLACK
    }
}
