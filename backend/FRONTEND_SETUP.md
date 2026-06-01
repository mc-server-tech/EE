# Backend Frontend Setup Guide

## Quick Start

### 1. Start the Backend

```bash
cd /workspaces/EE/backend
npm install
cp .env.example .env
npm start
```

**Backend runs on: http://localhost:4000**

### 2. Access the Built-in Test UI

Open your browser and go to: **http://localhost:4000**

You'll see an interactive interface to test:
- Sign up (create account)
- Log in
- Verify tokens
- Get account info
- Change password

### 3. Add Your Own Frontend

Place your HTML/CSS/JavaScript files in:

```
/workspaces/EE/backend/public/
├── index.html
├── css/
│   └── styles.css
├── js/
│   └── app.js
└── images/
    └── logo.png
```

Your app will be served at http://localhost:4000

## How to Connect Your Frontend to the API

Your JavaScript can call the backend API like this:

### Signup

```javascript
const response = await fetch('/api/auth/signup', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'player123',
    password: 'password123'
  })
});
const data = await response.json();
// data.token contains your JWT token
localStorage.setItem('token', data.token);
```

### Login

```javascript
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'player123',
    password: 'password123'
  })
});
const data = await response.json();
localStorage.setItem('token', data.token);
```

### Get Account Info

```javascript
const token = localStorage.getItem('token');
const response = await fetch('/api/account', {
  method: 'GET',
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();
console.log(data); // { id, username, created_at, last_login }
```

### Change Password

```javascript
const token = localStorage.getItem('token');
const response = await fetch('/api/account/change-password', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    oldPassword: 'current123',
    newPassword: 'newpass456'
  })
});
```

## Example: Simple Frontend

Here's a minimal example to save in `public/index.html`:

```html
<!DOCTYPE html>
<html>
<head>
  <title>My Game Accounts</title>
</head>
<body>
  <h1>My Game Account</h1>
  
  <div id="loginForm" style="display: none;">
    <input type="text" id="username" placeholder="Username">
    <input type="password" id="password" placeholder="Password">
    <button onclick="login()">Login</button>
  </div>
  
  <div id="accountInfo" style="display: none;">
    <p>Logged in as: <span id="currentUser"></span></p>
    <button onclick="logout()">Logout</button>
  </div>

  <script>
    async function login() {
      const username = document.getElementById('username').value;
      const password = document.getElementById('password').value;
      
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      
      const data = await res.json();
      if (res.ok) {
        localStorage.setItem('token', data.token);
        showAccountInfo();
      } else {
        alert(data.error);
      }
    }
    
    function showAccountInfo() {
      document.getElementById('loginForm').style.display = 'none';
      document.getElementById('accountInfo').style.display = 'block';
      document.getElementById('currentUser').textContent = 
        localStorage.getItem('username');
    }
    
    function logout() {
      localStorage.removeItem('token');
      document.getElementById('loginForm').style.display = 'block';
      document.getElementById('accountInfo').style.display = 'none';
    }
    
    // Check if already logged in
    if (localStorage.getItem('token')) {
      showAccountInfo();
    }
  </script>
</body>
</html>
```

## Directory Structure

```
/workspaces/EE/
├── eagercrat-1.12-src/          (Client code - Java)
│   └── src/main/java/net/lax1dude/eaglercraft/profile/
│       ├── AccountManager.java
│       ├── GuiScreenAccountLogin.java
│       └── GuiScreenAccountSignup.java
│
└── backend/                     (Backend + Frontend server - Node.js)
    ├── server.js                (API server)
    ├── package.json
    ├── .env                     (Configuration - create from .env.example)
    ├── .env.example
    ├── Dockerfile
    ├── docker-compose.yml
    ├── accounts.db              (Created automatically)
    └── public/                  (👈 PUT YOUR HTML/CSS/JS HERE)
        ├── index.html           (Your app homepage)
        ├── css/
        │   └── styles.css
        ├── js/
        │   └── app.js
        └── images/
            └── logo.png
```

## Port Information

- **Backend API**: Port 4000 (http://localhost:4000)
- **Frontend**: Served from same port (http://localhost:4000)
- **All `/api/*` routes** go to the backend
- **All other routes** serve static files from `public/`

## Troubleshooting

**Can't connect to backend?**
- Make sure backend is running: `npm start` from `/workspaces/EE/backend`
- Check port 4000 is available
- Look for error messages in terminal

**Frontend not appearing?**
- Make sure you have a file at `public/index.html`
- Check that files are in the right directory structure
- Refresh browser and check browser console for errors

**Tokens not working?**
- Make sure you're sending token in Authorization header: `Authorization: Bearer YOUR_TOKEN`
- Tokens expire after 7 days
- Check that token is stored correctly in localStorage

## Documentation

- Full API docs: `/workspaces/EE/backend/README.md`
- Frontend setup guide: `/workspaces/EE/backend/public/README.md`
- Integration guide: `/workspaces/EE/backend/INTEGRATION.md`
- Quick start: `/workspaces/EE/backend/QUICKSTART.md`
