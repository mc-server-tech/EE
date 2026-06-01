const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 4000;
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key-change-in-production';
const DB_PATH = process.env.DB_PATH || './accounts.db';

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Serve static frontend files from public directory
app.use(express.static(path.join(__dirname, 'public')));

// Initialize SQLite Database
const db = new sqlite3.Database(DB_PATH, (err) => {
	if (err) {
		console.error('Error opening database:', err);
	} else {
		console.log('Connected to SQLite database at ' + DB_PATH);
		initializeDatabase();
	}
});

// Initialize database schema
function initializeDatabase() {
	db.run(`
		CREATE TABLE IF NOT EXISTS accounts (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			username TEXT UNIQUE NOT NULL,
			password_hash TEXT NOT NULL,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			last_login DATETIME,
			is_active INTEGER DEFAULT 1
		)
	`, (err) => {
		if (err) {
			console.error('Error creating accounts table:', err);
		} else {
			console.log('Accounts table initialized');
		}
	});

	// Friends table
	db.run(`
		CREATE TABLE IF NOT EXISTS friends (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			user_id INTEGER NOT NULL,
			friend_id INTEGER NOT NULL,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY(user_id) REFERENCES accounts(id),
			FOREIGN KEY(friend_id) REFERENCES accounts(id),
			UNIQUE(user_id, friend_id)
		)
	`, (err) => {
		if (err) {
			console.error('Error creating friends table:', err);
		} else {
			console.log('Friends table initialized');
		}
	});

	// Friend requests table
	db.run(`
		CREATE TABLE IF NOT EXISTS friend_requests (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			from_user_id INTEGER NOT NULL,
			to_user_id INTEGER NOT NULL,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			status TEXT DEFAULT 'pending',
			FOREIGN KEY(from_user_id) REFERENCES accounts(id),
			FOREIGN KEY(to_user_id) REFERENCES accounts(id),
			UNIQUE(from_user_id, to_user_id)
		)
	`, (err) => {
		if (err) {
			console.error('Error creating friend_requests table:', err);
		} else {
			console.log('Friend requests table initialized');
		}
	});

	// Blocked users table
	db.run(`
		CREATE TABLE IF NOT EXISTS blocked_users (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			blocker_id INTEGER NOT NULL,
			blocked_id INTEGER NOT NULL,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY(blocker_id) REFERENCES accounts(id),
			FOREIGN KEY(blocked_id) REFERENCES accounts(id),
			UNIQUE(blocker_id, blocked_id)
		)
	`, (err) => {
		if (err) {
			console.error('Error creating blocked_users table:', err);
		} else {
			console.log('Blocked users table initialized');
		}
	});

	// Online status table
	db.run(`
		CREATE TABLE IF NOT EXISTS online_status (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			user_id INTEGER NOT NULL UNIQUE,
			last_seen DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY(user_id) REFERENCES accounts(id)
		)
	`, (err) => {
		if (err) {
			console.error('Error creating online_status table:', err);
		} else {
			console.log('Online status table initialized');
		}
	});
}

// Helper function to validate username
function isValidUsername(username) {
	return username && username.length >= 3 && username.length <= 16 && /^[a-zA-Z0-9_-]+$/.test(username);
}

// Helper function to validate password
function isValidPassword(password) {
	return password && password.length >= 6;
}

