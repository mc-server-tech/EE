# Eaglercraft Account System Integration Guide

This document explains how the client-side and backend account systems work together.

## Architecture

### Client-Side (`eagercrat-1.12-src/src/main/java/net/lax1dude/eaglercraft/profile/`)

- **AccountManager.java** - Manages local account storage in browser/client
- **GuiScreenAccountLogin.java** - Login UI screen
- **GuiScreenAccountSignup.java** - Signup UI screen
- **GuiScreenEditProfile.java** - Profile editor (already existed)

**Current Mode**: Client-side only. Accounts are stored locally in the client's storage.

### Backend Server (`backend/`)

- **server.js** - Express API server for centralized account management
- **SQLite database** - Persistent account storage
- **JWT authentication** - Token-based session management

**Features**:
- User registration with validation
- Secure login with bcrypt hashing
- Account info retrieval
- Password change
- Token verification

## Usage

### Client-Only (Current)

Users can create and login to local accounts on their device:

1. Start Eaglercraft
2. Click "Account Login" on main menu
3. Either login with existing credentials or signup for new account
4. Account is saved to browser storage

**Pros**: Works offline, no server needed
**Cons**: Account doesn't sync across devices, no social features

### Backend with Web Frontend

The backend can also serve your own custom web interface:

1. Start backend server (`npm start`)
2. Place your HTML/CSS/JS in the `public/` directory
3. Access your app at http://localhost:4000
4. Your app makes requests to `/api/*` endpoints

**Built-in Test UI**: Open http://localhost:4000 in your browser to see a pre-built testing interface for the account system.

#### Directory Structure

```
backend/public/
├── index.html              (Served at /)
├── css/
│   └── styles.css
├── js/
│   └── app.js
└── images/
    └── logo.png
```

Any file in `public/` is served directly by the backend.

### With Backend Server (Future)

To enable centralized account management:

#### Step 1: Start Backend Server

```bash
cd backend
npm install
cp .env.example .env
# Edit .env and set JWT_SECRET
npm start
```

Backend will run on `http://localhost:4000`

Frontend UI is available at `http://localhost:4000/` (built-in test interface)

#### Step 2: Update Client

Modify the client's `GuiScreenAccountLogin.java` and `GuiScreenAccountSignup.java` to make HTTP requests to the backend:

```java
// Example addition to GuiScreenAccountLogin.attemptLogin()
String apiUrl = "http://localhost:4000/api/auth/login";
// Make POST request to backend with username/password
// Get back token and store it
```

#### Step 3: Backend Provides

- **Signup**: POST `/api/auth/signup` with username/password
- **Login**: POST `/api/auth/login` with username/password → returns JWT token
- **Verify**: POST `/api/auth/verify` with token
- **Account Info**: GET `/api/account` with Bearer token

## Deployment

### Backend on VPS

```bash
# Install Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Clone repo and setup
cd /opt/eaglercraft-backend
npm install
cp .env.example .env
# Edit .env with strong JWT_SECRET

# Use PM2 for process management
sudo npm install -g pm2
pm2 start server.js --name "eaglercraft-api"
pm2 startup
pm2 save

# Use nginx as reverse proxy
```

### Environment Variables

Set `EAGLERCRAFT_API_URL` on client to point to your backend:

Example in HTML page:
```html
<script>
  window.EAGLERCRAFT_API_URL = "https://api.yourdomain.com";
</script>
```

## Security Checklist for Production

- [ ] Change JWT_SECRET to a strong random value
- [ ] Use HTTPS for all connections
- [ ] Implement rate limiting on backend
- [ ] Add CAPTCHA to signup endpoint
- [ ] Log authentication attempts
- [ ] Implement account lockout after failed attempts
- [ ] Use CORS properly (don't allow all origins)
- [ ] Keep dependencies updated
- [ ] Monitor database for anomalies
- [ ] Regular backups of accounts database

## API Response Examples

### Successful Signup
```json
{
  "success": true,
  "message": "Account created successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "player123"
}
```

### Successful Login
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "player123"
}
```

### Error Response
```json
{
  "error": "Invalid username or password"
}
```

## Future Enhancements

- [ ] Social features (friends list)
- [ ] Account recovery via email
- [ ] Two-factor authentication
- [ ] Profile customization
- [ ] Account linking to Discord/other platforms
- [ ] Statistics tracking
- [ ] Ban system
- [ ] Admin dashboard
