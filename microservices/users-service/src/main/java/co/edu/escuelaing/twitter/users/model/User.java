package co.edu.escuelaing.twitter.users.model;

/**
 * DynamoDB item model for a user profile.
 */
public class User {

    private String auth0Sub;
    private String email;
    private String name;
    private String picture;
    private String createdAt;

    public User() {}

    public String getAuth0Sub()         { return auth0Sub; }
    public void setAuth0Sub(String v)   { this.auth0Sub = v; }

    public String getEmail()            { return email; }
    public void setEmail(String v)      { this.email = v; }

    public String getName()             { return name; }
    public void setName(String v)       { this.name = v; }

    public String getPicture()          { return picture; }
    public void setPicture(String v)    { this.picture = v; }

    public String getCreatedAt()        { return createdAt; }
    public void setCreatedAt(String v)  { this.createdAt = v; }
}
