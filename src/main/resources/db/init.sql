create table if not exists users(
  id int auto_increment primary key,
  username varchar(60),
  password varchar(100),
  enabled boolean default 1
);
create table if not exists AUTHORITIES(
  id int auto_increment primary key ,
  authority varchar(60),
  authority_details varchar(200)
);

create table if not exists user_AUTHORITIES(
    id int auto_increment primary key,
    aid int,
    uid int
);
create table if not exists docs(
  id int auto_increment primary key ,
  project varchar(500),
  zpath varchar(500),
  path varchar(500),
  href_subfix varchar(500),
  utime timestamp,
  name varchar(100)
);