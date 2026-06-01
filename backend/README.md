# Eaglercraft Accounts Backend

A simple Node.js/Express backend server for managing custom Eaglercraft player accounts.

## Features

- User registration with validation
- Secure login with bcrypt password hashing
- JWT token-based authentication
- Account info retrieval
- Password change functionality
- SQLite database for account storage
- **Serves frontend files** - Static files from `public/` directory
- Built-in web UI at http://localhost:4000

## Frontend Setup

Place your HTML, CSS, and JavaScript files in the `public/` directory. The server will serve them automatically:

```
backend/
├── public/
│   ├── index.html           (Main page - served at /)
│   ├── css/
│   │   └── styles.css       (Available at /css/styles.css)
│   ├── js/
│   │   └── app.js           (Available at /js/app.js)
│   └── images/
│       └── logo.png
```

A built-in web UI for testing the account system is included at `public/index.html`. 

### For Single Page Apps (React, Vue, Angular, etc.)

1. Build your app to the `public/` directory
2. The server routes all non-API paths to `index.html`
3. Your app handles routing on the client side

**API routes** (`/api/*`) are always routed to the backend.

## Installation

```bash
cd backend
npm install
```

## Configuration

Create a `.env` file in the backend directory:

```env
PORT=4000
JWT_SECRET=your-very-secret-key-change-this
DB_PATH=./accounts.db
```

## Running

```bash
npm start
```

Or with auto-reload in development:

```bash
npm run dev
```

## API Endpoints

### Sign Up
```
POST /api/auth/signup
Content-Type: application/json

{
  "username": "player123",
  "password": "password123"
}

Response:
{
  "success": true,
  "message": "Account created successfully",
  "token": "eyJhbGc...",
  "username": "player123"
}
```

### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "player123",
  "password": "password123"
}

Response:
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGc...",
  "username": "player123"
}
```

### Verify Token
```
POST /api/auth/verify
Content-Type: application/json

{
  "token": "eyJhbGc..."
}

Response:
{
  "valid": true,
  "username": "player123"
}
```

### Get Account Info
```
GET /api/account
Authorization: Bearer eyJhbGc...

Response:
{
  "id": 1,
  "username": "player123",
  "created_at": "2026-06-01 12:00:00",
  "last_login": "2026-06-01 14:30:00"
}
```

### Change Password
```
POST /api/account/change-password
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "oldPassword": "password123",
  "newPassword": "newpassword456"
}

Response:
{
  "success": true,
  "message": "Password changed successfully"
}
```

### Health Check
```
GET /api/health

Response:
{
  "status": "ok",
  "timestamp": "2026-06-01T12:00:00.000Z"
}
```

## Validation Rules

- **Username**: 3-16 characters, alphanumeric + underscore and hyphen
- **Password**: Minimum 6 characters

## Database

The backend uses SQLite for data storage. The database file is created automatically on first run at the path specified in `.env` (default: `./accounts.db`).

### Database Schema

```sql
CREATE TABLE accounts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_login DATETIME,
  is_active INTEGER DEFAULT 1
);
```

## Security Notes

- All passwords are hashed with bcryptjs before storage
- Tokens expire after 7 days
- Change the JWT_SECRET in production
- Use HTTPS in production
- Consider implementing rate limiting for production
