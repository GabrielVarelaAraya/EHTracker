package com.example.ehtracker.data.model

enum class Currency(val symbol: String, val code: String, val displayName: String) {
    USD("$", "USD", "US Dollar"),
    EUR("\u20AC", "EUR", "Euro"),
    CRC("\u20A1", "CRC", "Costa Rican Col\u00F3n"),
    MXN("MX$", "MXN", "Mexican Peso"),
    GBP("\u00A3", "GBP", "British Pound"),
    JPY("\u00A5", "JPY", "Japanese Yen"),
    BRL("R$", "BRL", "Brazilian Real"),
    COP("COL$", "COP", "Colombian Peso"),
    PEN("S/", "PEN", "Peruvian Sol"),
    ARS("ARS$", "ARS", "Argentine Peso"),
    CLP("CLP$", "CLP", "Chilean Peso"),
    GTQ("Q", "GTQ", "Guatemalan Quetzal"),
    HNL("L", "HNL", "Honduran Lempira"),
    NIO("C$", "NIO", "Nicaraguan C\u00F3rdoba"),
    PAB("B/.", "PAB", "Panamanian Balboa"),
    DOP("RD$", "DOP", "Dominican Peso"),
    CUP("\u20B1", "CUP", "Cuban Peso");

    companion object {
        fun fromCode(code: String): Currency {
            return entries.find { it.code == code } ?: USD
        }
    }
}
