package cl.acid.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class User {

	@Id
    @GeneratedValue
    private Integer id;
	
	@Column(unique = true)
	private String username;
	
	@Lob
	@Column(length = 10485760)
	private byte[] image;

	public Integer getId() {
		return id;
	}

	public String getUsername() {
		return username.trim();
	}
	
	public byte[] getImage() {
		return image;
	}
	
	public void setImage(byte[] image) {
		this.image = image;
	}

	public User(String username, byte[] image) {
		super();
		this.username = username;
		this.image = image;
	}
	
	User(){}
}
