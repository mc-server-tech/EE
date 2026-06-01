# Eaglercraft Friends System

A complete friends system for Eaglercraft with client-side UI and backend API support.

## Features

### For Players

- **Add Friends** - Search for players by username and send friend requests
- **Manage Requests** - Accept or reject incoming friend requests
- **Friends List** - View all your friends with online/offline status
- **Remove Friends** - Unfriend players
- **Block Users** - Block unwanted players
- **Search Users** - Find players by username

### Backend Features

- Friend relationship management
- Friend request system with accept/reject
- Online status tracking
- User blocking
- User search with friend/block status

## Client-Side Implementation

### Java Classes

#### FriendsManager.java
Located in: `src/main/java/net/lax1dude/eaglercraft/profile/`

Manages friend data on the client:
- Stores friends list, requests, search results
- Provides data access methods
- In-memory storage (syncs with server as needed)

**Key Classes:**
- `Friend` - Represents a friend with username, ID, online status
- `FriendRequest` - Represents pending friend requests
- `SearchResult` - Represents user search results

**Usage:**
```java
List<Friend> friends = FriendsManager.getFriendsList();
int pendingCount = FriendsManager.getPendingRequestCount();
boolean isFriend = FriendsManager.isFriend("username");
```

#### GuiScreenFriends.java
Located in: `src/main/java/net/lax1dude/eaglercraft/profile/`

Main friends UI screen with three tabs:
1. **Friends** - List of current friends
2. **Requests** - Pending friend requests (incoming and outgoing)
3. **Search** - Search for users to add

Features:
- Tab-based interface
- Online/offline status indicators
- Action buttons for friend management
- Real-time search

#### GuiScreenAddFriend.java
Located in: `src/main/java/net/lax1dude/eaglercraft/profile/`

Simple screen for adding a friend by username:
- Username input field
- Validation
- Status messages
- Back button

### Integration Points

#### AccountManager.java
Added token management methods:
- `setCurrentToken(String token)` - Store JWT from backend
- `getCurrentToken()` - Retrieve stored token
- `hasToken()` - Check if token exists
- `clearToken()` - Clear token on logout

#### GuiMainMenu.java
Added "Friends" button:
- Button ID: 16
- Shows Friends screen if logged in
- Shows login screen if not logged in
- Located next to Account Login button

## Backend Implementation

### Database Schema

#### friends table
```sql
CREATE TABLE friends (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    friend_id INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES accounts(id),
    FOREIGN KEY(friend_id) REFERENCES accounts(id),
    UNIQUE(user_id, friend_id)
)
```

#### friend_requests table
```sql
CREATE TABLE friend_requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    from_user_id INTEGER NOT NULL,
    to_user_id INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TEXT DEFAULT 'pending',
    FOREIGN KEY(from_user_id) REFERENCES accounts(id),
    FOREIGN KEY(to_user_id) REFERENCES accounts(id),
    UNIQUE(from_user_id, to_user_id)
)
```

#### blocked_users table
```sql
CREATE TABLE blocked_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    blocker_id INTEGER NOT NULL,
    blocked_id INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(blocker_id) REFERENCES accounts(id),
    FOREIGN KEY(blocked_id) REFERENCES accounts(id),
    UNIQUE(blocker_id, blocked_id)
)
```

#### online_status table
```sql
CREATE TABLE online_status (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    last_seen DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES accounts(id)
)
```

### API Endpoints

#### Get Friends List
```
GET /api/friends
Authorization: Bearer <token>

Response:
{
  "friends": [
    {
      "id": 2,
      "username": "player2",
      "is_online": true,
      "last_seen": "2024-01-15 14:30:00"
    },
    {
      "id": 3,
      "username": "player3",
      "is_online": false,
      "last_seen": "2024-01-15 12:15:00"
    }
  ]
}
```

#### Get Friend Requests
```
GET /api/friends/requests
Authorization: Bearer <token>

Response:
{
  "incoming": [
    {
      "id": 1,
      "user_id": 4,
      "username": "newplayer",
      "created_at": "2024-01-15 10:00:00"
    }
  ],
  "outgoing": [
    {
      "id": 2,
      "user_id": 5,
      "username": "otherplayer",
      "created_at": "2024-01-15 09:30:00"
    }
  ]
}
```

#### Send Friend Request
```
POST /api/friends/request
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "targetplayer"
}

Response:
{
  "success": true,
  "message": "Friend request sent"
}
```

#### Accept Friend Request
```
POST /api/friends/accept
Authorization: Bearer <token>
Content-Type: application/json

{
  "requestId": 1
}

Response:
{
  "success": true,
  "message": "Friend request accepted"
}
```

#### Reject Friend Request
```
POST /api/friends/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "requestId": 1
}

Response:
{
  "success": true,
  "message": "Friend request rejected"
}
```

