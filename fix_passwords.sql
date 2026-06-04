UPDATE users SET password = '$2a$10$Xcfm25/HHlYbgHoCwRsmd.eOfYkL7FKh.ehQZuML9QyrHJrWcTELS' WHERE email = 'ayman.test@spawnta.com';
UPDATE users SET password = '$2a$10$Xcfm25/HHlYbgHoCwRsmd.eOfYkL7FKh.ehQZuML9QyrHJrWcTELS' WHERE email = 'admin@spawnta.com';
UPDATE users SET role = 'ADMIN' WHERE email IN ('ayman.test@spawnta.com', 'admin@spawnta.com');
