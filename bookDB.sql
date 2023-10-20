DROP DATABASE IF EXISTS books; -- 만약 market_db가 존재하면 우선 삭제한다.
CREATE DATABASE books;

use books;

select * from best;
select * from publisher;
select * from book;

truncate table best;

drop table best;
drop table publisher;
drop table book;


