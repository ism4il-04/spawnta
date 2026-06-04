# Google Stitch UI Design Prompt — Spawnta

---

## App Overview

Design the complete UI for **Spawnta**, a mobile-first social activity app that lets people discover and join spontaneous outdoor adventures and local meetups near them. Think of it as a cross between Meetup and a travel companion — users create trips (hikes, road trips, café meetups, beach days), others discover and join them on a real-time map, and everyone earns XP and level badges as they participate.

---

## Brand & Visual Identity

- **App Name:** Spawnta
- **Tagline:** *"Discover spontaneous adventures near you"*
- **Personality:** Adventurous, warm, energetic, community-driven, youthful but trustworthy
- **Color Palette:**
  - Primary: Deep teal / forest green (#1B6E5F or similar) — adventure, nature
  - Accent: Warm amber / sunset orange (#F5A623) — energy, spontaneity
  - Background (Light): Off-white (#F8F9FA)
  - Background (Dark): Deep charcoal (#121212)
  - Text: Near-black (#1A1A2E)
  - Surface cards: White (#FFFFFF) with subtle shadow
  - Danger/Error: Soft red (#E53935)
  - Success: Emerald green (#27AE60)
- **Typography:** Rounded, modern sans-serif (e.g., Inter or Nunito) — headings bold, body regular
- **Corner Radius:** Generous rounded corners (16–24px for cards, 50px for buttons/pills)
- **Icons:** Outlined style (Phosphor or similar), with filled state for active/selected
- **Elevation:** Subtle card shadows, no harsh borders
- **Theme:** Support both Light and Dark modes

---

## Platform

Mobile app (iOS/Android) — primary. All screens should be designed at 390×844px (iPhone 14 standard). The design should feel native and fluid.

---

## Screens to Design

### 1. Splash / Welcome Screen
- Full-screen illustration or gradient background (teal to amber gradient, or a scenic nature-inspired artwork)
- Spawnta logo centered with tagline below
- Two large pill buttons: **"Sign Up"** and **"Log In"**
- Small "Continue with Google" and "Continue with Facebook" options below

---

### 2. Sign Up — Step 1 (Basic Credentials)
- Progress indicator at top showing Step 1 of 3
- Heading: "Create your account"
- Form fields with floating labels:
  - Full Name
  - Email Address
  - Password (with show/hide toggle)
  - Confirm Password
- Primary CTA button: "Continue"
- Divider: "or"
- Social OAuth buttons: Google, Facebook (icon + label)
- Footer: "Already have an account? Log In" (link)

---

### 3. Sign Up — Email Verification Screen
- Illustration of an envelope with a checkmark
- Heading: "Check your email"
- Subtext: "We sent a verification link to [email]. Click it to continue."
- "Resend Email" ghost button
- "Open Email App" primary button

---

### 4. Sign Up — Step 2 (Profile Basics)
- Progress indicator: Step 2 of 3
- Heading: "Set up your profile"
- Circular avatar upload area with camera icon overlay (center of screen)
- Form fields:
  - Username (with @ prefix, real-time availability check — shows green checkmark or red X)
  - Date of Birth (date picker)
  - City / Region (location input with map pin icon, "Detect automatically" option)
- Primary CTA: "Continue"

---

### 5. Sign Up — Step 3 (Interests Selection)
- Progress indicator: Step 3 of 3
- Heading: "What are you into?"
- Subtext: "Pick 3 to 10 interests"
- Counter badge: "3 / 10 selected"
- 4-column grid of interest tiles, each showing:
  - Relevant emoji or icon
  - Interest label (e.g., "Hiking", "Beach", "Cycling")
  - Selected state: filled teal background, white text, checkmark
  - Unselected state: white card, gray text
- Sample interests: Hiking & Trekking, Coffee & Cafés, Road Trips, Beach & Swimming, Cultural Visits, Photography, Cycling, Nightlife, Food & Restaurants, Camping, Fitness & Sports, Volunteering, Language Exchange, Reading / Book Clubs, Board Games
- Primary CTA (disabled until 3 selected): "Finish & Enter Spawnta"

---

### 6. Log In Screen
- Heading: "Welcome back"
- Form fields:
  - Email Address
  - Password (show/hide toggle)
- "Forgot Password?" text link (right-aligned under password field)
- "Remember me" checkbox
- Primary CTA: "Log In"
- Divider: "or"
- Social OAuth buttons: Google, Facebook
- Footer: "Don't have an account? Sign Up"

---

### 7. Forgot Password Screen
- Back arrow in top-left
- Heading: "Reset your password"
- Subtext: "Enter your email and we'll send you a reset link."
- Email field
- Primary CTA: "Send Reset Link"
- Confirmation state: success illustration + "Check your inbox" message

---

### 8. Reset Password Screen
- Heading: "Set a new password"
- Fields: New Password, Confirm New Password (both with show/hide)
- Primary CTA: "Update Password"

---

### 9. Home — Map View (Main Screen)
- Full-screen interactive map (Google Maps style, slightly desaturated to not compete with pins)
- Bottom navigation bar with 5 tabs: Map (active), Feed, ➕ Create, Messages, Profile
- Top bar with:
  - Search bar (expandable)
  - Notification bell with unread badge
- Floating category filter pills scrollable horizontally at top of map (All, Hiking, Coffee, Beach, Road Trip, etc.)
- Activity pins on map — each pin is a small circular avatar of the host overlaid on an icon badge by category type
- "Near Me" floating button (bottom-right, above nav bar)
- **Activity Preview Card** (bottom sheet, appears on pin tap):
  - Host profile picture (small circle, left)
  - Activity title (bold)
  - Date + time chip
  - Participant count: avatars of first 3 joiners + "[N] joined"
  - Distance chip
  - "See Details" primary button

---

### 10. Home — Feed View
- Top bar: same as Map view
- Feed filter bar: scrollable horizontal chips (Category, Date, Distance, Join Mode, Availability)
- Activity cards in a vertical scroll list. Each card:
  - Cover image (full-width, 180px tall, with gradient overlay at bottom)
  - Category badge (pill, top-left of image)
  - Join Mode badge (top-right): "Open" (green) or "Request" (amber)
  - Premium highlight: subtle gold border or glow (for Premium activities)
  - Host avatar + name + level badge (small row below image)
  - Activity title (bold, large)
  - Date / Time and Location name (icon + text)
  - Distance chip (pin icon + "3.2 km away")
  - Participant avatars row + "[4 / 8] joined" counter
- "Suggested for You" horizontal scroll section at top of feed with explanation chips ("Because you like Hiking")

---

### 11. Activity Detail Page
- Back arrow at top-left
- Large cover photo hero (full-width, ~260px tall) with gradient fade at bottom
- Floating "Share" icon (top-right)
- Scrollable content below hero:
  - Activity title (H1, bold)
  - Category tag pills
  - Host row: circular avatar, name, level badge, "View Profile" link
  - Date/time row with calendar icon
  - Duration row (if set)
  - Participant count with small avatars
  - Description text block
  - Section: "Meeting Point" — mini embedded map with teal pin, address label
  - Section: "Destination" (if Trip type) — same mini map style
  - Participant list: horizontal row of avatars with names
- Sticky bottom bar:
  - Remaining spots text ("4 spots left")
  - Primary CTA button: "Join Activity" or "Request to Join"
  - After joining: "Open Trip Hub" button (teal filled)
  - Pending state: "Request Pending" disabled gray button

---

### 12. Request to Join Modal
- Bottom sheet overlay
- Heading: "Send a message to the host (optional)"
- Subtext: "Introduce yourself or explain why you'd like to join."
- Multiline text input (max 150 chars) with character counter
- Two buttons: "Skip" (ghost) and "Send Request" (primary)

---

### 13. Trip Hub — Group Chat Tab
- Top bar: Activity title + "Hub" label, back arrow, info icon
- Three tabs: Chat (active), Media, Info
- Chat area:
  - Bubble messages — outgoing right-aligned (teal), incoming left-aligned (white card)
  - Emoji reaction pills below messages
  - System messages centered (gray pill): "Aymane joined the trip"
  - Typing indicator (animated dots)
  - Image messages: rounded image thumbnail inline
- Bottom input bar: text input, emoji button, image attach button, send button

---

### 14. Trip Hub — Media Tab
- 3-column grid of uploaded photos/videos (with play icon overlay for videos)
- Caption text below each item
- Floating "+" upload button (bottom-right)
- Tapping an item opens a full-screen lightbox viewer with download button

---

### 15. Trip Hub — Info Tab
- Read-only card layout showing:
  - Activity title + description
  - Meeting point + destination mini maps
  - Date, time, estimated duration
  - Host info row

---

### 16. Create Activity — Step 1 (Basic Info)
- Back arrow, heading "Create Activity"
- Progress bar (step 1/4)
- Form:
  - Activity Title field (80 char max, counter)
  - Description text area (1000 char max, counter)
  - Activity Type toggle: "Local Meetup" | "Trip / Excursion"
  - Cover Picture upload (dashed border rectangle with camera icon + "Add cover photo")
  - Category/Tags multi-select chips (1 to 3)
- "Continue" CTA

---

### 17. Create Activity — Step 2 (Date & Time)
- Progress bar (step 2/4)
- Date picker (calendar style)
- Start Time picker (scroll wheel or clock)
- Estimated End Time picker (optional toggle)
- "Continue" CTA

---

### 18. Create Activity — Step 3 (Location)
- Progress bar (step 3/4)
- Full-width map with draggable pin
- Search bar above map: "Search a place or address"
- Place name text input below map
- If Trip type: second map section for destination + auto-drawn route line between the two pins
- "Continue" CTA

---

### 19. Create Activity — Step 4 (Participation Settings & Review)
- Progress bar (step 4/4)
- Join Mode toggle row: "Direct Join" / "Request to Join" (with short description of each)
- Capacity row: "Unlimited" toggle + number input (appears when unlimited is off)
- Summary card showing all previously entered details (title, date, location, type, tags)
- Two CTAs: "Publish Activity" (primary) and "Save as Draft" (ghost)

---

### 20. Post-Trip — Attendance Confirmation (Host View)
- Header: "Confirm who showed up"
- Subheader: activity title and date
- Scrollable list of approved participants:
  - Avatar, name, level badge
  - Toggle: "Present ✓" (green) / "Absent ✗" (red)
  - Default: all toggled to Present
- "Confirm Attendance" primary CTA

---

### 21. Post-Trip — Rate Experience (Participant View)
- Header: "How was it?"
- Activity title
- Section 1: Overall Trip Rating — 5 large tappable stars + optional text review (300 chars)
- Section 2: Rate Your Fellow Explorers (one card per participant):
  - Avatar + name
  - 5-star rating
  - Quick tag chips: Friendly, Punctual, Fun, Respectful, Communicative
  - Optional comment (200 chars)
- "Submit Ratings" CTA

---

### 22. Level Up Screen
- Full-screen celebration animation (confetti, subtle fireworks)
- New level badge (large, centered, glowing)
- "Level [N]: [Title]" (e.g., "Level 5: Trail Explorer")
- XP gained: "+120 XP"
- "Continue" button

---

### 23. Profile — Own Profile
- Top section:
  - Cover/header background (gradient or abstract pattern)
  - Profile picture (large circle, overlapping the header)
  - Name (bold H1) + username (@handle)
  - Level badge pill (e.g., "Level 4 · Trailblazer")
  - XP progress bar with label ("480 / 600 XP to Level 5")
  - "Edit Profile" button (outlined)
- Stats row: Friends count, Activities Hosted, Activities Joined
- Interests: scrollable horizontal pill chips
- Bio text block
- Countries Visited: world map graphic or flag chips
- Tabs below: Upcoming | Hosting | Past | Drafts
  - Activity cards in selected tab (compact style)
- Social links row at bottom (Facebook, Instagram, WhatsApp icons)

---

### 24. Profile — Edit Profile
- Top bar: "Edit Profile" title, "Save Changes" button (disabled if no changes)
- Same sections as profile but all fields are editable:
  - Profile picture edit (tap to upload)
  - Full name, Username, Bio (character counter)
  - Interests multi-select grid
  - Countries Visited picker
  - Photo gallery upload grid (tap + to add)
  - Social links inputs

---

### 25. Profile — Other User's Profile
- Same layout as Own Profile but:
  - No "Edit Profile" — replaced with "Add Friend" (or "Message" if already friends)
  - No Drafts tab
  - Fields shown/hidden based on that user's privacy settings
  - Ratings summary section: average star rating + total reviews

---

### 26. Search Screen
- Search bar (expanded, focused, with keyboard)
- Recent searches section
- Results segmented into tabs: Activities | People | Places
- People result cards: avatar, name, level badge, mutual friends count, "Add Friend" button
- Activity result cards: compact feed card style

---

### 27. Messages Tab
- List of conversations, sorted by most recent:
  - Avatar, name, last message preview, timestamp, unread badge
  - Group trip conversations have a group icon overlay
- Tapping opens the 1-on-1 chat screen (same design as Trip Hub group chat)
- Floating "New Message" FAB

---

### 28. Friends Section (within Profile or dedicated screen)
- "People You May Know" horizontal scroll row (AI suggestions):
  - Card: avatar, name, reason chip ("3 common interests"), "Add Friend" button
- "Friend Requests" section (if any): avatar, name, mutual count, "Accept" / "Decline" buttons
- Friends list: searchable, sorted alphabetically or by recent activity
  - Each row: avatar, name, level badge, "Message" icon button

---

### 29. Notifications Center
- Screen title: "Notifications"
- Tabs: All | Activity | Social | System
- Notification rows:
  - Avatar of person involved (or activity icon for system)
  - Bold notification message
  - Relative timestamp
  - Unread state: slight teal background tint
  - Action buttons inline where applicable ("Accept" / "Decline" for requests)

---

### 30. Settings Screen
- Section list (grouped):
  - **Account:** Change email, Change password, Linked accounts, Delete account
  - **Profile:** Shortcut to Edit Profile
  - **Privacy:** Field visibility toggles, activity privacy toggles
  - **Notifications:** Per-category push notification toggles
  - **Subscription:** Current plan badge, "Upgrade to Premium" CTA
  - **Appearance:** Light / Dark / System theme selector
  - **Language:** Language selector
  - **Help & Support:** FAQ, Contact support, Report a bug
  - **About:** App version, Terms of Service, Privacy Policy
  - **Log Out:** Red text, confirmation dialog

---

### 31. Subscription / Plans Screen
- Heading: "Go Premium"
- Side-by-side comparison table: Free vs. Premium
- Features rows with checkmarks and X's
- Billing toggle: "Monthly" | "Annual" (annual shows "Save 30%" badge)
- Price display (large, bold) with billing period
- Payment method icons: Card, PayPal, Apple Pay, Google Pay
- "Start Premium" large primary CTA
- "No thanks" ghost link below

---

### 32. Privacy Settings Screen
- Heading: "Privacy"
- Grouped toggle rows:
  - Bio visibility (Public / Friends Only / Private)
  - Photo Gallery visibility
  - Countries Visited visibility
  - Social Links visibility
  - "Show me in participant lists" toggle
  - "Show my past trips on profile" toggle
- Each row has: label, optional description, and a segmented control or toggle

---

## General UI Patterns to Apply Everywhere

- **Empty states:** Custom illustrations (nature/adventure themed) with friendly message and a CTA
- **Loading states:** Skeleton screens (not spinners) for feed and profile content
- **Error states:** Inline field validation with red text + icon; toast notifications for API errors
- **Confirmations:** Bottom sheet dialogs (not full-screen modals) for destructive actions (Leave activity, Remove friend, Delete account)
- **Success feedback:** Brief green toast snackbar at bottom
- **Badges & Chips:** Level badges as small colored pills next to names everywhere they appear
- **Animations:** Smooth spring transitions between screens; micro-animations on likes, joins, and level ups

---

## Navigation Structure Summary

```
Bottom Nav:
├── Map (Map View)
├── Feed (Activity Feed)
├── + Create (Activity Creation Flow)
├── Messages (Conversations List)
└── Profile
    ├── Edit Profile
    ├── Friends
    └── Settings
        ├── Privacy
        ├── Notifications
        └── Subscription
```

---

*Generate all screens above with consistent design tokens, a complete component library (buttons, inputs, cards, chips, navigation), and both light and dark mode variants where applicable.*
