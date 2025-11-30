
# Sprint 2 Planning Meeting

## 📅 Date
November 3, 2025

## 🎯 Sprint Goal
The goal of this sprint is to enhance YUCircle with more advanced and engaging features that make the application fully usable by real students. This includes improving schedule functionality, enabling student-to-student interactions, and supporting key account management features such as password reset and notifications.

## 🧭 Scope / Key Features (Epics)
- Edit Schedule  
- Notifications  
- Password Reset  
- Send Message to Others  
- Find Other Students  
- Upload Schedule *(carried over from Sprint 1)*  

## 👥 Participants
- Jason Deng  
- David Oredina  
- Alice Luong  
- Ejaaj Ahmed  
- Brandon Cusato  

## 📋 Sprint 2 User Story Decisions
For Sprint 2, we chose to expand upon the foundational features delivered in Sprint 1. Since authentication, profiles, and basic posting are already completed, the next objective is to build features that make the platform meaningful for everyday student use.

A major priority in this sprint is the completion of the **Upload Schedule** story (8 story points), which was deferred from Sprint 1 due to its complexity. This story is critical to the core purpose of YUCircle—helping students find others on campus based on overlapping availability.

Beyond that, we included multiple high-value features such as editing schedules, finding other students, messaging, and notifications. These additions shift YUCircle from a basic prototype to a functional and interactive student networking app.

## 🛠️ Task Breakdown

### **Carryover Task from Sprint 1**
#### Upload Schedule
- Complete backend functionality to upload and parse schedule files  
- Store parsed schedule data in the database  
- Render schedules on the user profile  
- Make schedules searchable for availability matching  

---

### **New Sprint 2 Tasks**

#### Edit Schedule
- Create UI for modifying or replacing existing schedules  
- Build backend endpoints to update schedule entries  
- Ensure schedule changes are immediately reflected on profiles  

#### Notifications
- Implement backend triggers for key user actions  
- Build UI for notification display  
- Store notifications persistently  

#### Password Reset
- Build “Forgot Password” request flow  
- Generate and validate reset tokens  
- Send reset links via email  

#### Send Message to Others
- Create messaging database models  
- Build messaging API  
- Implement chat UI for conversations  

#### Find Other Students
- Implement searching by name, major, interests, or availability  
- Build UI for browsing/discovering other students  
- Integrate discovery with profile and schedule features  