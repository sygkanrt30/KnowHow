package ru.tbank.knowhow.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@ToString
@Table(name = "app_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50, name = "username")
    private String username;

    @Column(nullable = false, length = 150, name = "password")
    @ToString.Exclude
    private String password;

    @Column(nullable = false, length = 50, unique = true, name = "email")
    @ToString.Exclude
    private String email;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, name = "role", columnDefinition = "usr_role")
    private Role role;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "balance_id", nullable = false, unique = true)
    @ToString.Exclude
    private Balance balance;

    @Column(name = "created_at")
    @CreationTimestamp
    @ToString.Exclude
    private Instant createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "purchased_course",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @ToString.Exclude
    @Builder.Default
    private List<Course> purchasedCourses = new ArrayList<>();

    public void addPurchasedCourse(Course course) {
        purchasedCourses.add(course);
    }

    public void removePurchasedCourse(Course course) {
        purchasedCourses.remove(course);
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Rating> userRatings = new ArrayList<>();

    public void addUserRatings(Rating rating) {
        userRatings.add(rating);
    }

    public void removeUserRatings(Rating rating) {
        userRatings.remove(rating);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        var user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
