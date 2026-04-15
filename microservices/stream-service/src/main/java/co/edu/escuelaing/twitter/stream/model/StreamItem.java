package co.edu.escuelaing.twitter.stream.model;

/**
 * Represents a single post in the public stream response.
 */
public class StreamItem {

    private String postId;
    private String content;
    private String userName;
    private String userEmail;
    private String userPicture;
    private String createdAt;

    public StreamItem() {}

    public String getPostId()           { return postId; }
    public void setPostId(String v)     { this.postId = v; }

    public String getContent()          { return content; }
    public void setContent(String v)    { this.content = v; }

    public String getUserName()         { return userName; }
    public void setUserName(String v)   { this.userName = v; }

    public String getUserEmail()        { return userEmail; }
    public void setUserEmail(String v)  { this.userEmail = v; }

    public String getUserPicture()      { return userPicture; }
    public void setUserPicture(String v){ this.userPicture = v; }

    public String getCreatedAt()        { return createdAt; }
    public void setCreatedAt(String v)  { this.createdAt = v; }
}
