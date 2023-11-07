DROP DATABASE IF EXISTS books; -- 만약 market_db가 존재하면 우선 삭제한다.
CREATE DATABASE books;

use books;

select * from best;
select * from publisher;
select * from book;
select * from inventory;

use books;
 SELECT id, itemId, stockStatus FROM inventory;
 SELECT publisher, COUNT(*) as count FROM book GROUP BY publisher;
 
 SET SQL_SAFE_UPDATES = 0;
 SET SQL_SAFE_UPDATES = 1;
 
 UPDATE inventory
SET stockStatus = FLOOR(100 + RAND() * 101);

UPDATE inventory_history
SET stockStatus = CONCAT(FLOOR(RAND() * 201))
WHERE date = '2023-11-05';



CREATE TABLE inventory_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    itemId VARCHAR(255),
    date DATE,
    stockStatus VARCHAR(255)
);

select * from inventory_history;

SELECT *
FROM inventory_history
WHERE date = '2023-11-06';

truncate table best;

drop table best;
drop table publisher;
drop table book;

insert into inventory (id, publisher, title, link, author, pubdate, isbn, isbn13, itemid, pricesales, pricestandard, categoryid, categoryname, stockstatus, cover)
select id, publisher, title, link, author, pubdate, isbn, isbn13, itemid, pricesales, pricestandard, categoryid, categoryname, stockstatus, cover from book;