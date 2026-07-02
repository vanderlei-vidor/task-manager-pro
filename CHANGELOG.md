# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Real-time notifications (WebSocket) - planned
- Task comments and activity log - planned
- File attachments - planned

### Changed
- Improved performance of Kanban board rendering

### Fixed
- Calendar navigation on mobile devices

---

## [1.0.0] - 2026-07-02

### Added

#### 🎯 Core Features
- Dashboard with real-time statistics and progress tracking
- Kanban board with 3-column workflow (TODO, DOING, DONE)
- Calendar system with month/week/day views
- Tag management system with custom colors
- Global search with real-time filtering
- Keyboard shortcuts (Ctrl+N, Ctrl+K, Ctrl+D, Esc, ?)

#### 🔐 Security & Authentication
- JWT authentication with HS256 signing
- Refresh token rotation (prevents replay attacks)
- Automatic token cleanup (scheduled job at 3 AM)
- Spring Security integration
- BCrypt password hashing
- Brute-force protection (5 attempts → 15min lockout)
- Rate limiting (60 req/min, 1000 req/hour)
- Multi-tenant data isolation
- Security headers (CSP, HSTS, X-Frame-Options, etc.)

#### 📊 Reports & Analytics
- PDF export (OpenPDF engine)
- Excel export (Apache POI)
- Charts (Chart.js): doughnut, bar, line
- Activity heatmap (GitHub-style)
- Progress ring with animations

#### 🎨 UI/UX
- Dark mode with smooth transitions
- Responsive design (mobile-first)
- Toast notifications system
- Animated progress bars
- Micro-interactions and hover effects
- Professional design system (inspired by Linear, Notion, Asana)

#### 🏗️ Architecture
- Layered architecture (Controller → Service → Repository)
- DTO pattern with MapStruct
- Modular SCSS (7-1 pattern)
- ES6 JavaScript modules
- Vite bundler for frontend assets
- Environment variables management (.env + .env.example)

#### 🧪 Testing & Quality
- 104 automated tests (88 unit + 16 integration)
- 100% coverage on critical services (JWT, RefreshToken, Task)
- Testcontainers for real PostgreSQL in tests
- JaCoCo coverage reporting
- JUnit 5 + Mockito + AssertJ

#### 🐳 DevOps & Deployment
- Docker containerization (multi-stage build)
- docker-compose.yml for full stack
- GitHub Actions CI/CD pipeline
- Railway/Render deployment ready
- Environment-based configuration

#### 📚 Documentation
- Professional README with 13 sections
- ROADMAP.md with detailed phases
- CONTRIBUTING.md for contributors
- CODE_OF_CONDUCT.md
- SECURITY.md policy
- CHANGELOG.md (this file)

### Security
- Implemented JWT with stateless authentication
- Added refresh token rotation to prevent replay attacks
- Configured brute-force protection
- Enabled rate limiting on all endpoints
- Added security headers (CSP, HSTS, X-Frame-Options)
- Implemented multi-tenant data isolation
- Used BCrypt for password hashing
- Prevented SQL injection with JPA/Hibernate
- Added XSS protection
- Configured CORS properly

### Performance
- Optimized database queries with proper indexing
- Implemented lazy loading for relationships
- Added caching for frequently accessed data
- Optimized frontend with Vite bundling
- Minimized CSS/JS with production builds
- Implemented pagination for large datasets

### Documentation
- Created comprehensive README
- Added Javadoc for all public methods
- Created .env.example for easy setup
- Added architecture diagrams
- Documented API endpoints
- Created contributing guidelines

---

## [0.9.0] - 2026-06-15

### Added
- Initial implementation of task CRUD operations
- Basic user authentication with sessions
- PostgreSQL database integration
- Thymeleaf templates
- Basic responsive design

### Changed
- Migrated from H2 to PostgreSQL for production readiness

### Fixed
- Fixed timezone issues in calendar
- Resolved memory leaks in session management

---

## [0.1.0] - 2026-05-01

### Added
- Initial project setup
- Spring Boot 3.4 configuration
- Basic project structure
- README.md

---

## Types of Changes

- **Added** for new features
- **Changed** for changes in existing functionality
- **Deprecated** for soon-to-be removed features
- **Removed** for now removed features
- **Fixed** for any bug fixes
- **Security** in case of vulnerabilities

---

<div align="center">

**Keep your changelog updated!** 📝

<br/>

<a href="#changelog">
  <img src="https://img.shields.io/badge/↑ Back to Top-6366F1?style=for-the-badge" alt="Back to Top"/>
</a>

</div>