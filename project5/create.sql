CREATE TABLE Login_Users (" +
					"userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
					"username VARCHAR(32) NOT NULL UNIQUE, " +
					"password CHAR(64) NOT NULL, " +
					"usersalt CHAR(32) NOT NULL);

CREATE TABLE Search_History (" +
							"entry INTEGER AUTO_INCREMENT PRIMARY KEY, " +
							"username VARCHAR(32) NOT NULL, " +
							"query VARCHAR(100) NOT NULL);