#### Remove Friend
```
DELETE /api/friends/:friendId
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "Friend removed"
}
```

#### Block User
```
POST /api/friends/block
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "targetplayer"
}

Response:
{
  "success": true,
  "message": "User blocked"
}
```

#### Search Users
```
GET /api/friends/search/:query
Authorization: Bearer <token>

Response:
{
  "results": [
    {
      "id": 1,
      "username": "player1",
      "is_friend": false,
      "is_blocked": false,
      "has_pending_request": false
    }
  ]
}
```

#### Update Online Status
```
POST /api/status/online
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "Status updated"
}
```

## How to Use

### For Players

1. **Login to Account**
   - Click "Account Login" on main menu
   - Create or login to your account

2. **Access Friends**
   - Click "Friends" button on main menu (requires login)
   - Or click "Friends" from account screen

3. **Add Friends**
   - Go to Search tab
   - Type a username (minimum 2 characters)
   - Press Enter to search
   - Click on a result to add as friend

4. **Manage Requests**
   - Go to Requests tab
   - View pending incoming/outgoing requests
   - Accept or reject incoming requests

5. **View Friends**
   - Go to Friends tab
   - See all friends with online status
   - Click to remove friend if needed

### For Developers

#### Connecting Client to Backend

1. **Store Token on Login/Signup**
   ```java
   // In GuiScreenAccountLogin or GuiScreenAccountSignup
   String token = responseData.getToken();
   AccountManager.setCurrentToken(token);
   ```

2. **Send API Requests**
   ```java
   String token = AccountManager.getCurrentToken();
   String url = "http://your-server.com:4000/api/friends";
   
   // Make HTTP request with token in Authorization header
   // Authorization: Bearer <token>
   ```

3. **Update Friend Data**
   ```java
   // Parse response from backend
   List<Friend> friends = parseFriendsFromJson(jsonResponse);
   FriendsManager.setFriendsList(friends);
   ```

## Configuration

### Backend Environment Variables

```env
# Port for backend server
PORT=4000

# JWT secret for token generation
JWT_SECRET=your-secret-key

# Database path
DB_PATH=./accounts.db
```

### Client Configuration

The client connects to the backend at the URL stored in `BACKEND_API_URL` environment variable:

```java
private static final String BACKEND_API_URL = System.getenv("EAGLERCRAFT_API_URL") != null 
    ? System.getenv("EAGLERCRAFT_API_URL") 
    : "http://localhost:4000/api";
```

## Security Considerations

1. **Tokens** - Always sent in `Authorization: Bearer <token>` header
2. **Validation** - All inputs validated on both client and server
3. **Relationships** - Friend relationships are bidirectional
4. **Blocking** - Blocked users cannot send friend requests
5. **Privacy** - Players can only see their own requests
6. **Rate Limiting** - Should be implemented for production (add in future)

## Future Enhancements

- [ ] In-game friend notifications
- [ ] Friend status messages
- [ ] Friend messaging system
- [ ] Clan/group system
- [ ] Friend ranking/tiers
- [ ] Favorite friends
- [ ] Friend activity feed
- [ ] Social achievements
- [ ] Rate limiting
- [ ] Moderation tools
- [ ] Report system
- [ ] Friend aliases (custom names for friends)

## Troubleshooting

### Friends button doesn't appear
- Make sure you're logged in
- Check that GuiMainMenu has been recompiled

### Can't add friends
- Check backend is running on port 4000
- Verify token is stored (check browser console)
- Make sure target username exists
- Check network console for API errors

### Friends list not updating
- Manually refresh by reopening Friends screen
- Check backend API response in network tab
- Verify token hasn't expired

### Online status always showing offline
- Players need to call `/api/status/online` on login
- Check that backend is tracking online status

## Files Modified/Created

### Created Files
- `src/main/java/net/lax1dude/eaglercraft/profile/FriendsManager.java`
- `src/main/java/net/lax1dude/eaglercraft/profile/GuiScreenFriends.java`
- `src/main/java/net/lax1dude/eaglercraft/profile/GuiScreenAddFriend.java`

### Modified Files
- `src/main/java/net/lax1dude/eaglercraft/profile/AccountManager.java` - Added token methods
- `src/game/java/net/minecraft/client/gui/GuiMainMenu.java` - Added Friends button

### Backend Files
- `server.js` - Added friends database schema and API endpoints

## Testing

### Manual Testing Checklist

- [ ] Can login/create account
- [ ] Friends button appears after login
- [ ] Can open Friends screen
- [ ] Can search for users
- [ ] Can send friend request
- [ ] Can accept/reject requests
- [ ] Can see friends list
- [ ] Can remove friends
- [ ] Online status updates correctly
- [ ] Can block users
- [ ] Cannot add blocked users
- [ ] Cannot add self
- [ ] Cannot add duplicate friend requests
