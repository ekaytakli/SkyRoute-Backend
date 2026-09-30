package com.flightapp.flightticketsystem.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "user_first_name")
    private String firstName;

    @Column(name = "user_last_name")
    private String lastName;

    @Email(message = "Lütfen geçerli bir e-posta adresi giriniz")
    @NotNull(message = "E-posta alanı zorunludur")
    @NotBlank(message = "E-posta alanı boş bırakılamaz")
    @Column(name = "email")
    private String email;

    @NotNull(message = "Şifre alanı zorunludur")
    @NotBlank(message = "Şifre alanı boş bırakılamaz")
    @Column(name = "password")
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Ticket> tickets;

    @ManyToMany(fetch = FetchType.EAGER)
    //kullanıcı sisteme giriş yaptığında rollerinin de anında veritabanından çekilmesini sağlar
    @JoinTable(
            name = "user_roles", // köprü tablonun adı
            joinColumns = @JoinColumn(name = "user_id"), // bu tablodan gidecek olan id
            inverseJoinColumns = @JoinColumn(name = "role_id") //karşı tablodan gelecek id
    )
    private Set<Role> roles = new HashSet<>(); //aynı rolün iki kez eklenmesini engeller




    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.getRoleName().toUpperCase(java.util.Locale.ENGLISH)))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.email; //SpringSecurity kullanıcıyı tanımak için bir kullanıcı adı arar biz sisteme kullanıcı adı ile değil e-posta ile girş yapıyoruz
        //oyüzden email döndürüyoruz
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;    //burda hesabın süresinin asla dolmadığını söylüyoruz
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;   //diyerek tüm hesapların her zaman açık olduğunu belirtiyoruz
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; //diyerek şifrenin her zaman geçerli olduğunu söylüyoruz
    }

    @Override
    public boolean isEnabled() {
        return true; //diyerek hesapları aktif hale getiriyoruz
    }

}
