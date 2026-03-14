package com.example.servlet;

final class ComplexNumber {
    static final ComplexNumber ZERO = new ComplexNumber(0.0, 0.0);

    final double re;
    final double im;

    ComplexNumber(double re, double im) {
        this.re = re;
        this.im = im;
    }

    static ComplexNumber fromPolar(double magnitude, double phase) {
        return new ComplexNumber(magnitude * Math.cos(phase), magnitude * Math.sin(phase));
    }

    ComplexNumber add(ComplexNumber other) {
        return new ComplexNumber(re + other.re, im + other.im);
    }

    ComplexNumber subtract(ComplexNumber other) {
        return new ComplexNumber(re - other.re, im - other.im);
    }

    ComplexNumber multiply(ComplexNumber other) {
        return new ComplexNumber(re * other.re - im * other.im, re * other.im + im * other.re);
    }

    ComplexNumber scale(double factor) {
        return new ComplexNumber(re * factor, im * factor);
    }

    ComplexNumber divide(ComplexNumber other) {
        double den = other.re * other.re + other.im * other.im;
        return new ComplexNumber(
                (re * other.re + im * other.im) / den,
                (im * other.re - re * other.im) / den
        );
    }

    ComplexNumber conjugate() {
        return new ComplexNumber(re, -im);
    }

    double abs() {
        return Math.hypot(re, im);
    }

    boolean isZero() {
        return abs() < 1e-9;
    }

    String toLatex() {
        String real = formatDouble(re);
        String imag = formatDouble(Math.abs(im));
        String imagUnit = Math.abs(Math.abs(im) - 1.0) < 1e-9 ? "j" : imag + "j";
        if (Math.abs(im) < 1e-9) {
            return real;
        }
        if (Math.abs(re) < 1e-9) {
            return (im < 0 ? "-" : "") + imagUnit;
        }
        return real + (im < 0 ? " - " : " + ") + imagUnit;
    }

    private static String formatDouble(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return Long.toString(Math.round(value));
        }
        Fraction fraction = approximateFraction(value, 1000);
        if (fraction != null) {
            if (fraction.denominator == 1L) {
                return Long.toString(fraction.numerator);
            }
            return "\\frac{" + fraction.numerator + "}{" + fraction.denominator + "}";
        }
        return String.format(java.util.Locale.US, "%.4f", value);
    }

    private static Fraction approximateFraction(double value, long maxDenominator) {
        double absValue = Math.abs(value);
        long bestNumerator = 0L;
        long bestDenominator = 1L;
        double bestError = Double.MAX_VALUE;

        for (long denominator = 1; denominator <= maxDenominator; denominator++) {
            long numerator = Math.round(absValue * denominator);
            double error = Math.abs(absValue - ((double) numerator / denominator));
            if (error < bestError) {
                bestError = error;
                bestNumerator = numerator;
                bestDenominator = denominator;
            }
            if (error < 1e-9) {
                break;
            }
        }

        if (bestError > 1e-6) {
            return null;
        }

        long gcd = gcd(bestNumerator, bestDenominator);
        bestNumerator /= gcd;
        bestDenominator /= gcd;
        if (value < 0) {
            bestNumerator = -bestNumerator;
        }
        return new Fraction(bestNumerator, bestDenominator);
    }

    private static long gcd(long a, long b) {
        long x = Math.abs(a);
        long y = Math.abs(b);
        while (y != 0) {
            long tmp = x % y;
            x = y;
            y = tmp;
        }
        return x == 0 ? 1 : x;
    }

    private static final class Fraction {
        final long numerator;
        final long denominator;

        private Fraction(long numerator, long denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }
    }
}
