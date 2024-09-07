import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class DataCenter implements Serializable{
	//Instance variable
	private static DataCenter instance = null;
	
	//ArrayList needed to store and save user data
	private ArrayList<User> users;
	
	private DataCenter() {
		this.users = new ArrayList<>();
	}
	
	public static DataCenter getInstance(){
		if(instance == null) {
			instance = loadData();
			if(instance == null) {
				instance = new DataCenter();
			}
		}
		return instance;
	}
	
	public static DataCenter loadData() {
		DataCenter x = null;
		
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.dat"))){
			x = (DataCenter) ois.readObject();
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		} catch (ClassNotFoundException e) {
			
		}
		
		return x;
	}
	
	public void saveData() {
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.dat"))){
			oos.writeObject(instance);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<User> getUserList(){
		return this.users;
	}

	public boolean addUser(User user) {
		if(!instance.findUserByUser(user.getUsername()) && !instance.findUserByEmail(user.getEmail())) {
			this.users.add(user);
			return true;
		}
		return false;
	}
	
	public boolean validateLogin(String username, String password) {
		
		for(int i = 0; i < users.size(); i++) {
			if(users.get(i).getUsername().equals(username) && users.get(i).getPassword().equals(password)) {
				users.get(i).setFailedLoginAttempts(0);
				return true;
			}
		}
		return false;
	}
	
	public boolean findUserByUser(String username) {
		
		for(int i = 0; i < users.size(); i++) {
			if(users.get(i).getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean findUserByEmail(String email) {
		
		for(int i = 0; i < users.size(); i++) {
			if(users.get(i).getEmail().equals(email)) {
				return true;
			}
		}
		return false;
	}
	
	public User getUser(String username) {

		for(int i = 0; i < users.size(); i++) {
			if(users.get(i).getUsername().equals(username)) {
				return users.get(i);
			}
		}
		return null;
	}
}