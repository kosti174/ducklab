package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Zorgt ervoor dat je index.html verbinding mag maken met Java
public class AdminController {

    @Autowired
    private JavaMailSender mailSender;

    // Je admin e-mail om in te loggen op het verborgen admin-paneel
    private final String ADMIN_EMAIL = "kostasnikora@gmail.com";
    private final String BUSINESS_EMAIL = "ducklabstudio009@gmail.com";
    
    private List<String> bestellingen = new ArrayList<>();
    private String huidigeLoginCode;

    // 1. LOGIN CODE GENEREREN EN VERSTUREN VIA DUCKLAB EMAIL
    @PostMapping("/auth/code")
    public String stuurLoginCode(@RequestParam String email) {
        // Genereer een random 6-cijferige code
        Random random = new Random();
        huidigeLoginCode = String.format("%06d", random.nextInt(100000));

        try {
            SimpleMailMessage bericht = new SimpleMailMessage();
            bericht.setFrom(BUSINESS_EMAIL); // Verzonden vanuit je studio mail
            bericht.setTo(email); // Naar het ingevoerde e-mailadres
            bericht.setSubject("Je DuckLab Studio Verificatiecode");
            bericht.setText("Hallo,\n\nJe unieke verificatiecode om in te loggen is: " + huidigeLoginCode + "\n\nVeel printplezier!\n\nMet vriendelijke groet,\nDuckLab Studio");
            
            mailSender.send(bericht);
            System.out.println("E-mail succesvol verzonden naar " + email + " met code: " + huidigeLoginCode);
            return "Code verzonden!";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("E-mail verzenden mislukt: " + e.getMessage());
        }
    }

    // 2. PRODUCT UPLOADEN (Naam, Prijs, Foto)
    @PostMapping("/admin/upload")
    public String uploadProduct(
            @RequestParam String naam,
            @RequestParam double prijs,
            @RequestParam("foto") MultipartFile foto) {
        
        String fotoNaam = foto.getOriginalFilename();
        System.out.println("Nieuw item ontvangen voor DuckLab Studio: " + naam + " (€" + prijs + ") - Foto: " + fotoNaam);
        
        return "Product succesvol live gezet!";
    }

    // 3. BESTELLING AFREKENEN EN IN DE LIJST ZETTEN
    @PostMapping("/checkout/payconiq")
    public String verwerkBestelling(
            @RequestParam String naam,
            @RequestParam String postcode,
            @RequestParam String adres) {
        
        // Sla de bestelling op zonder het busnummer
        String nieuweBestelling = "Klant: " + naam + " | Adres: " + adres + ", " + postcode + " | Status: Wacht op Payconiq betaling";
        bestellingen.add(nieuweBestelling);
        
        System.out.println("Nieuwe bestelling binnengekomen! " + nieuweBestelling);
        return "Payconiq betaling gestart!";
    }

    // 4. BESTELLINGSLIJST OPVRAGEN (Alleen voor jou als admin)
    @GetMapping("/admin/bestellingen")
    public List<String> getBestellingen(@RequestParam String email) {
        if (!email.equalsIgnoreCase(ADMIN_EMAIL)) {
            throw new RuntimeException("Geen toegang! Je bent niet de admin van DuckLab Studio.");
        }
        return bestellingen;
    }
}
