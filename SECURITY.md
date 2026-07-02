# Security Policy

## Supported Versions

Use this section to tell people about which versions of your project are
currently being supported with security updates.

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please follow these steps:

### 📧 How to Report

**DO NOT** open a public issue. Instead:

1. Email us at: **[vanderleividor1@gmail.com]**
2. Use the subject line: `Security Vulnerability: [Brief Description]`
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if you have one)

### 🕐 What to Expect

- **Acknowledgment**: Within 48 hours
- **Initial Assessment**: Within 7 days
- **Regular Updates**: Every 5 business days until resolved
- **Disclosure**: We'll work with you on a coordinated disclosure timeline

### 🔒 Security Best Practices

When contributing to this project:

- ✅ Keep dependencies updated
- ✅ Use strong, unique passwords
- ✅ Enable 2FA on your GitHub account
- ✅ Never commit secrets or credentials
- ✅ Report suspicious activity
- ✅ Follow the principle of least privilege

### 🛡️ Security Features

This project implements:

- JWT authentication with refresh token rotation
- BCrypt password hashing
- Rate limiting (60 req/min, 1000 req/hour)
- Brute-force protection (5 attempts → 15min lockout)
- Multi-tenant data isolation
- Security headers (CSP, HSTS, X-Frame-Options)
- Input validation and sanitization
- SQL injection prevention (JPA/Hibernate)
- XSS protection

### 📋 Security Checklist

Before deploying:

- [ ] Review all dependencies for known vulnerabilities
- [ ] Use strong JWT secrets (64+ characters)
- [ ] Enable HTTPS in production
- [ ] Configure CORS properly
- [ ] Set up security headers
- [ ] Enable rate limiting
- [ ] Configure database access controls
- [ ] Set up monitoring and alerting
- [ ] Review authentication flows
- [ ] Test authorization boundaries

### 🏆 Security Acknowledgments

We appreciate responsible disclosure. Contributors who report valid security vulnerabilities will be acknowledged in our security hall of fame (unless they prefer to remain anonymous).

---

**Thank you for helping keep Task Manager Pro secure!** 🔒

For general questions about security features, please open a [Discussion](https://github.com/vanderlei-vidor/task-manager-pro/discussions).