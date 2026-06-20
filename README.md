# KnowHow - Educational Platform for Knowledge Sharing

## About the Project

**KnowHow** is a microservices-based educational platform that allows users to create, sell, and purchase educational courses. The system includes moderation functionality, rating systems, user balance management, and email notifications.

### Key Features

- Authentication and authorization using JWT tokens stored in cookies
- Course creation and management with moderation system
- Balance system for purchasing courses with internal currency
- Rating system for course evaluation by users
- Email notifications for events (verification, purchase, moderation)
- Tags and search for convenient course navigation
- User profiles with purchase history and ratings

---

### Microservices

1. **Core Service** — main service implementing business logic:
   - User management
   - Course CRUD operations
   - Moderation system
   - Course purchasing
   - Ratings
   - Balance management

2. **Notification Service** — notification service:
   - Email sending
   - RabbitMQ event processing
   - Processing statistics

3. **Shared** — shared library:
   - Event DTOs
   - Common models and utilities

---

## Technology Stack

### Core Service
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Main language |
| Spring Boot | - | Framework |
| Spring Security | - | Authentication and authorization |
| Spring Data JPA | - | ORM and database operations |
| PostgreSQL | 17.5 | Primary database |
| Redis | 7.2 | Caching, verification code storage |
| RabbitMQ | 4.2 | Message broker |
| MapStruct | - | Object mapping |
| Nimbus JOSE JWT | - | JWT tokens |
| Flyway | - | Database migrations |
| Resilience4j | - | Retry mechanisms |
| SpringDoc OpenAPI | - | Swagger documentation |

### Notification Service
| Technology |
|------------|
| Spring Boot Starter Mail |
| Spring AMQP |

### Infrastructure
| Component |
|-----------|
| Docker |
| Jib |
| Lombok |

---

## Installation and Setup

### Prerequisites

- JDK 21 or higher
- Maven 3.9+
- Docker and Docker Compose

### Step 1: Clone Repository

```bash
git clone https://github.com/your-username/knowhow.git
cd knowhow
```

### Step 2: Build Project

```bash
mvn clean package
```

### Step 3: Start with Docker Compose

```bash
docker-compose up -d
```

### Step 4: Verify

- Swagger UI: http://localhost:8080/swagger-ui.html
- RabbitMQ Management: http://localhost:15672 (admin/admin123)
---

## API Endpoints

### Authentication
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/v1/auth/reg` | User registration |
| POST | `/api/v1/auth/login` | Login |
| GET | `/api/v1/auth/me` | Current user info |
| POST | `/api/v1/auth/logout` | Logout |

### Courses
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/v1/courses` | Create course |
| GET | `/api/v1/courses/search` | Search courses |
| GET | `/api/v1/courses/{id}` | Get course by ID |
| POST | `/api/v1/courses/pay/{id}` | Purchase course |
| PUT | `/api/v1/courses/retry-pass-moderation/{id}` | Resubmit for moderation |
| DELETE | `/api/v1/courses/{id}` | Delete course |

### Moderation
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/v1/courses/moderation/{id}/approve` | Approve course |
| POST | `/api/v1/courses/moderation/{id}/reject` | Reject course |
| GET | `/api/v1/courses/moderation/queue/on_moderation` | Courses in moderation |

### User
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/v1/profile` | User profile |
| GET | `/api/v1/users/purchased-courses` | Purchased courses |
| DELETE | `/api/v1/users` | Delete account |
| PATCH | `/api/v1/users/contact/email` | Update email |

### Balance
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/v1/balance/history` | Transaction history |
| PATCH | `/api/v1/balance` | Update balance |

### Ratings
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/v1/courses/{courseId}/rating` | Rate course |

### Tags
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/v1/tags` | Get all tags |

---

## Security

### Authentication
- JWT tokens in HttpOnly cookies
- CSRF protection with tokens
- BCrypt for password hashing

## Docker

### Building Images

```bash
# Build core-service
cd core-service
mvn compile jib:build

# Build notification-service
cd notification-service
mvn compile jib:build
```

### Docker Compose Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down

# Stop with volume removal
docker-compose down -v
```

---

## Monitoring

### RabbitMQ Metrics
- Confirm logging every 10 minutes
- Success/failure statistics

### Notification Statistics
- Logging every 60 minutes
- Success/failure processing counts
- Messages per second throughput

---

## License

This project is distributed under the MIT License. See LICENSE file for details.

---

## Contact

- **Email**: slava.vy.2006@gmail.com
- **Telegram**: [@perunve](https://t.me/perunve)

---

## Acknowledgments

- Spring Framework
- T-Bank Education
- All project contributors
