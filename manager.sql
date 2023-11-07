use books;

select * from inventory;
select * from inventory_history;

 SELECT id, itemId, stockStatus FROM inventory;
 SELECT publisher, COUNT(*) as count FROM book GROUP BY publisher;
 
 SELECT itemId, title, ISBN, stockStatus FROM inventory WHERE publisher;