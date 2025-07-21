# PETI - Smart Virtual Wardrobe & Outfit Recommendation System

A full-stack Spring Boot web application that helps users manage their wardrobe, get outfit recommendations, and track clothing usage.

## Features

- **User Authentication**: Secure JWT-based authentication system
- **Wardrobe Management**: Add, categorize, and manage clothing items
- **Usage Tracking**: Track how often you wear each item
- **Search & Filter**: Find items by category, color, season, or occasion
- **Responsive UI**: Modern, mobile-friendly interface with Bootstrap
- **MySQL Database**: Persistent storage with Spring Data JPA

## Tech Stack

- **Backend**: Spring Boot 3.5.3, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript
- **Database**: MySQL 8.0
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- MySQL 8.0
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Petitemsi/Peti.git
   cd Peti
   ```

2. **Configure Database**
   - Create a MySQL database named `peti`
   - Update `src/main/resources/application.properties` with your database credentials

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open your browser and go to `http://localhost:8080`
   - Register a new account and start managing your wardrobe!

## Project Structure

```
src/
├── main/
│   ├── java/com/peti/
│   │   ├── controller/     # REST controllers
│   │   ├── model/         # JPA entities
│   │   ├── repository/    # Data access layer
│   │   ├── service/       # Business logic
│   │   └── security/      # JWT authentication
│   └── resources/
│       ├── templates/     # Thymeleaf views
│       └── application.properties
```

## Contributing

This project is maintained by Petitemsi. Feel free to submit issues and enhancement requests!

## License

This project is licensed under the MIT License.

## 🎯 Features

### Core Features
- **User Authentication**: Secure registration and login with Spring Security
- **Wardrobe Management**: Add, edit, delete, and categorize clothing items
- **Item Tracking**: Track usage of clothing items and generate reports
- **Search & Filter**: Search through wardrobe by name, category, color, etc.
- **Image Upload**: Upload images for clothing items (local storage)
- **Usage Analytics**: Monitor how often items are used

### Planned Features
- **Outfit Recommendations**: AI-powered outfit suggestions based on occasion and weather
- **Weather Integration**: Filter recommendations based on current weather
- **Monthly Reports**: Automated reports highlighting unused items
- **Sustainability Suggestions**: Recommend combinations using underutilized items

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.5.3, Spring Security, Spring Data JPA
- **Database**: MySQL 8.0
- **Frontend**: Thymeleaf, Bootstrap 5, Font Awesome
- **Authentication**: JWT tokens
- **Build Tool**: Maven

## 📁 Project Structure

```
src/main/java/com/peti/
├── PetiApplication.java          # Main Spring Boot application
├── controller/                   # REST and web controllers
│   ├── AuthController.java      # Authentication endpoints
│   ├── HomeController.java      # Home page routing
│   └── WardrobeController.java  # Wardrobe management
├── model/                       # JPA entities
│   ├── User.java               # User entity with Spring Security
│   ├── ClothingItem.java       # Clothing item entity
│   ├── Outfit.java             # Outfit combinations
│   └── UsageLog.java           # Usage tracking
├── repository/                  # Data access layer
│   ├── UserRepository.java
│   ├── ClothingItemRepository.java
│   ├── OutfitRepository.java
│   └── UsageLogRepository.java
├── service/                     # Business logic
│   ├── UserService.java
│   └── WardrobeService.java
└── security/                    # Security configuration
    ├── WebSecurityConfig.java
    ├── CustomUserDetailsService.java
    ├── JwtAuthenticationFilter.java
    └── JwtService.java
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd peti
   ```

2. **Configure Database**
   - Create a MySQL database named `peti`
   - Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open your browser and go to `http://localhost:8080`
   - Register a new account or login

### Default Configuration
- **Database**: MySQL on localhost:3306
- **Username**: root
- **Password**: password
- **Database**: peti (auto-created if not exists)

## 📋 API Endpoints

### Authentication
- `POST /register` - User registration
- `POST /login` - User login
- `POST /api/auth/login` - JWT authentication

### Wardrobe Management
- `GET /wardrobe` - View wardrobe dashboard
- `GET /wardrobe/upload` - Upload form
- `POST /wardrobe/upload` - Add new item
- `GET /wardrobe/search?q={term}` - Search items
- `GET /wardrobe/category/{category}` - Filter by category
- `POST /wardrobe/{id}/use` - Mark item as used
- `DELETE /wardrobe/{id}` - Delete item

## 🎨 UI Features

### Modern Design
- Responsive Bootstrap 5 interface
- Clean, intuitive user experience
- Card-based item display
- Search and filtering capabilities
- Mobile-friendly design

### Pages
- **Login/Register**: Beautiful gradient design with form validation
- **Dashboard**: Grid view of wardrobe items with usage tracking
- **Upload Form**: Comprehensive form for adding new items
- **Navigation**: Easy access to all features

## 🔒 Security

- Spring Security with JWT authentication
- Password encryption with BCrypt
- Role-based access control (USER, ADMIN)
- CSRF protection
- Secure session management

## 📊 Data Models

### User
- Username, email, password
- Role-based permissions
- Account creation tracking

### ClothingItem
- Name, category, color, season, occasion
- Image URL, description
- Usage tracking (count, last used date)
- User association

### Outfit
- Name, occasion, season
- Multiple clothing items
- Usage tracking
- User association

### UsageLog
- Item usage tracking
- Date and time stamps
- Optional notes
- Outfit association

## 🚧 Next Steps

### Phase 2: Outfit Recommendation Engine
- Implement outfit combination logic
- Add weather API integration
- Create recommendation algorithms
- Build outfit suggestion interface

### Phase 3: Advanced Features
- Image recognition for automatic tagging
- Monthly usage reports
- Sustainability suggestions
- Social features (sharing outfits)

### Phase 4: Deployment
- Cloud deployment (Heroku, Railway, or Render)
- Production database setup
- Environment configuration
- Performance optimization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions, please open an issue in the repository or contact the development team.

---

**PETI** - Making wardrobe management smart and sustainable! 🧥👗👠 