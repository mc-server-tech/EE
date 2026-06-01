# Frontend Files

This directory contains your frontend (HTML, CSS, JavaScript, images, etc.) that the backend will serve.

## How It Works

When you run the backend server (`npm start`), it will serve all files from this directory at `http://localhost:4000/`.

### Examples

- `public/index.html` → http://localhost:4000/
- `public/style.css` → http://localhost:4000/style.css
- `public/js/app.js` → http://localhost:4000/js/app.js
- `public/images/logo.png` → http://localhost:4000/images/logo.png

## Setting Up Your Frontend

### Option 1: Plain HTML/CSS/JavaScript

Just place your files here:

```
public/
├── index.html
├── style.css
├── app.js
└── images/
    └── logo.png
```

### Option 2: Build Output from a Framework

If using React, Vue, Angular, or similar:

1. Configure your build tool to output to this `public/` directory
2. The backend will serve the built files
3. All non-API routes will serve `index.html` (for client-side routing)

**Example for React:**
```bash
# In your React project's package.json
"build": "vite build --outDir ../backend/public"

# Or with Create React App
"build": "react-scripts build && mv build/* ../backend/public"
```

### Option 3: Use the Built-in Test UI

An interactive test interface is included at `public/index.html`. You can use it to test the account system directly in your browser.

## API Communication

Your frontend can make requests to the backend API at these endpoints (all starting with `/api`):

```javascript
// Signup
fetch('/api/auth/signup', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'player', password: 'pass123' })
})

// Login
fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'player', password: 'pass123' })
})

// Get account info (requires Bearer token)
fetch('/api/account', {
  method: 'GET',
  headers: { 'Authorization': 'Bearer YOUR_TOKEN_HERE' }
})
```

See `../README.md` for complete API documentation.

## Notes

- The backend runs on port 4000 by default
- All files are served as static files (no processing)
- `/api/*` routes always go to the backend (not to static files)
- For SPAs, the backend redirects non-API routes to `index.html`
