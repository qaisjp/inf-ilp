# inf-ilp

3rd year [Informatics Large Practical](http://www.drps.ed.ac.uk/18-19/dpt/cxinfr09051.htm) coursework during year 2017-2018.

| cw    | weighting | mark    | percent |
|-------|-----------|---------|---------|
| cw1   | 25% of cw | 23/25   | 92%     |
| cw2   | 75% of cw | 54.5/75 | 71.71%  |

- Total: 77.5% (A3)
- **Final (scaled) mark for ILP**: 78% (A3)

## Cloud Firestore Rules

```
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userID} {
      allow write: if request.auth.uid == userID; // only this user can write to this user
      allow read: if true; // anyone can read this user
      
      match /coinsIn/{document=**} {
      	allow create: if true; // anyone can send coins to this user (only create)
        allow read,write: if request.auth.uid == userID; // only this user can read/write coins sent
        
        // New project developers may want to narrow this down to:
        // - allow create: if request.auth.uid != userID;
        // - allow read,delete: if request.auth.uid == userID;
      }
      
      match /{document=**} {
      	allow read,write: if request.auth.uid == userID; // this user can read/write any subcollectoin
      }
    }

    match /{document=**} {
      allow read, write: if false; // default that nobody has access to anything
    }
  }
}
```
