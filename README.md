AI-Powered Movie Recommendation Platform · Java Spring Boot · React · TypeScript · Qdrant Vector DB
# 🎬 MovieMentor  
### AI-Powered Movie Recommendation Platform  
**Full-Stack System Built with Java Spring Boot, React + TypeScript, Qdrant Vector DB, Redis, JWT Authentication & Embedding-Based Recommendations**

MovieMentor is a full-stack movie discovery platform designed to recommend movies using **AI-driven semantic similarity**, **user profiling**, **genre clustering**, and **vector embeddings**.  
It combines **backend engineering**, **frontend development**, **vector search**, **ML concepts**, and a polished user experience — creating a production-style project.

---

# 🚀 Features

## 🔐 **Authentication & User Management**
- Secure login & register flows (JWT)
- Password hashing (BCrypt)
- User profile + stored preferences
- Protected routes on both backend & frontend

---

## 🎥 **Movie Data & Content**
- Movies include:
  - Title, original title
  - Overview & synopsis
  - Trailer URL
  - Poster URL
  - Release date, popularity, ratings
  - Actors & cast information
  - Genres & categories
  - Image gallery

---

## 🤖 **AI Recommendation Engine**
### Built using:
- **Semantic Embeddings**
- **Vector Database (Qdrant)**
- **Metadata-based filtering**
- **User viewing history + favorites**

### Recommendation Strategies:
- Similar movies by embedding distance  
- Hybrid scoring:
  - Genre similarity  
  - Actor overlap  
  - Popularity weighting  
  - Semantic relevance  

### Results:
Smooth, responsive and **personalized recommendations** for every user.

---

## ⚡ **Tech Stack Overview**

### 🟦 Backend (Java · Spring Boot)
- Spring Web (REST)
- Spring Security + JWT
- Lombok
- JPA / Hibernate
- Qdrant Client for Vector Search
- Redis caching layer
- Exception handling + validation
- Modular controller/service/repository architecture

### 🟩 Frontend (React · TypeScript)
- Vite build system
- React Router
- Axios client
- Toast notifications
- Responsive UI components
- Movie carousel & gallery
- Authentication context provider
- Clean file structure

### 🔳 Databases
- **Qdrant vector DB** – movie embeddings  
- **Redis** – cache layer for hot endpoints  
- PostgreSQL (depending on your setup)

---

# 🧠 System Architecture


            ┌─────────────────┐
            │  React Client   │
            │  (TypeScript)   │
            └───────┬────────┘
                    │
            HTTP / Axios
                    │
        ┌───────────▼───────────┐
        │   Spring Boot API     │
        │ Auth · Movies · Reco  │
        └───────┬───────┬──────┘
                │       │
         JWT Auth   Metadata Logic
                │
 ┌──────────────▼──────────────┐
 │      Recommendation Engine   │
 │ Embeddings + Genre + Actors │
 └───────────┬───────────┬─────┘
             │           │
 ┌───────────▼───┐   ┌───▼──────────┐
 │   Qdrant DB   │   │   Redis Cache │
 │  Vector Index │   │  Hot Queries  │
 └───────────────┘   └──────────────┘


---

# 🌟 Key Backend Endpoints

## Auth  


POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout


## Movies  


GET /api/movies
GET /api/movies/{id}
GET /api/movies/top-rated
GET /api/movies/now-playing
GET /api/movies/upcoming
GET /api/movies/search?query=...


## Recommendations  


GET /api/user/recommendations
GET /api/user/history
POST /api/user/history/{movieId}
POST /api/user/favorites/{movieId}


---

# 🧩 Folder Structure  
*(recommended final structure)*



MovieMentor/
│
├── backend/
│ ├── src/main/java/movieMentor/
│ │ ├── controllers/
│ │ ├── services/
│ │ ├── repositories/
│ │ ├── config/
│ │ ├── security/
│ │ └── recommendation/
│ └── src/main/resources/
│
├── frontend/
│ ├── src/
│ │ ├── components/
│ │ ├── pages/
│ │ ├── hooks/
│ │ ├── api/
│ │ └── styles/
│ └── public/
│
└── README.md


---

# 🛠️ Installation & Setup

## 🟦 Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run

Environment variables:
JWT_SECRET=yourSecretKey
QDRANT_URL=http://localhost:6333
REDIS_HOST=localhost

🟩 Frontend
cd frontend
npm install
npm run dev


🧪 Future Improvements

Multilingual metadata support

Collaborative filtering model

Real user preference learning

Trending model based on time windows

ElasticSearch-based hybrid search

Admin panel for movie ingestion

📬 Contact

Author: Elishay Amar
📧 amarElishay@gmail.com

💼 LinkedIn: www.linkedin.com/in/elishay-amar-8b9b38221

⭐ If you like this project

Please consider starring the repository to support future updates!

