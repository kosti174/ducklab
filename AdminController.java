package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.*;
import java.util.List;
import java.util.Random;

// ==========================================
// DB ENTITIES (Toegevoegd: fotoUrl kolom!)
// ==========================================

@Entity
class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String naam;
    private double prijs;
    private String fotoUrl; // Houdt de link naar de afbeelding bij

    public Product() {}
    public Product(String naam, double prijs, String fotoUrl) { 
        this.naam = naam; 
        this.prijs = prijs; 
        this.fotoUrl = fotoUrl; 
    }
    public Long getId() { return id; }
    public String getNaam() { return naam; }
    public double getPrijs() { return prijs; }
    public String getFotoUrl() { return fotoUrl; }
}

@Entity
class Bestelling {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String klantNaam;
    private String postcode;
    private String adres;
    private String status;

    public Bestelling() {}
    public Bestelling(String klantNaam, String postcode, String adres, String status) {
        this.klantNaam = klantNaam; this.postcode = postcode; this.adres = adres; this.status = status;
    }
    public Long getId() { return id; }
    public String getKlantNaam() { return klantNaam; }
    public String getPostcode() { return postcode; }
    public String getAdres() { return adres; }
    public String getStatus() { return status; }
}

// ==========================================
// DB REPOSITORIES
// ==========================================

@Repository interface ProductRepository extends JpaRepository<Product, Long> {}
@Repository interface BestellingRepository extends JpaRepository<Bestelling, Long> {}

// ==========================================
// REST CONTROLLER
// ==========================================

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired private JavaMailSender mailSender;
    @Autowired private ProductRepository productRepository;
    @Autowired private BestellingRepository bestellingRepository;

    private final String ADMIN_EMAIL = "kostasnikora@gmail.com";
    private final String BUSINESS_EMAIL = "ducklabstudio009@gmail.com";
    private String huidigeLoginCode;

    // 1. E-MAIL CODE VERZENDEN
    @PostMapping("/auth/code")
    public String stuurLoginCode(@RequestParam String email) {
        Random random = new Random();
        huidigeLoginCode = String.format("%06d", random.nextInt(100000));

        SimpleMailMessage bericht = new SimpleMailMessage();
        bericht.setFrom(BUSINESS_EMAIL);
        bericht.setTo(email);
        bericht.setSubject("Je DuckLab Studio Verificatiecode");
        bericht.setText("Hallo,\n\nJe unieke verificatiecode om in te loggen is: " + huidigeLoginCode + "\n\nVeel printplezier!\n\nMet vriendelijke groet,\nDuckLab Studio");
        
        mailSender.send(bericht);
        System.out.println("E-mail ECHT verzonden naar " + email + " met code: " + huidigeLoginCode);
        return "Code verzonden!";
    }

    @PostMapping("/auth/verifieer")
    public boolean verifieerCode(@RequestParam String code) {
        return code.equals(huidigeLoginCode);
    }

    // 2. PRODUCTEN BEHEREN (Nu met fotoUrl!)
    @GetMapping("/products")
    public List<Product> getProducten() {
        return productRepository.findAll();
    }

    @PostMapping("/admin/upload")
    public String uploadProduct(
            @RequestParam String naam, 
            @RequestParam double prijs, 
            @RequestParam String fotoUrl) {
        
        Product nieuwProduct = new Product(naam, prijs, fotoUrl);
        productRepository.save(nieuwProduct);
        return "Product met afbeelding succesvol opgeslagen!";
    }

    @DeleteMapping("/admin/product/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "Product succesvol verwijderd!";
    }

    // 3. BESTELLINGEN BEHEREN
    @PostMapping("/checkout/payconiq")
    public String verwerkBestelling(
            @RequestParam String naam,
            @RequestParam String postcode,
            @RequestParam String adres) {
        
        Bestelling nieuweBestelling = new Bestelling(naam, postcode, adres, "Wacht op Payconiq betaling");
        bestellingRepository.save(nieuweBestelling);
        return "Bestelling opgeslagen!";
    }

    @GetMapping("/admin/bestellingen")
    public List<Bestelling> getBestellingen(@RequestParam String email) {
        if (!email.equalsIgnoreCase(ADMIN_EMAIL)) {
            throw new RuntimeException("Geen toegang!");
        }
        return bestellingRepository.findAll();
    }
}
