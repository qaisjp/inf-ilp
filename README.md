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
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```
