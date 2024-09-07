import java.util.ArrayList;

import javafx.beans.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Display extends Application {
	
	//Made these class data members to give access to the methods that need to modify them
	private BorderPane root;
	private Pane currentPage;
	private static DataCenter dc = DataCenter.getInstance();
	private transient User tempUser = new User();
	
	public void start(Stage stage) throws Exception {
		
		root = new BorderPane();
		
		currentPage = basePage();
		
		root.setCenter(currentPage);
		
		Scene scene = new Scene(root, 800, 600 );
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		for(User u : dc.getUserList()) {
			System.out.println(u.getUsername());
		}
		Application.launch(args);
	}
	//Base page, enables you to login, or navigate to register for an account, change your password or recover (if allowed due to the account being lock)
	public Pane basePage() {
		
		BorderPane bp = new BorderPane();
		
		Label titleLabel = new Label("Login page");
		bp.setTop(titleLabel);
		bp.setAlignment(titleLabel, Pos.CENTER);
		bp.setPadding(new Insets(20));
		
		Label userLabel = new Label("Username: ");
		TextField userText = new TextField();
		
		Label passLabel = new Label("Password: ");
		TextField passText = new TextField();
		
		Button loginBtn = new Button("Login");
		loginBtn.setDisable(true);
		
		Button changePassBtn = new Button("Change password");
		
		HBox hb = new HBox(loginBtn, changePassBtn);
		hb.setSpacing(10.0);
		hb.setAlignment(Pos.CENTER);
		hb.setPadding(new Insets(20));
		VBox vb = new VBox(userLabel, userText, passLabel, passText, hb);
		vb.setAlignment(Pos.CENTER);
		vb.setSpacing(5.0);
		bp.setCenter(vb);
		
		Button regBtn = new Button("Register for account");
		regBtn.setPrefWidth(150);
		Button recoverBtn = new Button("Recover account");
		recoverBtn.setDisable(true);
		recoverBtn.setPrefWidth(150);
		
		HBox hb2 = new HBox(recoverBtn, regBtn);
		hb2.setSpacing(475);
		bp.setBottom(hb2);
		//listens for an update
		InvalidationListener updateListener = (observable) -> {
			loginBtn.setDisable(userText.getText().length() == 0 || passText.getText().length() == 0);
			User u = dc.getUser(formatString(userText.getText()));
			recoverBtn.setDisable(u == null || u.getAccountLocked() == false);
		};
		
		//Apply the uodateListener to the fields that will be filled out
		userText.textProperty().addListener(updateListener);
		passText.textProperty().addListener(updateListener);
		
		//Changes to the scene depending on which button you press
		regBtn.setOnAction(e ->{
			setCurrentScene(registerTabs());
		});
		
		changePassBtn.setOnAction(e ->{
			setCurrentScene(changePassword());
		});
		
		recoverBtn.setOnAction(e ->{
			setCurrentScene(recoveryPage());
		});
		//Validates login by checking database for user existing, and then checking if the password matches. Notifies user if their login was successful or not
		loginBtn.setOnAction(e ->{
			String user = formatString(userText.getText());
			String pass = passText.getText();
			User u = dc.getUser(user);
			
			if(dc.validateLogin(user, passText.getText()) && u.getAccountLocked() == false) {
				showAlert(Alert.AlertType.CONFIRMATION, "Successful login", "User has successfully logged in");
				u.setFailedLoginAttempts(0);
			}
			else if(!dc.validateLogin(user, pass)){
				showAlert(Alert.AlertType.ERROR, "Unsuccessful login attempt" ,"Please verify username and password are correct");
				if(u != null) {
					u.setFailedLoginAttempts(u.getFailedLoginAttempts() + 1);
					if(u.getFailedLoginAttempts() > 3) {
						loginBtn.setDisable(true);
						recoverBtn.setDisable(false);
						u.setAccountLocked(true);
					}
				}
			}
			
			if(u != null && u.getAccountLocked() == true) {
				showAlert(Alert.AlertType.ERROR, "Unsuccessful login attempt", "Account for user " + user + " is locked");
			}
			dc.saveData();
		});
		
		return bp;
	}
	
	public Pane registerTabs() {
		//Tab pane to allow user to move back and forth between the different registration pages
		BorderPane bp = new BorderPane();
		
		Label titleLabel = new Label("Registration Pages");
		
		TabPane tp = new TabPane();
		
		Tab t1 = new Tab("Register Page");
		t1.setClosable(false);
		Tab t2 = new Tab("Provide user info");
		t2.setClosable(false);
		Tab t3 = new Tab("Security Questions");
		t3.setClosable(false);
		
		Button returnToHome = new Button("Exit");
		HBox hb = new HBox(returnToHome);
		hb.setPadding(new Insets(20));
		hb.setPrefHeight(25);
		hb.setAlignment(Pos.BOTTOM_RIGHT);
		
		t1.setContent(registerPage());
		t2.setContent(userInfo());
		t3.setContent(securityQuestionsAnswer());
		
		tp.getTabs().addAll(t1, t2, t3);
	
		bp.setCenter(tp);
		
		bp.setBottom(hb);
		
		bp.setTop(titleLabel);
		
		bp.setAlignment(titleLabel, Pos.CENTER);
		
		returnToHome.setOnAction(e ->{
			setCurrentScene(basePage());
		});
		
		return bp;
	}
	
	
	public Pane registerPage() {
		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(15));
		
		Label userLabel = new Label("Username: ");
		TextField userText = new TextField();
		Label pass1Label = new Label("Password: ");
		TextField pass1Text = new TextField();
		Label pass2Label = new Label("Confirm password: ");
		TextField pass2Text = new TextField();
		//Labels to tell user what's wrong with their input
		Label passwordsDontMatch = new Label("Password's must match");
		passwordsDontMatch.setVisible(false);
		labelStyle(passwordsDontMatch);
		
		Label passwordLengthError = new Label("Passwords must be > 4 charachters");
		passwordLengthError.setVisible(false);
		labelStyle(passwordLengthError);
		
		VBox vb1 = new VBox(passwordsDontMatch, passwordLengthError);
		
		VBox vb = new VBox(userLabel, userText, pass1Label, pass1Text, pass2Label, pass2Text);
		vb.setSpacing(5.0);
		
		bp.setCenter(vb);
		
		bp.setBottom(vb1);
		//When an update occurs to an observable field, preform below actions
		InvalidationListener updateListener = observable ->{
			updateUserLogin(userText, pass1Text, pass2Text);
			String pass1 = pass1Text.getText();
			String pass2 = pass2Text.getText();
			boolean passwordsMatching = pass1.equals(pass2);
			passwordsDontMatch.setVisible(!(passwordsMatching));
			passwordLengthError.setVisible((pass1.length() <= 4 && pass1.length() > 0) || (pass2.length() <= 4 && pass2.length() > 0));
		};
		//Set listener for these text properties
		userText.textProperty().addListener(updateListener);
		pass1Text.textProperty().addListener(updateListener);
		pass2Text.textProperty().addListener(updateListener);
		
		return bp;
	}
	
	public Pane userInfo() {
		//Page to collect user ingo (name ,address, phone number, etc) 
		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(20));
		
		Label fullNameLabel = new Label("Enter full name: ");
		TextField fullNameText = new TextField();
		Label userNumberLabel = new Label("Enter phone number: ");
		TextField userNumberText = new TextField();
		Label userStreetAddressLabel = new Label("Enter street address");
		TextField userStreetAddressText = new TextField();
		Label userTownLabel = new Label("Enter town: ");
		TextField userTownText = new TextField();
		Label userStateLabel = new Label("Enter state: ");
		TextField userStateText = new TextField();
		Label userZipCodeLabel = new Label("Enter zip code");
		TextField userZipCodeText = new TextField();
		Label userEmailLabel = new Label("Enter email: ");
		TextField userEmailText  = new TextField();
		
		//Label's to inform of issues with input
		Label invalidNumber = new Label("Phone number must be exactly 10 digits and can only be comprised of numbers");
		labelStyle(invalidNumber);
		invalidNumber.setVisible(false);
		Label invalidAddress = new Label("Address is not in the correct format");
		labelStyle(invalidAddress);
		invalidAddress.setVisible(false);
		Label invalidEmail = new Label("Email address must contain a domain name");
		labelStyle(invalidEmail);
		invalidEmail.setVisible(false);
		
		VBox errorLabelVb = new VBox(invalidNumber, invalidAddress, invalidEmail);
		
		
		VBox vb = new VBox(fullNameLabel, fullNameText, userNumberLabel, userNumberText, userStreetAddressLabel, 
						   userStreetAddressText, userTownLabel, userTownText, userStateLabel, userStateText, userZipCodeLabel,
						   userZipCodeText, userEmailLabel, userEmailText);
		vb.setSpacing(5.0);
		
		bp.setCenter(vb);
		bp.setBottom(errorLabelVb);
		
		//Listener for changes in properties
		InvalidationListener updateListener  = (observable)-> {
			updateUserInfo(fullNameText, userNumberText, userStreetAddressText, userTownText, userStateText, userZipCodeText, userEmailText);
			String userNumber = formatString(userNumberText.getText());
			String userStreetAddress = formatString(userStreetAddressText.getText()); 
			//For the purpose of validating the address format (should also be (house number street name street/avenue/way/place) which will mean the length is 3
			String userTown = formatString(userTownText.getText());
			String userState = formatString(userStateText.getText());
			String userZipCode = formatString(userZipCodeText.getText());
			String userEmail = formatString(userEmailText.getText());
			String userFullAddress = userStreetAddress + ", " + userTown + ", " + userState + ", " + userZipCode;
			
			invalidAddress.setVisible(userFullAddress.length() > 9 && !isValidAddress(userStreetAddress, userTown, userState, userZipCode));
			invalidEmail.setVisible(userEmail.length() > 4 &&!isValidEmail(userEmail));
			invalidNumber.setVisible(userNumber.length() > 1 && !isValidPhoneNumber(userNumber));
			
		};
		//Set listener for these properties (when test changes)
		fullNameText.textProperty().addListener(updateListener);
		userNumberText.textProperty().addListener(updateListener);
		userStreetAddressText.textProperty().addListener(updateListener);
		userStateText.textProperty().addListener(updateListener);
		userZipCodeText.textProperty().addListener(updateListener);
		userEmailText.textProperty().addListener(updateListener);

		return bp;
	}
	
	public Pane securityQuestionsAnswer() {
		//Page to get security questions
		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(20));
		
		Label sq1Label = new Label("What elementary school did you attend?");
		TextField sq1Tf = new TextField();
		Label sq2Label = new Label("What's the name of your first pet?");
		TextField sq2Tf  = new TextField();
		Label sq3Label = new Label("What's your mother's maiden name?");
		TextField sq3Tf = new TextField();
		Label sq4Label = new Label("What street was your first home on?");
		TextField sq4Tf = new TextField();
		
		Button submitBtn = new Button("Register");
		submitBtn.setDisable(true);
		
		HBox hb1 = new HBox(submitBtn);
		hb1.setAlignment(Pos.CENTER);
		hb1.setPadding(new Insets(10));
		
		VBox vb = new VBox(sq1Label, sq1Tf, sq2Label, sq2Tf, sq3Label, sq3Tf, sq4Label, sq4Tf, hb1);
		vb.setSpacing(10);
		
		bp.setCenter(vb);
		//Listens for updates
		InvalidationListener updateListener = (observable) ->{
			updateSecurityQuestions(sq1Tf, sq2Tf, sq3Tf, sq4Tf);
		};
		
		//Add listener for these properties
		sq1Tf.textProperty().addListener(updateListener);
		sq2Tf.textProperty().addListener(updateListener);
		sq3Tf.textProperty().addListener(updateListener);
		sq4Tf.textProperty().addListener(updateListener);
		
		//Switched to set on mouse moved because if I tried to register and then filled out all the necessary fields,
		//and then backspaced an element not on the the seurityQuestions page and then came back to the securityPage, I could register the account with empty fields 
		bp.setOnMouseMoved(e ->{
			submitBtn.setDisable(!(tempUser.isFullyInitialized()));
		});
		
		//When submitted add's user, (add user checks if user is registered, both by email and username before adding to them)
		submitBtn.setOnAction(e ->{
			boolean b = dc.addUser(tempUser);
			//If successfully added
			if(b == true) {
				showAlert(Alert.AlertType.CONFIRMATION, "New account registered", "New account has been successfully been created");
				dc.saveData();
				//Reset temp user for next registration
				tempUser = new User();
				setCurrentScene(basePage());
			}
			else {
				showAlert(Alert.AlertType.ERROR, "New account failed to register", "User already existed");
			}
		});
		
		return bp;
	}
	
	public Pane changePassword() {
		//Page to change password
		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(20));
		
		Label titleLabel = new Label("Change Password Page");
		
		Label userLabel = new Label("Enter Username: ");
		TextField userText = new TextField();
		Label pass1Label = new Label("Old password: ");
		TextField pass1Text = new TextField();
		Label pass2Label = new Label("New password: ");
		TextField pass2Text = new TextField();
		Label pass3Label = new Label("Confirm new password: ");
		TextField pass3Text = new TextField();
		
		//Labels for feedback
		Label passwordsDontMatch = new Label("New password's must match");
		passwordsDontMatch.setVisible(false);
		labelStyle(passwordsDontMatch);
		
		Label isNotValidPassword = new Label("Old password does not match password associated with entered username");
		isNotValidPassword.setVisible(false);
		labelStyle(isNotValidPassword);
		
		Button changePassBtn = new Button("Change password");
		changePassBtn.setDisable(true);
		
		VBox errorVb = new VBox(passwordsDontMatch, isNotValidPassword);
		
		HBox hb = new HBox(changePassBtn);
		hb.setAlignment(Pos.CENTER);
		hb.setPadding(new Insets(20));
		
		Button returnToHome = new Button("Exit");
		HBox hb1 = new HBox(errorVb,returnToHome);
		hb1.setSpacing(275);
		hb1.setPadding(new Insets(20));
		hb1.setPrefHeight(25);
		hb1.setAlignment(Pos.BOTTOM_RIGHT);
		
		VBox vb = new VBox(userLabel, userText, pass1Label, pass1Text, pass2Label, pass2Text, pass3Label, pass3Text, hb);
		vb.setSpacing(5.0);
		vb.setAlignment(Pos.CENTER);
		
		bp.setCenter(vb);
		
		bp.setBottom(hb1);
		
		bp.setTop(titleLabel);
		
		bp.setAlignment(titleLabel, Pos.CENTER);
		//Listener for updates
		InvalidationListener updateListener = (observable) ->{
			String pass1 = pass1Text.getText();
			String pass2 = pass2Text.getText();
			String pass3 = pass3Text.getText();
			String user = formatString(userText.getText());
			User u = dc.getUser(user);
			passwordsDontMatch.setVisible(!pass2.equals(pass3));
			if(u != null) {
				changePassBtn.setDisable(!(pass2.equals(pass3)) || (pass2.length() <= 4 && pass3.length() <= 4 )|| !(u.getPassword().equals(pass1)) || pass1.length() < 4);
				isNotValidPassword.setVisible(!(u.getPassword().equals(pass1)) && pass1.length() > 4);
			}
		};
		
		//Listens for text changes
		pass1Text.textProperty().addListener(updateListener);
		pass2Text.textProperty().addListener(updateListener);
		pass3Text.textProperty().addListener(updateListener);
		userText.textProperty().addListener(updateListener);
		
		//When change pass button is pressed
		changePassBtn.setOnAction(e ->{
			//Checks if user is null above before enabling button so does not have to be checked here
			User u = dc.getUser(formatString(userText.getText()));
			String oldPass = pass1Text.getText();
			//If old password matches
			if(oldPass.equals(u.getPassword())) {
				//Set password to new password and save to data center
				u.setPassword(pass2Text.getText());
				dc.saveData();
				showAlert(Alert.AlertType.CONFIRMATION, "Successful password change", "Password changed successfully");
				setCurrentScene(basePage());
			}
			else {
				showAlert(Alert.AlertType.ERROR, "Unsuccessful password change", "Password not changed, please try again");
			}
		});
		
		//Exit button
		returnToHome.setOnAction(e ->{
			setCurrentScene(basePage());
		});
		
		bp.setBottom(hb1);
		
		return bp;
	}
	
	public Pane recoveryPage() {
		//Page to recover account
		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(20));
		
		Label titleLabel = new Label("Recovery Page");
		
		//Array for the questions to choose from
		String[] questions = {"What elementary school did you attend?", 
								"What's the name of your first pet?",
								"What's your mother's maiden name?",	
								"What street was your first home on?"};
		
		//Generate random indexes (that are unique)
		//These indexes will also line up with the indexes in the User class in which the answers are stored 
		//so we can directly compare the answers with the same index generated here.
		int r1 = -1;
		int r2 = -1;
		while(r1 == r2) {
			r1 = (int) (Math.random() * 4);
			r2 = (int) (Math.random() * 4);
		}
		
		Label l1 = new Label("Enter username");
		TextField userTf = new TextField();
		Label sq1Label = new Label(questions[r1]);
		TextField sq1Tf = new TextField();
		Label sq2Label = new Label(questions[r2]);
		TextField sq2Tf  = new TextField();

		Button submitBtn = new Button("Submit");
		submitBtn.setDisable(true);
		submitBtn.setPrefWidth(150);
		
		Button exitBtn = new Button("Exit");
		
		//Labels for error feedback
		Label isNotValidPassword = new Label("Old password does not match password associated with entered username");
		isNotValidPassword.setVisible(false);
		
		VBox errorVb = new VBox(isNotValidPassword, exitBtn);
		
		VBox vb = new VBox(l1, userTf, sq1Label, sq1Tf, sq2Label, sq2Tf, submitBtn);
		vb.setAlignment(Pos.CENTER);
		vb.setSpacing(10);
		
		bp.setCenter(vb);
		
		bp.setBottom(errorVb);
		
		bp.setTop(titleLabel);
		
		bp.setAlignment(titleLabel, Pos.CENTER);
		
		//Listener for updates in properties
		InvalidationListener updateListener = (observable) ->{
			String user = formatString(userTf.getText());
			User u = dc.getUser(user);
			//Enables submit button if account of user entered is locked (to avoid a user trying to recover a non locked account)
			if(user.length() > 3 && u != null && u.getAccountLocked() == true && sq1Tf.getText().length() > 2 && sq2Tf.getText().length() > 2) {
				submitBtn.setDisable(false);
			}
			else {
				submitBtn.setDisable(true);
			}
		};
		
		//Set listener for these properties
		userTf.textProperty().addListener(updateListener);
		sq1Tf.textProperty().addListener(updateListener);
		sq2Tf.textProperty().addListener(updateListener);
		
		final int finR1 = r1;
		final int finR2 = r2;
		
		submitBtn.setOnAction(e ->{
			boolean b = false;
			String answer1 = formatString(sq1Tf.getText());
			String answer2 = formatString(sq2Tf.getText());
			User user = dc.getUser(formatString(userTf.getText()));
			//If user exists, and the answers match b = true
			if(user != null && answer1.equals(user.getSecurityAnswers()[finR1]) && answer2.equals(user.getSecurityAnswers()[finR2])) {
				b = true;
			}
			//if true
			if(b == true) {
				//Account recovered succesffuly, user given temp password and account is unlocked as well as failed login attempts is set to 0, and it's all saved
				showAlert(Alert.AlertType.CONFIRMATION, "Account recovered", "Temporary password: 'password'. Please change at your earliest convenience");
				user.setPassword("password");
				user.setAccountLocked(false);
				user.setFailedLoginAttempts(0);
				dc.saveData();
			}
			else {
				showAlert(Alert.AlertType.ERROR, "Account not recovered", "Incorrect answer to security questions");
			}
			setCurrentScene(basePage());
		});
		
		exitBtn.setOnAction(e ->{
			setCurrentScene(basePage());
		});

		return bp;
	}
	
	//Shows alert
	public void showAlert(Alert.AlertType alertType, String title, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setContentText(content);
		alert.show();
	}
	//Styles error labels red
	public void labelStyle(Label l) {
		l.setStyle("-fx-text-fill: red;");
	}
	//Extra methods that don't directly make the graphics just to avoid some repeated logic
	//Formats string for uniform storage in data center
	public String formatString(String string) {
		return string.strip().toLowerCase();
	}
	
	//switches current page and view
	public void setCurrentScene(Pane p) {
		currentPage = p;
		root.setCenter(currentPage);
	}
	
	//Validates input for state field
	public boolean isValidState(String stateInput) {
		String[] states = {
				"alabama", "alaska", "arizona", "arkansas", "california",
			    "colorado", "connecticut", "delaware", "florida", "georgia",
			    "hawaii", "idaho", "illinois", "indiana", "iowa",
			    "kansas", "kentucky", "louisiana", "maine", "maryland",
			    "massachusetts", "michigan", "minnesota", "mississippi", "missouri",
			    "montana", "nebraska", "nevada", "new hampshire", "new jersey",
			    "new mexico", "new york", "north carolina", "north dakota", "ohio",
			    "oklahoma", "oregon", "pennsylvania", "rhode island", "south carolina",
			    "south dakota", "tennessee", "texas", "utah", "vermont",
			    "virginia", "washington", "west virginia", "wisconsin", "wyoming"
			};
		
		for(String state : states) {
			if(state.equals(stateInput)) {
				return true;
			}
		}
		return false;
	}
	
	//Validates email in a basic fashion
	public boolean isValidEmail(String emailInput) {
		return emailInput.length() > 2 && emailInput.contains("@") && (emailInput.contains(".com") || emailInput.contains(".net") || emailInput.contains(".org") || emailInput.contains(".edu") && !emailInput.contains(" "));
	}
	
	//Validates address
	public boolean isValidAddress(String userStreetAddress, String userTown, String userState, String userZipCode) {
		return (userStreetAddress.split(" ").length == 3 && userTown.length() > 2 
				&& isValidState(userState) && userZipCode.matches("\\d+") && userZipCode.length() == 5);
	}
	
	//validates phone number
	public boolean isValidPhoneNumber(String userNumber) {
		return (userNumber.length() == 10 && userNumber.length() != 0 && userNumber.matches("\\d{10}"));
	}
	
	public void updateUserLogin(TextField usernameTf, TextField password1Tf, TextField password2Tf) {
		
		String username = formatString(usernameTf.getText());
		String password1 = password1Tf.getText();
		String password2 = password2Tf.getText();
		if(password1.equals(password2) && username.length() > 3 && password2.length() > 4) {
			tempUser.setUsername(username);
			tempUser.setPassword(password2);
		}
		
		//Must be added to make the isFullyInitialized of the user to fail if text fields start being backspaced
		else {
			tempUser.setUsername("");
			tempUser.setPassword("");
		}
	}
	
	public void updateUserInfo(TextField fullNameTf, TextField numberTf, TextField streetAddressTf, TextField townTf, TextField stateTf, TextField zipTf, TextField emailTf){
		
		String fullName = formatString(fullNameTf.getText());
		String number = formatString(numberTf.getText());
		String streetAddress = formatString(streetAddressTf.getText());
		String town = formatString(townTf.getText());
		String state = formatString(stateTf.getText());
		String zip = formatString(zipTf.getText());
		String fullAddress = streetAddressTf.getText() + ", " + townTf.getText() + ", " + stateTf.getText() + ", " + zipTf.getText();
		String email = formatString(emailTf.getText());
		
		if(fullName.length() > 3 && isValidPhoneNumber(number) && isValidAddress(streetAddress, town, state, zip) && isValidEmail(email)) {
			tempUser.setFullName(fullName);
			tempUser.setPhoneNumber(number);
			tempUser.setAddress(fullAddress);
			tempUser.setEmail(email);
		}
		else {
			tempUser.setFullName("");
			tempUser.setPhoneNumber("");
			tempUser.setAddress("");
			tempUser.setEmail("");
		}
	}
	
	public void updateSecurityQuestions(TextField sq1Tf, TextField sq2Tf, TextField sq3Tf, TextField sq4Tf) {
		
		String sq1 = formatString(sq1Tf.getText());
		String sq2 = formatString(sq2Tf.getText());
		String sq3 = formatString(sq3Tf.getText());
		String sq4 = formatString(sq4Tf.getText());
		
		if(sq1.length() > 2 && sq2.length() > 2 && sq3.length() > 2 && sq4.length() > 2) {
			tempUser.setSecurityAnswers(sq1, sq2, sq3, sq4);
		}
		else {
			tempUser.setSecurityAnswers("", "", "", "");
		}
	}
}