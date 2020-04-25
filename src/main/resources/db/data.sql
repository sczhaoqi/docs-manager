select 1;
merge into USERS KEY(ID) values ( 1,'admin','123456',1);
merge into user_AUTHORITIES KEY(ID) values ( 1,1,1 );
merge into authorities key(id) values (1,'ADMIN','管理员');