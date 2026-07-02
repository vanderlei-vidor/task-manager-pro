# 🤝 Contributing to Task Manager Pro

> Thank you for your interest in contributing! This guide will help you get started.

<div align="center">

<img src="https://img.shields.io/badge/Contributions-Welcome-brightgreen?style=for-the-badge" alt="Contributions Welcome"/>

</div>

---

## 🎯 Ways to Contribute

You don't need to write code to help! Here are some ways:

### 🐛 **Report Bugs**
Found something broken? [Open an issue](https://github.com/vanderlei-vidor/task-manager-pro/issues/new) with:
- Clear description
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)

### 💡 **Suggest Features**
Have an idea? [Open a feature request](https://github.com/vanderlei-vidor/task-manager-pro/issues/new) and let's discuss!

### 📝 **Improve Documentation**
- Fix typos or unclear sections
- Add examples or tutorials
- Translate to other languages

### 🧪 **Write Tests**
Help us maintain **100% coverage** on critical services!

### ⭐ **Spread the Word**
- Star the repository
- Share on social media
- Write a blog post about it

---

## 🚀 Development Setup

### Prerequisites

- **Java 17+** — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/)
- **PostgreSQL 15+** — [Download](https://www.postgresql.org/)
- **Node.js 18+** — [Download](https://nodejs.org/)

### Quick Start

```bash
# 1. Fork and clone
git clone https://github.com/vanderlei-vidor/task-manager-pro.git
cd task-manager-pro

# 2. Create feature branch
git checkout -b feature/my-feature

# 3. Setup environment
cp .env.example .env
# Edit .env with your settings

# 4. Install dependencies
./mvnw clean install
npm install

# 5. Build frontend
npm run sass:watch  # Keep running

# 6. Start application
./mvnw spring-boot:run

# 7. Open http://localhost:8081
```

---

## 📏 Coding Standards

### Java (Backend)

✅ **DO:**
```java
// Use meaningful names
public class TaskService {
    private final TaskRepository taskRepository;
    
    public TaskDTO createTask(TaskDTO dto, String userEmail) {
        // Clear, descriptive code
    }
}
```

❌ **DON'T:**
```java
// Avoid abbreviations
public class TaskService {
    private final TaskRepository r;
    public TaskDTO c(TaskDTO d, String e) { }
}
```

**Key rules:**
- Use `@RequiredArgsConstructor` for dependency injection
- Use `final` for fields when possible
- Write Javadoc for public methods
- Use DTOs — never expose entities in controllers
- Follow SOLID principles
- Keep methods under 30 lines

### JavaScript (Frontend)

✅ **DO:**
```javascript
// Use ES6 modules
import { Toast } from '../core/toast.js';

class TaskManager {
    async loadTasks() {
        // Async/await for clarity
    }
}
```

**Key rules:**
- Use `const` and `let` (never `var`)
- Use arrow functions for callbacks
- Use async/await instead of callbacks
- Keep functions pure when possible

### SCSS (Styles)

We follow the **7-1 Pattern**:
- Use variables from `_variables.scss`
- Use mixins from `_mixins.scss`
- Keep specificity low
- Mobile-first responsive design

---

## 🔄 Pull Request Process

### Before Submitting

- [ ] Code compiles without errors
- [ ] All tests pass (`./mvnw test`)
- [ ] Coverage maintained (no decrease)
- [ ] Documentation updated (if needed)
- [ ] Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/)

### Commit Message Format

```
<type>(<scope>): <subject>

Examples:
feat(auth): add refresh token rotation
fix(kanban): resolve drag-and-drop issue
docs(readme): update installation guide
test(service): add unit tests for TaskService
```

### Submitting

1. Push your branch: `git push origin feature/my-feature`
2. Open a Pull Request on GitHub
3. Fill out the PR template
4. Link related issues: `Closes #123`
5. Request review from maintainers

---

## 🧪 Testing

```bash
# All tests
./mvnw test

# Unit tests only
./mvnw test -Dtest='*ServiceTest'

# Integration tests only
./mvnw test -Dtest='*IntegrationTest'

# Coverage report
./mvnw clean test jacoco:report
```

**Testing guidelines:**
- Follow **AAA pattern** (Arrange, Act, Assert)
- Use **descriptive names** (`shouldCreateTaskWhenValid`)
- Use **@DisplayName** for readability
- Test **edge cases** and error scenarios
- Maintain **100% coverage** on critical services

---

## 🐛 Reporting Bugs

Use our [Bug Report Template](.github/ISSUE_TEMPLATE/bug_report.md) and include:

- **Description**: What's the bug?
- **Steps to reproduce**: How can we see it?
- **Expected behavior**: What should happen?
- **Actual behavior**: What actually happened?
- **Environment**: OS, browser, Java version
- **Screenshots**: If applicable

---

## 💡 Suggesting Features

Use our [Feature Request Template](.github/ISSUE_TEMPLATE/feature_request.md) and include:

- **Problem**: What problem does this solve?
- **Solution**: What do you want to happen?
- **Alternatives**: Other solutions considered?
- **Context**: Mockups, examples, references

---

## 📚 Documentation

Update docs when:
- ✅ New feature added
- ✅ API changes
- ✅ Configuration changes
- ✅ Installation steps change

---

## 👥 Community

- 🐙 **GitHub**: [github.com/vanderlei-vidor/task-manager-pro](https://github.com/vanderlei-vidor/task-manager-pro)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/vanderlei-vidor/task-manager-pro/discussions)

---

## 🏆 Recognition

All contributors are recognized in our [Contributors](https://github.com/vanderlei-vidor/task-manager-pro/graphs/contributors) page!

---

## 📄 License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).

---

<div align="center">

> 💡 *"Alone we can do so little; together we can do so much."* — Helen Keller

<br/>

**Thank you for contributing!** 🙏

<br/>

<a href="#-contributing-to-task-manager-pro">
  <img src="https://img.shields.io/badge/↑ Back to Top-6366F1?style=for-the-badge" alt="Back to Top"/>
</a>

</div>