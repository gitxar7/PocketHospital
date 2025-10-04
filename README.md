<div align="center">
  <img src="https://github.com/gitxar7/PocketHospital/blob/main/logo.png" alt="PocketHospital Logo" width="100%">
  <h1>PocketHospital - Hospital Management System</h1>
</div>

## Project Overview

PocketHospital is a comprehensive hospital management system that digitizes healthcare services for Karapitiya Hospital, Galle. The system consists of two Android applications and a Java-based backend service that work together to provide seamless healthcare management solutions.

## System Architecture

### Frontend Applications

#### 1. **PocketHospitalApp** (Patient Mobile App)

- **Package**: `com.ar7enterprise.pockethospital`
- **Target Users**: Patients and general public
- **Primary Functions**:
  - Patient registration and authentication
  - Appointment scheduling and management
  - Medical reports and document management
  - E-Pharmacy services for medicine ordering
  - Step tracking and health monitoring
  - Department location services
  - Emergency contact features

#### 2. **AdminPanelApp** (Administrative Dashboard)

- **Package**: `com.ar7enterprise.adminpanel`
- **Target Users**: Hospital administrators and staff
- **Primary Functions**:
  - User management and monitoring
  - Admin authentication and password changes
  - Patient data oversight
  - System administration tools
  - Analytics and reporting dashboard

### Backend Service

#### **PocketHospitalBackEnd** (Java Web Service)

- **Technology**: Java EE 7, Hibernate ORM, MySQL Database
- **Architecture**: RESTful Web Services
- **Primary Functions**:
  - User authentication and authorization
  - Database management for users, appointments, and medical records
  - API endpoints for mobile applications
  - Admin panel data services
  - Secure data validation and processing

## Key Features

### Patient App Features

- **User Management**: Registration, sign-in, profile management
- **Appointment System**: Schedule, view, and manage medical appointments
- **Medical Reports**: Access and download medical test results
- **E-Pharmacy**: Order medicines using medical prescriptions
- **Health Tracking**: Step counter and fitness monitoring
- **Department Locator**: Interactive maps to find hospital departments
- **Emergency Services**: Quick access to emergency contacts
- **Push Notifications**: Real-time updates and reminders

### Admin Panel Features

- **User Administration**: Manage patient accounts and access levels
- **Analytics Dashboard**: View system usage and patient statistics
- **Security Management**: Admin authentication and password controls
- **Reporting Tools**: Generate system and patient reports
- **System Configuration**: Manage application settings and parameters

### Backend API Features

- **Authentication**: Secure login for patients and administrators
- **Data Management**: CRUD operations for all system entities
- **Real-time Sync**: Live data synchronization between apps
- **RESTful APIs**: Standardized endpoints for mobile communication
- **Data Validation**: Comprehensive input validation and sanitization

## Technology Stack

### Mobile Applications (Android)

- **Language**: Java
- **Framework**: Android SDK (API Level 24-35)
- **Libraries**:
  - Firebase Firestore (Real-time database)
  - Google Maps SDK (Location services)
  - MPAndroidChart (Data visualization)
  - OkHttp (Network communication)
  - Gson (JSON parsing)
  - PayHere SDK (Payment processing)
  - iText7 (PDF generation)

### Backend Service

- **Language**: Java 7
- **Framework**: Java EE 7 Web Profile
- **ORM**: Hibernate 4.3.1.Final
- **Database**: MySQL 8.0.24
- **Server**: GlassFish 4
- **Dependencies**:
  - Gson 2.11.0 (JSON processing)
  - MySQL Connector 8.0.24
  - Hibernate ecosystem libraries
  - Connection pooling (C3P0)

## Project Structure

```
ProjectPocketHospital/
├── AdminPanelApp/                 # Admin Android application
│   ├── app/
│   │   ├── src/main/java/         # Java source code
│   │   ├── src/main/res/          # Android resources
│   │   └── build.gradle.kts       # Build configuration
│   └── gradle/                    # Gradle wrapper files
├── PocketHospitalApp/             # Patient Android application
│   ├── app/
│   │   ├── src/main/java/         # Java source code
│   │   ├── src/main/res/          # Android resources
│   │   └── build.gradle.kts       # Build configuration
│   └── gradle/                    # Gradle wrapper files
├── PocketHospitalBackEnd/         # Java web service
│   ├── src/java/                  # Java source code
│   │   ├── controller/            # REST API controllers
│   │   ├── entity/                # JPA entities
│   │   └── model/                 # Business logic
│   ├── web/WEB-INF/              # Web configuration
│   ├── lib/                       # JAR dependencies
│   └── nbproject/                 # NetBeans project files
└── project_report.pdf             # Detailed project documentation
```

## Setup and Installation

### Prerequisites

- **Android Studio** Arctic Fox or newer
- **Java Development Kit (JDK)** 8 or newer
- **NetBeans IDE** 8.2 or newer (for backend)
- **GlassFish Server** 4.x
- **MySQL Server** 8.0 or newer
- **Android SDK** with API levels 24-35

### Backend Setup

1. **Database Configuration**:

   ```sql
   CREATE DATABASE pocket_hospital;
   ```

2. **Configure Hibernate**:

   - Update `hibernate.cfg.xml` with your database credentials
   - Ensure MySQL connector is in the classpath

3. **Deploy to GlassFish**:

   - Import project in NetBeans
   - Build the project
   - Deploy `PocketHospital.war` to GlassFish server

4. **Verify Installation**:
   - Access `http://localhost:8080/PocketHospital/Test`
   - Check database connectivity

### Mobile App Setup

1. **Clone and Import**:

   ```bash
   # Import both Android projects in Android Studio
   ```

2. **Configure Dependencies**:

   - Sync Gradle files
   - Ensure all dependencies are downloaded

3. **Firebase Setup**:

   - Add `google-services.json` to both app modules
   - Configure Firebase Firestore

4. **API Configuration**:

   - Update API endpoints in source code
   - Configure Google Maps API key

5. **Build and Run**:
   ```bash
   ./gradlew assembleDebug
   ```

## API Endpoints

### User Management

- `POST /Register` - User registration
- `POST /SignIn` - User authentication
- `POST /ChangePassword` - Update user password
- `GET /GetUsers` - Retrieve user list (Admin)
- `POST /DeactivateUser` - Deactivate user account

### Admin Functions

- `POST /AdminSignIn` - Admin authentication
- `POST /AdminChangePassword` - Admin password update
- `GET /GetData` - Retrieve system data

## Contributing

This project was developed as part of a hospital management system initiative. For contributions:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is developed for educational and healthcare improvement purposes. Please refer to the institution's guidelines for usage and distribution.

## Support

For technical support and inquiries:

- **Developer**: Abdur Rahman Hanas
- **Email**: nxt.genar7@gmail.com
- **GitHub**: [@gitxar7](https://github.com/gitxar7)
- **Organization**: AR7 Enterprise

## Security Notice

This system handles sensitive medical data. Ensure proper security measures are implemented:

- Use HTTPS in production
- Implement proper authentication
- Regular security audits
- Comply with healthcare data protection regulations

## Version History

- **v1.0**: Initial release with core functionality
- Backend API with user management
- Patient mobile application
- Admin panel for system management

---

**Note**: This README provides a comprehensive overview of the PocketHospital system. For detailed technical documentation, please refer to the `project_report.pdf` file included in the repository.
