package com.example.openai.demo.model;

import java.util.List;

public class Country {

    public record CountryCities(String countryName, List<String> listOfCities) {}
}
