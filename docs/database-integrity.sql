-- PostgreSQL: Uygulamayi durdurun, yedek alin ve once bu sorgulari inceleyin.
SELECT email, count(*) FROM users GROUP BY email HAVING count(*) > 1;
SELECT pnr_kodu, count(*) FROM tickets GROUP BY pnr_kodu HAVING count(*) > 1;
SELECT seat_id, count(*) FROM tickets GROUP BY seat_id HAVING count(*) > 1;
SELECT flight_id, seat_number, count(*) FROM seats GROUP BY flight_id, seat_number HAVING count(*) > 1;
SELECT user_id FROM users WHERE email IS NULL;
SELECT ticket_id FROM tickets WHERE pnr_kodu IS NULL OR seat_id IS NULL;

-- Yukaridaki sorunlari veri kaybetmeden cozdükten sonra asagidaki blogu calistirin.
-- Kopya veya NULL kayit varsa islem hata verip geri alinir; otomatik silme yapilmaz.
BEGIN;
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tickets_pnr ON tickets(pnr_kodu);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tickets_seat ON tickets(seat_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_seat_flight_number ON seats(flight_id, seat_number);
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE tickets ALTER COLUMN pnr_kodu SET NOT NULL;
ALTER TABLE tickets ALTER COLUMN seat_id SET NOT NULL;
COMMIT;
