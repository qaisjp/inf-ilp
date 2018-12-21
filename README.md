---
author:
  - name: Qais Patankar
  - uun: s1620208
course:
  - acronym: ilp
  - drps: http://www.drps.ed.ac.uk/18-19/dpt/cxinfr09051.htm
---

# inf-ilp
Informatics Large Practical

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

