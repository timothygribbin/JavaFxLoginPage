import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class User implements Serializable{
	
	//All values that are associated with a user account
	private String username;
	private String password;
	private String fullName;
	private String address;
	private String phoneNumber;
	private String email;
	private String[] securityAnswers;
	private int failedLoginAttempts;
	private boolean accountLocked;
	
	//Constructors
	public User() {
		this("", "", "", "", "", "", new String[] {"", "", "", ""});
	}
	
	public User(String username, String password, String fullName, String address,
			String phoneNumber, String email, String[] securityAnswers) {
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.securityAnswers = securityAnswers;
		this.failedLoginAttempts = 0;
		this.accountLocked = false;
	}

	//getters and setters
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getFullName() {
		return this.fullName;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getPhoneNumber() {
		return this.phoneNumber;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public String[] getSecurityAnswers() {
		return this.securityAnswers;
	}
	
	public int getFailedLoginAttempts() {
		return this.failedLoginAttempts;
	}
	
	public boolean getAccountLocked() {
		return this.accountLocked;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setSecurityAnswers(String ans1, String ans2, String ans3, String ans4) {
		String[] securityAnswers = new String[4];
		
		securityAnswers[0] = ans1;
		securityAnswers[1] = ans2;
		securityAnswers[2] = ans3;
		securityAnswers[3] = ans4;
		
		this.securityAnswers = securityAnswers;
	}
	
	public void setFailedLoginAttempts(int failedLoginAttempts){
		this.failedLoginAttempts = failedLoginAttempts;
	}
	
	public void setAccountLocked(boolean b) {
		this.accountLocked = b;
	}
	//Check if the user has non empty strings in each value
	public boolean isFullyInitialized() {
		return (!this.username.equals("") && !this.password.equals("") && !this.fullName.equals("") 
			&& !this.address.equals("") && !this.phoneNumber.equals("") && !this.email.equals("") 
			&& !securityAnswers[0].equals("") && !securityAnswers[1].equals("") && !securityAnswers[2].equals("") && !securityAnswers[3].equals(""));
	}
}