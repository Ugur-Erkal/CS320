INSERT INTO User (Username, Password, UserType) VALUES
('ahmet123', '1234', 'Manager'),
('elif_g', 'abc123', 'Manager'),
('osmanK', 'pass', 'Manager'),
('zeynep', 'znp12', 'Customer'),
('murat_78', 'mrpass', 'Customer'),
('ayse1', 'aypass', 'Customer'),
('enes_34', 'enes34', 'Customer'),
('berrin', 'berr123', 'Customer');

INSERT INTO UserPhoneNumber (PhoneNumber) VALUES
('05551234567'), ('05332221100'), ('05447894512'),
('05079998811'), ('05331112233'), ('05050001112'), ('05325548877'), ('05313912414');

INSERT INTO HasPhoneNum (UserID, PhoneID) VALUES
(1,1), (2,2), (3,3), (4,4), (5,5), (6,6), (7,7), (8,8);

INSERT INTO UserAddress (Address, City) VALUES
('Beylikdüzü', 'İstanbul'), ('Bornova', 'İzmir'), ('Nilüfer', 'Bursa'),
('Üsküdar', 'İstanbul'), ('Çankaya', 'Ankara'), ('Üsküdar','İstanbul'),
('Kadıköy','İstanbul'), ('Ataşehir','İstanbul');

INSERT INTO Lives (UserID, AddressID) VALUES
(1,1), (2,2), (3,3), (4,4), (5,5), (6,6), (7,7), (8,8);

INSERT INTO Restaurant (RestaurantName, Address, City, CuisineType) VALUES
('Makarna Durağı', 'Kartal Mah. No:5', 'İstanbul', 'italyan'),
('Burger Kasabı', 'Ataşehir Bulv. No:12', 'İstanbul', 'amerikan'),
('Sushi Noktası', 'Kadıköy Cad. No:7', 'İstanbul', 'japon'),
('Vegan Cennet', 'Kıbrıs Sk. No:3', 'İzmir', 'vegan'),
('Kebapçı Hüso', 'Sanayi Mah. No:8', 'Ankara', 'türk');


INSERT INTO Manages (UserID, RestaurantID) VALUES
(1,1), (2,2), (1,3), (3,4), (3,5);

INSERT INTO Keyword (Keyword) VALUES
('makarna'), ('ızgara'), ('sushi'), ('sağlıklı'), ('kebap');

INSERT INTO AssociatedWith (RestaurantID, KeywordID) VALUES
(1,1), (2,2), (3,3), (4,4), (5,5);

INSERT INTO MenuItem (Description, Price, Name, Image) VALUES
('Spagetti', 45.00, 'Spagetti', 'https://i.ibb.co/G3dpFFsb/1.jpg'),
('etli burger', 65.00, 'Mega Burger', 'https://i.ibb.co/wZk0jFK9/2.jpg'),
('çiğ somon sarması', 55.00, 'Salmon Roll', 'https://i.ibb.co/sJvzgr2n/3.jpg'),
('tofulu bowl', 40.00, 'Tofu Bowl', 'https://i.ibb.co/0jwmkyj7/4.jpg'),
('adana dürüm', 48.00, 'Adana Dürüm', 'https://i.ibb.co/prhvRntd/5.jpg'),
('dört peynirli makarna', 52.00, '4 Peynirli makarna', 'https://i.ibb.co/5hXw6tRb/6.jpg'),
('tavuk burger', 50.00, 'Tavuk Burger', 'https://i.ibb.co/4wSBW7Fh/7.jpg'),
('ton balıklı sushi', 60.00, 'Tuna Sushi', 'https://i.ibb.co/xqpCLRCR/8.jpg'),
('avokado salata', 35.00, 'AvokadoMix', 'https://i.ibb.co/gLz7sHKM/9.jpg'),
('lahmacun', 25.00, 'Lahmacun', 'https://i.ibb.co/7tw6QnKH/10.jpg'),
('sebzeli wrap', 38.00, 'Vegan Wrap', 'https://i.ibb.co/3mdCvB2K/11.jpg'),
('pide', 42.00, 'Etli Pide', 'https://i.ibb.co/XBkkCq0/12.jpg'),
('şehriyeli pilav', 20.00, 'Pilav', 'https://i.ibb.co/ccdQkmDp/13.jpg'),
('yoğurtlu makarna', 32.00, 'Yoğurtlu Makarna', 'https://i.ibb.co/LDqfNkB9/14.jpg'),
('köri soslu tavuk', 46.00, 'Köri Tavuk', 'https://i.ibb.co/chCWYLgR/15.jpg');

INSERT INTO Has (RestaurantID, MenuItemID) VALUES
(1,1), (2,2), (3,3), (4,4), (5,5),
(1,6), (2,7), (3,8), (4,9), (5,10),
(4,11), (5,12), (1,13), (2,14), (3,15);

INSERT INTO Discount (Discount) VALUES
(10.00), (15.00), (5.00);

INSERT INTO Applied (DiscountID, MenuItemID, StartDate, EndDate) VALUES
(1,1,'2024-04-01','2024-04-10'),
(2,2,'2024-04-05','2024-04-20'),
(3,3,'2024-04-10','2024-04-30');

INSERT INTO Cart (Status, AcceptedAt) VALUES
('accepted', NOW()),
('accepted', NOW() - INTERVAL 1 HOUR),
('accepted', NOW() - INTERVAL 3 HOUR),
('accepted', NOW() - INTERVAL 25 HOUR),
('accepted', NOW() - INTERVAL 10 HOUR),
('sent', NULL),
('sent', NULL),
('sent', NULL),
('sent', NULL),
('sent', NULL);

INSERT INTO Belongs (CartID, UserID) VALUES
(1,4),(2,5),(3,6),(4,7),(5,4),(6,5),(7,6),(8,7),(9,4),(10,5);

INSERT INTO Holds (CartID, RestaurantID) VALUES
(1,1),(2,1),(3,2),(4,2),(5,3),(6,3),(7,4),(8,4),(9,5),(10,5);

INSERT INTO Contains (CartID, MenuItemID, Quantity) VALUES
(1,1,2),(1,6,1),(2,1,1),(3,2,1),(4,2,1),(5,3,2),(6,3,1),
(7,4,1),(8,4,2),(9,5,1),(10,5,2);

INSERT INTO Ratings (Rating, RatingDate, Comment, CartID) VALUES
(5, '2024-04-01', 'Çok iyidi', 1),
(4, '2024-04-02', 'güzeldi ama geç geldi', 2),
(5, '2024-04-02', 'efsane sos', 3),
(3, '2024-04-03', 'idare eder', 4),
(4, '2024-04-04', 'lezzetliydi', 5),
(2, '2024-04-04', 'soğuktu', NULL),
(5, '2024-04-05', 'ellerinize sağlık', NULL),
(1, '2024-04-06', 'berbat', NULL),
(3, '2024-04-07', 'fena değil', NULL),
(4, '2024-04-07', 'başarılı', NULL);

INSERT INTO WrittenBy (RatingID, UserID) VALUES
(1,4),(2,5),(3,6),(4,7),(5,4),(6,5),(7,6),(8,7),(9,4),(10,5);

INSERT INTO ForRestaurant (RatingID, RestaurantID) VALUES
(1,1),(2,1),(3,2),(4,2),(5,3),(6,3),(7,4),(8,4),(9,5),(10,5);