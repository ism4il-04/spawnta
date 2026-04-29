# Spawnta — Detailed Product Specifications

**Project:** Spawnta
**Team:** Ismail LYAMANI, Abdellatif OUMHELLA, Zakariyae EL ALLOUCHE, Mohammed Aymane Saber
**Level/Institution:** 4ème année Génie Informatique, ENSA Tétouan
**Module:** JEE (Java Enterprise Edition)

---

## Table of Contents

1. [Onboarding & Authentication](#1-onboarding--authentication)
2. [Profile Setup](#2-profile-setup)
3. [Home Screen & Navigation](#3-home-screen--navigation)
4. [Activity / Trip Creation](#4-activity--trip-creation)
5. [Discovering Activities (Map & Feed)](#5-discovering-activities-map--feed)
6. [Joining an Activity](#6-joining-an-activity)
7. [Trip Hub (Active Trip Experience)](#7-trip-hub-active-trip-experience)
8. [Post-Trip Flow](#8-post-trip-flow)
9. [Social Features — Friends & Messaging](#9-social-features--friends--messaging)
10. [Profile View (Own & Others)](#10-profile-view-own--others)
11. [Privacy Settings](#11-privacy-settings)
12. [AI Features](#12-ai-features)
13. [Subscription & Plans](#13-subscription--plans)
14. [Notifications](#14-notifications)
15. [Settings](#15-settings)

---

## 1. Onboarding & Authentication

### 1.1. Welcome / Splash Screen

When a new user opens Spawnta for the first time, they land on a welcome screen that presents two options:

- **Sign Up** — create a new account
- **Log In** — access an existing account

A short tagline summarizing the app's purpose is displayed (e.g., *"Discover spontaneous adventures near you"*).

---

### 1.2. Sign Up — Step by Step

Registration is broken into clear steps so it doesn't feel overwhelming.

#### Step 1 — Basic Credentials

| Field | Type | Details |
|---|---|---|
| Full Name | Text input | First and last name; displayed publicly |
| Email Address | Email input | Must be a valid email format; used for login and notifications |
| Password | Password input (masked) | Minimum 8 characters; must include at least one number |
| Confirm Password | Password input (masked) | Must match the Password field |

A **"Sign up with Google"** or **"Sign up with Facebook"** OAuth button is also available. Tapping it redirects to the respective provider's authentication page, then returns the user to Step 2 with the name and email pre-filled.

After submitting Step 1, the user receives a **verification email**. They must click the confirmation link before proceeding to Step 2.

---

#### Step 2 — Profile Basics (shown after email verification)

| Field | Type | Details |
|---|---|---|
| Username | Text input | Unique handle (e.g., `@aymane_travels`); no spaces; letters, numbers, and underscores only; checked for availability in real time |
| Date of Birth | Date picker | Used to verify the user is 13 or older; not displayed publicly |
| Profile Picture | Image upload | Optional at this stage; user can upload from device gallery or skip |
| City / Region | Location picker | The user's home base; used to show nearby activities; can be typed manually or detected automatically |

---

#### Step 3 — Interests Selection

The user is presented with a visual grid of interest categories. They must choose **between 3 and 10** interests to complete registration. Examples of available interests:

- Hiking & Trekking
- Coffee & Cafés
- Road Trips
- Beach & Swimming
- Cultural Visits (Museums, Ruins)
- Photography
- Cycling
- Nightlife
- Food & Restaurants
- Camping
- Fitness & Sports
- Volunteering
- Language Exchange
- Reading / Book Clubs
- Board Games

Each interest tile shows a relevant icon and a label. Selected tiles are visually highlighted. A counter shows how many are selected (e.g., *"5 / 10"*).

A **"Finish & Enter Spawnta"** button activates once at least 3 interests are chosen.

---

### 1.3. Log In

| Field | Type | Details |
|---|---|---|
| Email Address | Email input | The email used during registration |
| Password | Password input (masked) | The account's password |

Additional options on the login screen:

- **"Remember me"** checkbox — keeps the session active across app restarts
- **"Forgot Password?"** link — triggers a password-reset email to the entered address
- **"Log in with Google / Facebook"** — OAuth quick login

#### Forgot Password Flow

1. User taps "Forgot Password?" and enters their registered email.
2. A reset link is emailed to them.
3. Clicking the link opens a screen with two fields: **New Password** and **Confirm New Password**.
4. After submission, the password is updated and the user is redirected to the login screen.

---

## 2. Profile Setup

After initial registration, users can complete and enrich their profile at any time from the **Edit Profile** screen.

### 2.1. Edit Profile — All Fields

| Section | Field | Type | Details |
|---|---|---|---|
| Identity | Full Name | Text input | Publicly displayed |
| Identity | Username | Text input | Unique handle; changeable (limited to once every 30 days) |
| Identity | Profile Picture | Image upload | Cropped to a circle; recommended square image |
| About | Biography / Bio | Text area | Max 300 characters; supports line breaks; displayed on profile |
| About | Interests | Multi-select grid | 3 to 10 interests (Free plan); up to unlimited (Premium) |
| Travel | Countries Visited | Multi-select country picker | Searchable list of all countries; displayed as a world-map highlight or a text list |
| Gallery | Photo Gallery | Image upload | Add photos from past trips/activities; Free plan: up to 12 photos; Premium: unlimited |
| Social Links | Facebook URL | Text input | Paste a full profile URL |
| Social Links | Instagram URL | Text input | Paste a full profile URL |
| Social Links | WhatsApp Number | Phone number input | International format (e.g., +212 6XX XXX XXX); opens a WhatsApp chat when tapped by other users |

All changes are saved with a **"Save Changes"** button. Unsaved changes prompt a confirmation dialog if the user tries to leave the page.

---

## 3. Home Screen & Navigation

### 3.1. Navigation Bar

The bottom navigation bar (or side drawer on larger screens) contains:

| Tab | Description |
|---|---|
| 🗺️ Map | Interactive map showing nearby activities |
| 📋 Feed | Chronological/algorithmic list of activities |
| ➕ Create | Shortcut to create a new activity |
| 💬 Messages | Direct messages with friends |
| 👤 Profile | The user's own profile page |

### 3.2. Top Bar

- A **search icon** allowing the user to search for activities, users, or places.
- A **notification bell** showing unread notifications count.

---

## 4. Activity / Trip Creation

Accessed via the **"➕ Create"** button. The creation form is split into logical sections.

### 4.1. Basic Information

| Field | Type | Details |
|---|---|---|
| Activity Title | Text input | Max 80 characters; e.g., *"Sunset hike to Jbel Zemzem"* |
| Description | Text area | Max 1000 characters; the host explains the plan, what to bring, physical difficulty, etc. |
| Activity Type | Single-select | Choose one: **Local Meetup** (e.g., café, park) or **Trip / Excursion** (point A to point B) |
| Cover Picture | Image upload | Optional; displayed as the activity's visual thumbnail in the feed and map card |
| Category / Tags | Multi-select | Select 1 to 3 relevant interest tags (e.g., Hiking, Photography); helps with discoverability |

---

### 4.2. Date & Time

| Field | Type | Details |
|---|---|---|
| Date | Date picker | Cannot be in the past |
| Start Time | Time picker | When participants should arrive / the activity begins |
| Estimated End Time | Time picker | Optional but recommended; used for post-trip attendance flow |

---

### 4.3. Location

**If Activity Type = Local Meetup:**

| Field | Type | Details |
|---|---|---|
| Meeting Point | Map pin / Search box | The host taps a point on the map or types an address / place name; a pin is dropped |
| Place Name (optional) | Text input | Human-readable label (e.g., "Café Riad, Avenue Hassan II") |

**If Activity Type = Trip / Excursion:**

| Field | Type | Details |
|---|---|---|
| Start Location (Meeting Point) | Map pin / Search box | Where everyone meets before departing |
| Destination (Target Location) | Map pin / Search box | The final destination of the trip |
| Route Preview | Auto-generated map line | Once both points are set, a route is drawn on the map for the host to review |

---

### 4.4. Participation Settings

| Field | Type | Details |
|---|---|---|
| Join Mode | Toggle / Radio | **Direct Join:** anyone can join immediately; **Request to Join:** the host reviews and approves each request |
| Capacity | Number input or toggle | The host either sets a **maximum number of spots** (e.g., 8 people) or leaves it as **Unlimited** |

---

### 4.5. Confirmation & Publishing

A summary screen shows all the entered details. The host reviews everything and taps:

- **"Publish Activity"** — makes the activity visible on the map and feed immediately.
- **"Save as Draft"** — saves without publishing; accessible later from the host's profile.

---

## 5. Discovering Activities (Map & Feed)

### 5.1. Map View

The interactive map shows pins for nearby published activities. Each pin is color-coded or icon-coded by category (e.g., a coffee cup for café meetups, a mountain for hikes).

**Tapping a pin** opens a small preview card showing:
- Activity title
- Host's name and profile picture
- Date and start time
- Number of participants joined vs. capacity
- A "See Details" button

**Map Controls:**
- Zoom in / out
- Toggle between activity types (filter by category)
- "Near me" button to re-center the map on the user's current location

---

### 5.2. Feed View

A scrollable list of activities, sorted by default as **"Nearest & Soonest"**. Each activity card in the feed shows:

- Cover picture (if provided) or a category-based placeholder image
- Activity title
- Host's profile picture, name, and level badge
- Date, time, and meeting location name
- Distance from the user's current location (e.g., *"3.2 km away"*)
- Participant count (e.g., *"4 / 8 joined"*)
- Join Mode badge (*"Open"* or *"Request"*)
- Premium activities are visually highlighted (e.g., a subtle glow or badge)

**Feed Filters:**

Users can filter the feed using the following criteria:

| Filter | Options |
|---|---|
| Category | One or more interest tags |
| Date | Today / This Weekend / This Week / Custom range |
| Distance | Within 2 km / 5 km / 10 km / 20 km / Any |
| Join Mode | Direct Join only / Request only / Both |
| Availability | Show only activities with open spots |

---

## 6. Joining an Activity

### 6.1. Activity Detail Page

Tapping "See Details" (from pin or feed card) opens the full activity page, which contains:

- Large cover photo
- Title, description, and category tags
- Host info (clickable profile link)
- Date, time, and estimated duration
- Meeting point and/or destination shown on a mini embedded map
- Current participant list (profile pictures + names of people who have joined)
- Remaining spots indicator (if a capacity is set)

---

### 6.2. Direct Join

If the activity is set to **Direct Join**, a prominent **"Join Activity"** button is shown. Tapping it:

1. Adds the user to the participant list immediately.
2. Sends the user a confirmation notification.
3. Grants the user access to the **Trip Hub** (group chat + media space) for that activity.
4. The activity appears in the user's **"My Activities"** section under "Upcoming."

---

### 6.3. Request to Join

If the activity is set to **Request to Join**, the button reads **"Request to Join"**. Tapping it:

1. Sends the host a join request notification.
2. The button changes to *"Request Pending"* and becomes inactive.
3. The user can optionally include a short message to the host (max 150 characters) — e.g., *"Hi! I'm really into hiking, would love to join."*
4. The host receives the request in their notifications with the requester's profile and optional message.

**Host's view of a pending request:**
- The host sees the requester's name, profile picture, level, and mutual interests.
- The host taps **"Accept"** or **"Decline"**.
- The requester is notified of the decision.
- If accepted, the user is added to the Trip Hub.

---

### 6.4. Leaving an Activity

A joined participant can leave an activity before it starts via the activity detail page. A confirmation dialog is shown: *"Are you sure you want to leave this activity?"* Leaving removes them from the participant list and Trip Hub.

---

## 7. Trip Hub (Active Trip Experience)

Once a user joins an activity (and it is approved if applicable), they gain access to the **Trip Hub** — a dedicated space for that activity.

### 7.1. Group Chat

A real-time group messaging space accessible to all participants and the host.

| Feature | Details |
|---|---|
| Text Messages | Standard text input with send button |
| Emoji Reactions | React to any message with an emoji |
| Image Sharing | Participants can send photos directly in the chat |
| System Messages | Automatic messages appear when someone joins or leaves (e.g., *"Aymane joined the trip"*) |
| Read Receipts | Premium users see who has read their messages |
| Typing Indicator | Premium users see *"[Name] is typing…"* |

---

### 7.2. Shared Media Space

A gallery-style tab within the Trip Hub where participants can upload and view photos and videos from the trip.

| Feature | Details |
|---|---|
| Upload Photo | Select from device gallery or take a new photo |
| Upload Video | Select a video (max 2 minutes for Free, max 10 minutes for Premium) |
| View Gallery | Grid layout of all uploaded media |
| Download Media | Any participant can download media shared in the space |
| Caption | Optional short caption when uploading (max 100 characters) |

---

### 7.3. Trip Info Tab

A read-only view of the activity's details (title, description, meeting point, destination, date/time) so participants can refer back without leaving the hub.

---

## 8. Post-Trip Flow

After the activity's scheduled end time passes, the following sequence is triggered.

### 8.1. Host — Attendance Confirmation

The host receives a notification: *"Your trip '[Title]' has ended. Please confirm attendance."*

On the attendance screen, the host sees a list of all approved participants. Each participant has a toggle defaulting to **"Present ✓"**. The host manually toggles off anyone who did not show up, marking them as **"Absent ✗"**.

The host taps **"Confirm Attendance"** to submit.

---

### 8.2. Participant — Rating & Feedback

After the host confirms attendance, all participants marked as **"Present"** receive a notification to rate the experience.

#### Overall Trip Rating
The participant gives the trip an overall star rating from 1 to 5, plus an optional written review (max 300 characters).

#### Peer Ratings
For each other participant they interacted with, the user can leave:

| Field | Type | Details |
|---|---|---|
| Rating | 1–5 stars | Overall impression of the person |
| Tags (optional) | Multi-select | Quick positive descriptors: *Friendly*, *Punctual*, *Fun*, *Respectful*, *Communicative* |
| Written Feedback (optional) | Text area | Max 200 characters; open-ended comment |

Peer ratings are anonymous to preserve community trust. The aggregate score is reflected on each user's public profile.

---

### 8.3. XP & Level Up

After attendance is confirmed:

- **Host** earns XP based on: the number of participants who attended + the average rating received from participants.
- **Participants** earn XP based on: their attendance + the rating they received from peers.

An **XP gain animation** is shown on the user's profile screen (e.g., *"+120 XP!"*).

If the user crosses a level threshold, a **"Level Up!"** screen is shown with their new level badge and title (e.g., *Level 5: Trail Explorer*).

The leveling system rewards consistent, well-rated participation, making higher-level profiles more trustworthy in the community.

---

## 9. Social Features — Friends & Messaging

### 9.1. Finding Users

Users can search for others by **username** or **full name** using the search bar. The search results show:
- Profile picture
- Username and name
- Level badge
- Number of mutual friends (if any)
- A **"Add Friend"** or **"Message"** button

---

### 9.2. Friend Request Flow

**Sending a request:**
1. User visits another person's profile and taps **"Add Friend"**.
2. The button changes to *"Request Sent"* and becomes inactive.
3. The recipient receives a notification: *"[Name] sent you a friend request."*

**Receiving a request:**
- The recipient opens the notification or the Friends section.
- They see the requester's profile, level, and mutual friends.
- They tap **"Accept"** or **"Decline"**.
- If accepted, both users are added to each other's friends list and can now message each other.

**Removing a friend:**
- From the friend's profile, tap the three-dot menu and select **"Remove Friend"**. A confirmation dialog appears.

---

### 9.3. One-on-One Messaging

Only available between confirmed friends.

| Feature | Details |
|---|---|
| Text Messages | Standard text input |
| Emoji Reactions | React to any message |
| Image Sharing | Send photos from gallery or camera |
| Message Status | Sent ✓ / Delivered ✓✓ |
| Read Receipts | Premium: ✓✓ turns blue when read |
| Typing Indicator | Premium: *"[Name] is typing…"* |
| Message Search | Search through conversation history |

Conversations are listed in the **Messages tab**, sorted by most recent activity.

---

## 10. Profile View (Own & Others)

### 10.1. Own Profile

The user's own profile shows all their information as the public would see it (based on privacy settings), plus an **"Edit Profile"** button.

Additional sections visible only to the owner:

- **My Activities:** Tabs for *Upcoming*, *Hosting*, and *Past*.
- **My Drafts:** Saved but unpublished activity drafts.
- **XP & Level:** A progress bar showing current XP and how much is needed to reach the next level, plus the full level history.

---

### 10.2. Visiting Another User's Profile

When viewing someone else's profile, the visible sections depend on their privacy settings, but always include:

- Name, profile picture, level badge
- Interests
- Activity history (public trips only)
- Ratings summary (average star rating + total reviews received)
- An **"Add Friend"** button (if not already friends)
- A **"Message"** button (if already friends)
- Social links (if the user has made them public)

---

## 11. Privacy Settings

Accessible from **Settings → Privacy**.

### 11.1. Field Visibility

| Field | Always Public | User-Controlled (Public / Friends Only / Private) |
|---|---|---|
| Name | ✓ | |
| Profile Picture | ✓ | |
| Interests | ✓ | |
| Biography / Bio | | ✓ |
| Photo Gallery | | ✓ |
| Countries Visited | | ✓ |
| Social Links | | ✓ |

### 11.2. Activity Privacy

| Setting | Description |
|---|---|
| Show me in participant lists | Toggle; if off, the user appears as "Anonymous" in other users' activity participant lists |
| Show my past trips on profile | Toggle; if off, the "Past Activities" section is hidden from profile visitors |

---

## 12. AI Features

### 12.1. Smart Activity Suggestions

On the Feed and Map screens, a dedicated **"Suggested for You"** section shows activities ranked by the AI. The algorithm considers:

- The user's selected interests
- Categories of past activities they joined or hosted
- Their bio keywords
- Their current location and the distance to the activity
- The ratings and level of the host

Each suggestion card shows a subtle label like *"Because you like Hiking"* to explain why it was recommended.

---

### 12.2. Friend Recommendations

In the **Friends** section, a **"People You May Know"** row shows up to 5 suggested users. Each card shows:
- Profile picture, name, level
- The reason for the suggestion (e.g., *"3 common interests · 2 mutual friends"*)
- A direct **"Add Friend"** button

The algorithm considers: overlapping interests, common activity participation history, geographical proximity, and mutual friends.

---

## 13. Subscription & Plans

### 13.1. Plan Comparison Screen

Accessible from **Settings → Subscription**. Shows a side-by-side comparison of Free vs. Premium features.

| Feature | Free | Premium |
|---|---|---|
| View map & feed | ✓ | ✓ |
| Join public activities | ✓ | ✓ |
| Create activities per week | Up to 2 | Unlimited |
| Number of interests | Up to 10 | Unlimited |
| Photo gallery size | Up to 12 photos | Unlimited |
| Activity visibility on map/feed | Standard | Highlighted + priority placement |
| Typing indicators & read receipts | ✗ | ✓ |
| Custom chat themes | ✗ | ✓ |
| Ads | Shown | None |

### 13.2. Upgrading to Premium

Tapping **"Upgrade to Premium"** opens a payment screen with:
- Billing options: **Monthly** or **Annual** (annual shown with % savings)
- Payment methods: Credit/Debit Card, PayPal, Apple Pay / Google Pay
- A clear summary of what's included
- A **"Start Premium"** confirmation button

Upon successful payment, the Premium badge appears on the user's profile immediately.

---

## 14. Notifications

All notifications appear in the **Notification Center** (bell icon). Users can also receive push notifications on their device if permitted.

| Trigger | Notification Message |
|---|---|
| New friend request | *"[Name] sent you a friend request."* |
| Friend request accepted | *"[Name] accepted your friend request."* |
| Join request received (host) | *"[Name] wants to join your trip '[Title]'."* |
| Join request approved (participant) | *"Your request to join '[Title]' was approved!"* |
| Join request declined (participant) | *"Your request to join '[Title]' was not approved."* |
| Someone joins directly | *"[Name] joined your trip '[Title]'."* |
| New message | *"[Name]: [message preview]"* |
| New group chat message | *"[Name] in [Trip Title]: [message preview]"* |
| Trip starting soon | *"Reminder: '[Title]' starts in 1 hour."* |
| Attendance confirmation due | *"Your trip '[Title]' has ended. Please confirm attendance."* |
| Ratings available | *"Rate your experience on '[Title]' and your fellow participants."* |
| XP gained | *"You earned +[X] XP for '[Title]'!"* |
| Level up | *"Congratulations! You reached Level [N]: [Title]!"* |

Users can manage notification preferences per category under **Settings → Notifications**.

---

## 15. Settings

Accessible from the profile tab via the gear icon.

| Section | Options |
|---|---|
| Account | Change email, change password, linked social accounts, delete account |
| Profile | Shortcut to Edit Profile screen |
| Privacy | Field visibility toggles (see Section 11) |
| Notifications | Per-category push notification toggles |
| Subscription | View current plan, upgrade to Premium, manage billing |
| Language | Select app language |
| Appearance | Light / Dark / System default theme |
| Help & Support | FAQ, contact support form, report a bug |
| About | App version, terms of service, privacy policy |
| Log Out | Ends the current session with a confirmation dialog |

---

*End of Detailed Specifications — Spawnta*
