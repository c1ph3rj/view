package tech.c1ph3rj.view.ChatScreen.model;

public class ChatMessages {

    public String message;

    public String attach;
    public  boolean isFromMyEnd;

    public String senderName;

    public String date;
    public boolean isMsgLoading;
    public boolean isMessageStopped;
    public boolean isMsgCompleted;

    public String getMessage() {
        return message;
    }

    public boolean isFromMyEnd() {
        return isFromMyEnd;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getDate() {
        return date;
    }

    public ChatMessages(String message, String attach, boolean isFromMyEnd, String senderName, String date)
    {
        this.message = message;
        this.attach = attach;
        this.isFromMyEnd = isFromMyEnd;
        this.senderName = senderName;
        this.date = date;
    }
}