// Route: Health check
app.get('/api/health', (req, res) => {
	res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Route: Sign up
app.post('/api/auth/signup', async (req, res) => {
	const { username, password } = req.body;

	// Validation
	if (!username || !password) {
		return res.status(400).json({ error: 'Username and password are required' });
	}

	if (!isValidUsername(username)) {
		return res.status(400).json({ error: 'Username must be 3-16 characters, alphanumeric + _ and -' });
	}

	if (!isValidPassword(password)) {
		return res.status(400).json({ error: 'Password must be at least 6 characters' });
	}

	try {
		// Check if user already exists
		db.get('SELECT id FROM accounts WHERE username = ?', [username], async (err, row) => {
			if (err) {
				return res.status(500).json({ error: 'Database error' });
			}

			if (row) {
				return res.status(409).json({ error: 'Username already exists' });
			}

			// Hash password
			const hashedPassword = await bcrypt.hash(password, 10);

			// Insert new account
			db.run(
				'INSERT INTO accounts (username, password_hash) VALUES (?, ?)',
				[username, hashedPassword],
				(err) => {
					if (err) {
						console.error('Error creating account:', err);
						return res.status(500).json({ error: 'Failed to create account' });
					}

					// Generate token
					const token = jwt.sign({ username }, JWT_SECRET, { expiresIn: '7d' });
					res.status(201).json({
						success: true,
						message: 'Account created successfully',
						token,
						username
					});
				}
			);
		});
	} catch (err) {
		console.error('Error in signup:', err);
		res.status(500).json({ error: 'Internal server error' });
	}
});

// Route: Log in
app.post('/api/auth/login', async (req, res) => {
	const { username, password } = req.body;

	// Validation
	if (!username || !password) {
		return res.status(400).json({ error: 'Username and password are required' });
	}

	try {
		// Find user
		db.get('SELECT * FROM accounts WHERE username = ?', [username], async (err, row) => {
			if (err) {
				return res.status(500).json({ error: 'Database error' });
			}

			if (!row) {
				return res.status(401).json({ error: 'Invalid username or password' });
			}

			// Check if account is active
			if (!row.is_active) {
				return res.status(403).json({ error: 'Account is disabled' });
			}

			// Compare password
			const isPasswordValid = await bcrypt.compare(password, row.password_hash);
			if (!isPasswordValid) {
				return res.status(401).json({ error: 'Invalid username or password' });
			}

			// Update last login
			db.run('UPDATE accounts SET last_login = CURRENT_TIMESTAMP WHERE id = ?', [row.id]);

			// Generate token
			const token = jwt.sign({ username, id: row.id }, JWT_SECRET, { expiresIn: '7d' });
			res.json({
				success: true,
				message: 'Login successful',
				token,
				username
			});
		});
	} catch (err) {
		console.error('Error in login:', err);
		res.status(500).json({ error: 'Internal server error' });
	}
});

// Route: Verify token
app.post('/api/auth/verify', (req, res) => {
	const { token } = req.body;

	if (!token) {
		return res.status(400).json({ error: 'Token is required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		res.json({
			valid: true,
			username: decoded.username
		});
	} catch (err) {
		res.status(401).json({
			valid: false,
			error: 'Invalid or expired token'
		});
	}
});

// Route: Get account info (requires token)
app.get('/api/account', (req, res) => {
	const authHeader = req.headers.authorization;
	if (!authHeader || !authHeader.startsWith('Bearer ')) {
		return res.status(401).json({ error: 'No token provided' });
	}

	const token = authHeader.substring(7);
	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id, username, created_at, last_login FROM accounts WHERE username = ?', [decoded.username], (err, row) => {
			if (err || !row) {
				return res.status(404).json({ error: 'Account not found' });
			}
			res.json(row);
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Route: Change password (requires token)
app.post('/api/account/change-password', async (req, res) => {
	const authHeader = req.headers.authorization;
	if (!authHeader || !authHeader.startsWith('Bearer ')) {
		return res.status(401).json({ error: 'No token provided' });
	}

	const { oldPassword, newPassword } = req.body;
	if (!oldPassword || !newPassword) {
		return res.status(400).json({ error: 'Old and new passwords are required' });
	}

	if (!isValidPassword(newPassword)) {
		return res.status(400).json({ error: 'New password must be at least 6 characters' });
	}

	const token = authHeader.substring(7);
	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT * FROM accounts WHERE username = ?', [decoded.username], async (err, row) => {
			if (err || !row) {
				return res.status(404).json({ error: 'Account not found' });
			}

			const isPasswordValid = await bcrypt.compare(oldPassword, row.password_hash);
			if (!isPasswordValid) {
				return res.status(401).json({ error: 'Old password is incorrect' });
			}

			const hashedPassword = await bcrypt.hash(newPassword, 10);
			db.run('UPDATE accounts SET password_hash = ? WHERE id = ?', [hashedPassword, row.id], (err) => {
				if (err) {
					return res.status(500).json({ error: 'Failed to change password' });
				}
				res.json({ success: true, message: 'Password changed successfully' });
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// ==================== FRIENDS ENDPOINTS ====================

// Get friends list
app.get('/api/friends', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	if (!token) {
		return res.status(401).json({ error: 'Token required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, user) => {
			if (err || !user) {
				return res.status(404).json({ error: 'User not found' });
			}

			db.all(`
				SELECT a.id, a.username, os.last_seen,
					   CASE WHEN os.last_seen > datetime('now', '-5 minutes') THEN 1 ELSE 0 END as is_online
				FROM friends f
				JOIN accounts a ON f.friend_id = a.id
				LEFT JOIN online_status os ON a.id = os.user_id
				WHERE f.user_id = ?
				ORDER BY is_online DESC, a.username ASC
			`, [user.id], (err, rows) => {
				if (err) {
					return res.status(500).json({ error: 'Failed to fetch friends' });
				}
				res.json({ friends: rows || [] });
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Get friend requests
app.get('/api/friends/requests', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	if (!token) {
		return res.status(401).json({ error: 'Token required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, user) => {
			if (err || !user) {
				return res.status(404).json({ error: 'User not found' });
			}

			// Incoming requests
			db.all(`
				SELECT fr.id, a.id as user_id, a.username, fr.created_at
				FROM friend_requests fr
				JOIN accounts a ON fr.from_user_id = a.id
				WHERE fr.to_user_id = ? AND fr.status = 'pending'
				ORDER BY fr.created_at DESC
			`, [user.id], (err, incomingRows) => {
				if (err) {
					return res.status(500).json({ error: 'Failed to fetch requests' });
				}

				// Outgoing requests
				db.all(`
					SELECT fr.id, a.id as user_id, a.username, fr.created_at
					FROM friend_requests fr
					JOIN accounts a ON fr.to_user_id = a.id
					WHERE fr.from_user_id = ? AND fr.status = 'pending'
					ORDER BY fr.created_at DESC
				`, [user.id], (err, outgoingRows) => {
					if (err) {
						return res.status(500).json({ error: 'Failed to fetch requests' });
					}

					res.json({
						incoming: incomingRows || [],
						outgoing: outgoingRows || []
					});
				});
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Send friend request
app.post('/api/friends/request', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	const { username: targetUsername } = req.body;

	if (!token || !targetUsername) {
		return res.status(400).json({ error: 'Token and username required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, fromUser) => {
			if (err || !fromUser) {
				return res.status(404).json({ error: 'Your account not found' });
			}

			db.get('SELECT id FROM accounts WHERE username = ?', [targetUsername], (err, toUser) => {
				if (err || !toUser) {
					return res.status(404).json({ error: 'Target user not found' });
				}

				if (fromUser.id === toUser.id) {
					return res.status(400).json({ error: 'Cannot add yourself' });
				}

				// Check if already friends
				db.get('SELECT id FROM friends WHERE user_id = ? AND friend_id = ?', 
					[fromUser.id, toUser.id], (err, friend) => {
					if (friend) {
						return res.status(409).json({ error: 'Already friends' });
					}

					// Check for existing request
					db.get('SELECT id FROM friend_requests WHERE from_user_id = ? AND to_user_id = ?',
						[fromUser.id, toUser.id], (err, request) => {
						if (request) {
							return res.status(409).json({ error: 'Friend request already sent' });
						}

						// Check if blocked
						db.get('SELECT id FROM blocked_users WHERE blocker_id = ? AND blocked_id = ?',
							[toUser.id, fromUser.id], (err, blocked) => {
							if (blocked) {
								return res.status(403).json({ error: 'You are blocked by this user' });
							}

							// Create request
							db.run('INSERT INTO friend_requests (from_user_id, to_user_id) VALUES (?, ?)',
								[fromUser.id, toUser.id], (err) => {
								if (err) {
									return res.status(500).json({ error: 'Failed to send request' });
								}
								res.json({ success: true, message: 'Friend request sent' });
							});
						});
					});
				});
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Accept friend request
app.post('/api/friends/accept', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	const { requestId } = req.body;

	if (!token || !requestId) {
		return res.status(400).json({ error: 'Token and requestId required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, user) => {
			if (err || !user) {
				return res.status(404).json({ error: 'User not found' });
			}

			db.get('SELECT * FROM friend_requests WHERE id = ? AND to_user_id = ?',
				[requestId, user.id], (err, request) => {
				if (!request) {
					return res.status(404).json({ error: 'Request not found' });
				}

				// Add both directions as friends
				db.run('INSERT INTO friends (user_id, friend_id) VALUES (?, ?)',
					[request.to_user_id, request.from_user_id], (err) => {
					if (err) {
						return res.status(500).json({ error: 'Failed to accept request' });
					}

					db.run('INSERT INTO friends (user_id, friend_id) VALUES (?, ?)',
						[request.from_user_id, request.to_user_id], (err) => {
						if (err) {
							return res.status(500).json({ error: 'Failed to accept request' });
						}

						db.run('DELETE FROM friend_requests WHERE id = ?', [requestId], (err) => {
							if (err) {
								return res.status(500).json({ error: 'Failed to delete request' });
							}
							res.json({ success: true, message: 'Friend request accepted' });
						});
					});
				});
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Reject friend request
app.post('/api/friends/reject', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	const { requestId } = req.body;

	if (!token || !requestId) {
		return res.status(400).json({ error: 'Token and requestId required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, user) => {
			if (err || !user) {
				return res.status(404).json({ error: 'User not found' });
			}

			db.run('DELETE FROM friend_requests WHERE id = ? AND to_user_id = ?',
				[requestId, user.id], (err) => {
				if (err) {
					return res.status(500).json({ error: 'Failed to reject request' });
				}
				res.json({ success: true, message: 'Friend request rejected' });
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Remove friend
app.delete('/api/friends/:friendId', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	const { friendId } = req.params;

	if (!token || !friendId) {
		return res.status(400).json({ error: 'Token and friendId required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, user) => {
			if (err || !user) {
				return res.status(404).json({ error: 'User not found' });
			}

			// Remove both directions
			db.run('DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)',
				[user.id, friendId, friendId, user.id], (err) => {
				if (err) {
					return res.status(500).json({ error: 'Failed to remove friend' });
				}
				res.json({ success: true, message: 'Friend removed' });
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Block user
app.post('/api/friends/block', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	const { username: targetUsername } = req.body;

	if (!token || !targetUsername) {
		return res.status(400).json({ error: 'Token and username required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, blocker) => {
			if (err || !blocker) {
				return res.status(404).json({ error: 'Your account not found' });
			}

			db.get('SELECT id FROM accounts WHERE username = ?', [targetUsername], (err, blocked) => {
				if (err || !blocked) {
					return res.status(404).json({ error: 'Target user not found' });
				}

				if (blocker.id === blocked.id) {
					return res.status(400).json({ error: 'Cannot block yourself' });
				}

				db.run('INSERT OR IGNORE INTO blocked_users (blocker_id, blocked_id) VALUES (?, ?)',
					[blocker.id, blocked.id], (err) => {
					if (err) {
						return res.status(500).json({ error: 'Failed to block user' });
					}

					// Remove friend relationship if exists
					db.run('DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)',
						[blocker.id, blocked.id, blocked.id, blocker.id], (err) => {
						res.json({ success: true, message: 'User blocked' });
					});
				});
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Search users
app.get('/api/friends/search/:query', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');
	const { query } = req.params;

	if (!token || !query || query.length < 2) {
		return res.status(400).json({ error: 'Token and search query (min 2 chars) required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, user) => {
			if (err || !user) {
				return res.status(404).json({ error: 'User not found' });
			}

			db.all(`
				SELECT a.id, a.username, 
					   CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END as is_friend,
					   CASE WHEN b.id IS NOT NULL THEN 1 ELSE 0 END as is_blocked,
					   CASE WHEN fr.id IS NOT NULL THEN 1 ELSE 0 END as has_pending_request
				FROM accounts a
				LEFT JOIN friends f ON (f.user_id = ? AND f.friend_id = a.id)
				LEFT JOIN blocked_users b ON (b.blocker_id = ? AND b.blocked_id = a.id)
				LEFT JOIN friend_requests fr ON (fr.from_user_id = ? AND fr.to_user_id = a.id AND fr.status = 'pending')
				WHERE a.username LIKE ? AND a.id != ?
				LIMIT 10
			`, [user.id, user.id, user.id, `%${query}%`, user.id], (err, rows) => {
				if (err) {
					return res.status(500).json({ error: 'Search failed' });
				}
				res.json({ results: rows || [] });
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Update online status
app.post('/api/status/online', (req, res) => {
	const token = req.headers.authorization?.replace('Bearer ', '');

	if (!token) {
		return res.status(401).json({ error: 'Token required' });
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET);
		db.get('SELECT id FROM accounts WHERE username = ?', [decoded.username], (err, user) => {
			if (err || !user) {
				return res.status(404).json({ error: 'User not found' });
			}

			db.run('INSERT OR REPLACE INTO online_status (user_id, last_seen) VALUES (?, datetime(\'now\'))',
				[user.id], (err) => {
				if (err) {
					return res.status(500).json({ error: 'Failed to update status' });
				}
				res.json({ success: true, message: 'Status updated' });
			});
		});
	} catch (err) {
		res.status(401).json({ error: 'Invalid token' });
	}
});

// Error handling middleware
app.use((err, req, res, next) => {
	console.error('Unhandled error:', err);
	res.status(500).json({ error: 'Internal server error' });
});

// Serve index.html for non-API routes (SPA fallback)
app.use((req, res) => {
	res.sendFile(path.join(__dirname, 'public', 'index.html'), (err) => {
		if (err) {
			res.status(404).json({ error: 'Not found' });
		}
	});
});

// Start server
app.listen(PORT, () => {
	console.log(`Eaglercraft Accounts Backend running on http://localhost:${PORT}`);
	console.log('API Documentation:');
	console.log('  POST /api/auth/signup                    - Create new account');
	console.log('  POST /api/auth/login                     - Login to account');
	console.log('  POST /api/auth/verify                    - Verify token');
	console.log('  GET  /api/account                        - Get account info (requires token)');
	console.log('  POST /api/account/change-password        - Change password (requires token)');
	console.log('  GET  /api/friends                        - Get friends list (requires token)');
	console.log('  GET  /api/friends/requests               - Get friend requests (requires token)');
	console.log('  POST /api/friends/request                - Send friend request (requires token)');
	console.log('  POST /api/friends/accept                 - Accept friend request (requires token)');
	console.log('  POST /api/friends/reject                 - Reject friend request (requires token)');
	console.log('  DELETE /api/friends/:friendId            - Remove friend (requires token)');
	console.log('  POST /api/friends/block                  - Block user (requires token)');
	console.log('  GET  /api/friends/search/:query          - Search users (requires token)');
	console.log('  POST /api/status/online                  - Update online status (requires token)');
	console.log('  GET  /api/health                         - Health check');
});

// Graceful shutdown
process.on('SIGINT', () => {
	db.close((err) => {
		if (err) {
			console.error('Error closing database:', err);
		} else {
			console.log('Database connection closed');
		}
		process.exit(0);
	});
});
