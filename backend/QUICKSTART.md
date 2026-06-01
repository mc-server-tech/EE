# Quick Start - Backend

## Local Development

### Prerequisites
- Node.js 16+ and npm

### Setup

```bash
# Install dependencies
npm install

# Create environment file
cp .env.example .env

# Edit .env and set a JWT_SECRET
nano .env

# Start development server (with auto-restart)
npm run dev
```

Server runs on http://localhost:4000

Frontend UI is available at: http://localhost:4000/

### Test Endpoints

```bash
# Health check
curl http://localhost:4000/api/health

# Signup
curl -X POST http://localhost:4000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"player123","password":"password123"}'

# Login
curl -X POST http://localhost:4000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"player123","password":"password123"}'
```

## Where to Put Your Frontend

### Static Files Directory

Place your HTML, CSS, and JavaScript files in the `public/` directory:

```
backend/
├── public/
│   ├── index.html          (Main page)
│   ├── css/
│   │   └── styles.css
│   ├── js/
│   │   └── app.js
│   └── images/
│       └── logo.png
├── server.js
└── ...
```

Any file in `public/` will be served directly. For example:
- `public/index.html` → http://localhost:4000/
- `public/css/styles.css` → http://localhost:4000/css/styles.css
- `public/js/app.js` → http://localhost:4000/js/app.js

### Single Page Apps (React, Vue, etc.)

If using a frontend framework:

1. Build your app to output files in the `public/` directory
2. The server will serve `index.html` for any non-API routes
3. Your app handles routing on the client side
4. All `/api/*` routes go to the backend

Example with React:
```bash
# Build your React app to public/
npm run build -- --outDir ../public

# Start backend - your app is now served from there
cd ../backend
npm start
```

## Docker Deployment

### Build and Run

```bash
# Build image
docker build -t eaglercraft-api .

# Run container
docker run -p 4000:4000 \
  -e JWT_SECRET=your-secret-key \
  -v ./data:/app/data \
  eaglercraft-api
```

### Docker Compose (Recommended)

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Production Checklist

- [ ] Set strong JWT_SECRET in .env
- [ ] Enable HTTPS/TLS
- [ ] Set up reverse proxy (nginx)
- [ ] Configure CORS for your domain
- [ ] Enable rate limiting
- [ ] Set up database backups
- [ ] Monitor logs and errors
- [ ] Use PM2 for process management
- [ ] Update dependencies regularly
- [ ] Set up health monitoring

## Database

SQLite database is created automatically in `./data/accounts.db` on first run.

Backup regularly:
```bash
cp data/accounts.db data/accounts.db.backup
```

## API Documentation

See [README.md](README.md) for complete API documentation and examples.

See [INTEGRATION.md](INTEGRATION.md) for client integration guide.

