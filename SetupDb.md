# GlitchPoker Database Setup and Run Profile Setup

## Prerequisites

This guide will walk you through setting up PostgreSQL, configuring pgAdmin, creating a PostgreSQL user, adjusting the app settings, and running the GlitchPoker application with the corresponding profile.

---

### 1. Install PostgreSQL

Before running the application, you need to install and configure PostgreSQL.

#### **On macOS**:

1. **Install PostgreSQL**:
    - You can install PostgreSQL using Homebrew:
      ```bash
      brew install postgresql
      ```

2. **Start PostgreSQL**:
    - Once installed, start PostgreSQL using the following command:
      ```bash
      brew services start postgresql
      ```

#### **On Linux**:

1. **Update and Install PostgreSQL**:
    - Update your package manager:
      ```bash
      sudo apt-get update
      ```

    - Install PostgreSQL and the required extensions:
      ```bash
      sudo apt-get install postgresql postgresql-contrib
      ```

2. **Start PostgreSQL**:
    - To start PostgreSQL, use the following command:
      ```bash
      sudo service postgresql start
      ```

#### **On Windows**:

1. **Install PostgreSQL**:
    - Download the latest version of PostgreSQL from the [official PostgreSQL website](https://www.postgresql.org/download/windows/).
    - Run the installer and follow the on-screen instructions.

2. **Start PostgreSQL**:
    - After installation, PostgreSQL should start automatically. If not, you can manually start it via pgAdmin or by running PostgreSQL from the Start menu.

---

### 2. Setting Up pgAdmin and Creating a User

#### **Install and Configure pgAdmin**:

1. **Install pgAdmin**:
    - Download the latest version of pgAdmin from [pgAdmin's official website](https://www.pgadmin.org/download/).
    - Follow the installation instructions.

2. **Create a PostgreSQL User**:
    - Open pgAdmin and connect to your PostgreSQL server.
    - To create a new user:
        - Navigate to **Login/Group Roles** in the pgAdmin browser panel.
        - Right-click and select **Create > Login/Group Role**.
        - Fill in the details:
            - **Name**: Choose a name (e.g., `gianandreagerber`).
            - **Password**: Set a secure password.
            - **Privileges**: You can grant appropriate privileges (e.g., LOGIN).
        - Click **Save**.

3. **Create a Database**:
    - Right-click on **Databases** in the pgAdmin browser panel.
    - Select **Create > Database**.
    - Name the database (e.g., `glitchpoker`).
    - Click **Save**.

4. **Assign User to the Database**:
    - Navigate to your newly created database.
    - Go to **Properties > Security > Roles** and click **+** to add your user (`gianandreagerber`).
    - Save the changes.

---

### 3. Adjusting `appsettings-user.properties`

Now, you'll need to adjust the connection settings for PostgreSQL in your `appsettings-user.properties` file.
I added profiles for all of you.

1. **Locate `appsettings-user.properties`** in your project directory.

2. **Update the Connection Details**:
    - Modify the URL, username in `appsettings-user.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/glitchpoker
   spring.datasource.username=gianandreagerber
---

### 4. Run Application
Now all you need to do is run the application.
Since we dont want to compromise our passwords when we publish the settings, we have to enter it manually before the run. Or set it in your env :
"
export DB_PASSWORD=yourSecretPassword
"
Note that the profile is only the part after the '-', hence for Gian Andrea it is gga.
```bash
DB_PASSWORD=yourpassword ./gradlew bootRun -Pprofile=user
 ```
