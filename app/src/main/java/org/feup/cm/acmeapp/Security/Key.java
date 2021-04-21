package org.feup.cm.acmeapp.Security;

import java.util.Arrays;

public class Key {
    private byte[] modulus;
    private byte[] exponent;

    public Key(byte[] modulus, byte[] exponent) {
        this.modulus = modulus;
        this.exponent = exponent;
        this.toString();
    }

    public byte[] getModulus() {
        return modulus;
    }

    public byte[] getExponent() {
        return exponent;
    }

    @Override
    public String toString() {
        return "Key{" +
                "modulus=" + Arrays.toString(modulus) +
                ", exponent=" + Arrays.toString(exponent) +
                '}';
    }
}
