
# PETI – Smart Virtual Wardrobe with AI-Powered Outfit Suggestions

**PETI** is a full-stack, AI-enhanced virtual wardrobe system built with Spring Boot, Docker, MySQL, and Thymeleaf. It supports secure JWT authentication, intelligent outfit recommendations powered by OpenAI GPT-4o, real-time weather integration, image handling with Cloudinary, and usage analytics.

---

## 🚀 Features

- 🔐 JWT-based authentication & secure user sessions
- 👗 Wardrobe management with image uploads and categories
- 📊 Usage tracking & monthly reports
- 🤖 AI-powered outfit suggestions (OpenAI GPT-4o)
- 🌤️ Weather integration with [Open-Meteo](https://open-meteo.com)
- 🧺 Admin dashboard: user controls & analytics
- 📱 Responsive UI with Bootstrap
- 🐳 Docker-ready and unit-tested

---

## 🛠️ Tech Stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Java 17, Spring Boot 3.5.3, Spring Security, Spring Data JPA |
| Frontend  | Thymeleaf, Bootstrap 5, Font Awesome |
| Database  | MySQL 8.0 |
| AI        | OpenAI GPT-4o |
| Weather   | Open-Meteo API |
| Image API | Cloudinary |
| Auth      | JWT Tokens |
| Build     | Maven |
| Testing   | JUnit |
| DevOps    | Docker, Docker Compose |

---

## 🛠️ Local Setup & Installation

### 🔧 Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.6+
- (Optional) Docker & Docker Compose
- OpenAI API key
- Cloudinary account

### 📥 Clone the Repository

git clone https://github.com/Petitemsi/Peti-Project
cd peti


### ⚙️ Configuration

Edit `src/main/resources/application.properties`:

spring.datasource.url=jdbc:mysql://localhost:3306/peti
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

jwt.secret=your_jwt_secret
openai.api.key=your_openai_api_key

cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_cloudinary_api_key
cloudinary.api_secret=your_cloudinary_api_secret


### 🧱 Database Setup

CREATE DATABASE peti;

### ▶️ Run the Application

#### Option 1: Using Maven

./mvnw clean package
java -jar target/peti.jar

#### Option 2: Using Docker

docker-compose up --build

App is available at `http://localhost:8080`

---

## 🧠 AI Integration

- Uses OpenAI GPT-4o to suggest outfits based on wardrobe data, weather, and user preferences
- Prompts include type, color, usage, and weather context

---

## 🌤️ Weather Support

- Uses Open-Meteo API and browser geolocation
- Current weather is displayed and influences outfit suggestions

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/peti/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── security/
│   │   ├── dto/
│   │   └── engine/
│   └── resources/
│       ├── templates/
│       ├── static/
│       └── application.properties
```

---

## 📋 API Endpoints

### 🔐 Authentication

| Method | Endpoint              | Description               |
|--------|-----------------------|---------------------------|
| POST   | `/register`           | User registration         |
| POST   | `/login`              | Form-based login          |
| POST   | `/api/auth/login`     | JWT-based API login       |

### 👗 Wardrobe Management

| Method | Endpoint                           | Description                    |
|--------|------------------------------------|--------------------------------|
| GET    | `/wardrobe`                        | View wardrobe dashboard        |
| GET    | `/wardrobe/upload`                 | Upload clothing form           |
| POST   | `/wardrobe/upload`                 | Add new clothing item          |
| GET    | `/wardrobe/search?q={term}`        | Search items                   |
| GET    | `/wardrobe/category/{category}`    | Filter by category             |
| POST   | `/wardrobe/{id}/use`               | Mark item as used              |
| DELETE | `/wardrobe/{id}`                   | Delete item                    |

---

## 🔐 Environment Variables

| Variable            | Description               |
|---------------------|---------------------------|
| `JWT_SECRET`        | JWT token encryption key  |
| `DB_USERNAME`       | MySQL username            |
| `DB_PASSWORD`       | MySQL password            |
| `OPENAI_API_KEY`    | GPT-4o access key         |
| `CLOUDINARY_*`      | Cloudinary credentials    |

---

## 🧪 Testing

Run tests with:

```bash
./mvnw test
```

Covered services:
- AuthService
- ReportService
- WardrobeService
- OutfitSuggestionService

---

## 📊 Data Models

### 👤 User

- Username, email, password
- Role (USER/ADMIN)
- Account creation metadata

### 👕 ClothingItem

- Name, category, color, season, occasion
- Image URL, usage tracking, description
- Linked to user

### 👔 Outfit

- Name, season, occasion
- Multiple items
- User association, usage metadata

### 📝 UsageLog

- Item and date used
- Optional notes
- Connected to outfit

---

## 🚧 Next Steps

### 🧠 Phase 2: Outfit Recommendation

- ✅ Combination logic
- ✅ Weather-based suggestions
- ✅ GPT-4o integration

### 📦 Phase 3: Advanced Features

- ⏳ Image recognition auto-tagging
- 📆 Monthly reports
- 🌱 Sustainability metrics
- 🌍 Social outfit sharing

### 🚀 Phase 4: Deployment & Optimization

- 🔧 Production DB & Docker
- 🔐 Secured environment configs
- 🚄 Performance improvements

---

## 📊 Admin Features

- View and manage users
- Track usage stats
- Visual analytics with Chart.js

---

## 🎨 UI Overview

- Gradient login/register UI
- Responsive Bootstrap layout
- Real-time weather widget
- Upload forms & filters

---

## 📝 License

MIT License — see LICENSE file.

---

**Author**: Tansel Seray Aydinli  
**Year**: 2025  
**PETI** — Making wardrobe management smart and sustainable! 👗🧥👞 