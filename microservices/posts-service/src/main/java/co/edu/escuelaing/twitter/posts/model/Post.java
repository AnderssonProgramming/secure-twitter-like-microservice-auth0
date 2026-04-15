package co.edu.escuelaing.twitter.posts.model;

import java.time.Instant;

/**
 * DynamoDB item model for a post.
 */
public class Post {

    private String postId;
    private String content;
    private String userId;
    private String userEmail;
    private String userName;
    private String userPicture;
    private String createdAt;

    public Post() {}

    public Post(String postId, String content, String userId,
                String userEmail, String userName, String userPicture) {
        this.postId      = postId;
        this.content     = content;
        this.userId      = userId;
        this.userEmail   = userEmail;
        this.userName    = userName;
        this.userPicture = userPicture;
        this.createdAt   = Instant.now().toString();
    }

    public String getPostId()      { return postId; }
    public void setPostId(String v) { this.postId = v; }

    public String getContent()      { return content; }
    public void setContent(String v) { this.content = v; }

    public String getUserId()       { return userId; }
    public void setUserId(String v) { this.userId = v; }

    public String getUserEmail()        { return userEmail; }
    public void setUserEmail(String v)  { this.userEmail = v; }

    public String getUserName()         { return userName; }
    public void setUserName(String v)   { this.userName = v; }

    public String getUserPicture()      { return userPicture; }
    public void setUserPicture(String v){ this.userPicture = v; }

    public String getCreatedAt()        { return createdAt; }
    public void setCreatedAt(String v)  { this.createdAt = v; }
}
