package com.flightapp.flightticketsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * Uygulamanın çoklu dil (i18n) desteğini yapılandıran konfigürasyon sınıfıdır.
 * Gelen HTTP isteklerine göre sistemin hangi dilde yanıt vereceğini (Locale) çözer.
 */
@Configuration // Spring Boot'a bu sınıfın genel yapılandırma ve Bean tanımları içerdiğini bildirir.
public class LocaleConfig {

    /**
     * İstemcinin talep ettiği dili HTTP Header üzerinden tespit eden
     * ve geçerli dili belirleyen LocaleResolver bileşenini Spring Context'e kaydeder.
     *
     * @return Yapılandırılmış AcceptHeaderLocaleResolver nesnesi.
     */
    @Bean // Üretilen LocaleResolver nesnesini Spring IoC konteynerine bir Bean olarak tanıtır.
    public LocaleResolver localeResolver() {
        // İstemciden gelen HTTP 'Accept-Language' başlığını (header) okuyan çözümleyici nesnesi oluşturulur.
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();

        // İstemci herhangi bir dil başlığı göndermezse veya belirsiz bir dil isterse
        // sistemin varsayılan olarak Türkçe ("tr") çalışmasını sağlar.
        localeResolver.setDefaultLocale(new Locale("tr"));

        // Sistemin desteklediği resmi diller listesini belirler (Sadece Türkçe ve İngilizce kabul edilir).
        // Desteklenmeyen bir dil gelirse yine varsayılan dil olan Türkçe devreye girer.
        localeResolver.setSupportedLocales(Arrays.asList(new Locale("tr"), new Locale("en")));

        // Yapılandırılmış dil çözümleyicisini geri döndürür.
        return localeResolver;
    }
}