# Geliştirme raporu — 14 Kasım 2026

## Tamamlananlar

- Bilet ve ödeme akışları aynı koltuk satırını pessimistic write kilidiyle alır. Eşzamanlı ikinci satış ilk işlem bitene kadar bekler ve koltuğun güncel durumunu kontrol eder.
- Doğrudan bilet oluşturma transaction kapsamına alındı; bilet kaydı ve koltuk durumu birlikte geri alınır. Koltuk güncelleme/silme de aynı kilitleme yolunu kullanır. Biletli koltuğun değiştirilmesi engellendi.
- E-posta, PNR, bilet başına koltuk ve uçuş içindeki koltuk numarası için benzersizlik tanımları eklendi.
- JWT sabit yedek anahtarı kaldırıldı. Eksik/geçersiz/zayıf anahtar başlangıçta reddedilir. Veritabanı ve iyzico gizli bilgileri ortam değişkenlerine taşındı.
- Otomatik yönetici oluşturma varsayılan olarak kapatıldı. Açılırsa e-posta ve en az 12 karakterlik şifre gerekir. Şifre konsola yazdırılmaz.
- Kayıt bulunamaması 404, kayıt/koltuk çakışması 409, yetki hatası 403 olarak dönüyor. Bozuk JSON veya hatalı yol parametresi 400 dönüyor. Veritabanı hata ayrıntıları istemciye gönderilmiyor.
- Türkçe/İngilizce mesajlar ve kurulum belgesi güncellendi. PostgreSQL için elle incelenip uygulanacak `database-integrity.sql` eklendi.

## Doğrulama

Java 17 ile Maven test paketi: 65 test; hata, başarısız veya atlanan test yok.
Test kapsamına iki eşzamanlı rezervasyonda tek satış, transaction rollback, e-posta/koltuk/PNR benzersizliği, başarılı/başarısız ödeme, gerçek tutarın veritabanından alınması, güvenli başlangıç ayarları ve HTTP hata kodları eklendi. Ödeme servisi taklit edildi; gerçek tahsilat yapılmadı. Veritabanı testleri H2 üzerinde çalıştı.

## Çalıştırmadan önce

1. README'deki zorunlu ortam değişkenlerini tanımlayın. JWT için en az 32 rastgele baytın Base64 karşılığını kullanın.
2. Önceden paylaşılmış anahtarları yenileyin. Koddaki değerleri kaldırmak Git geçmişindeki değerleri geçersiz kılmaz.
3. Önceden oluşturulmuş varsayılan yönetici hesabının şifresi otomatik değişmez; ayrıca değiştirin.
4. Mevcut PostgreSQL veritabanını yedekleyip geçiş SQL'ini inceleyin; kopya/NULL kayıtlar varsa veri kaybetmeden çözün. Bu çalışma gerçek veritabanına script çalıştırmadı.

## Sınırlar ve sonraki çalışma

- H2 testi geçti; PostgreSQL üzerinde gerçek eşzamanlılık/yük testi ayrıca gereklidir.
- Ödeme API çağrısı boyunca koltuk kilidi tutulur; yavaş yanıtlar beklemeyi uzatabilir.
- Veritabanı rollback'i dış ödeme servisini geri almaz. Tahsilattan sonra veritabanı hatası veya belirsiz ağ sonucu için kalıcı ödeme kaydı, mutabakat ve iade/idempotency akışı henüz yoktur. Canlı ödeme için bu çalışma gerekir.
- Mevcut altı karakterlik PNR çakışması artık veritabanında reddedilir; otomatik yeni PNR üretip yeniden deneme eklenmedi.
- Veritabanı e-posta benzersizliği mevcut tam metin değerine göredir; büyük/küçük harf normalizasyonu ayrı bir veri geçişi gerektirir.
