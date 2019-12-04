import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Help {
    private static final BigInteger TWO = new BigInteger("2");
    private static final BigInteger THREE = new BigInteger("3");
    private static final BigInteger FOUR = new BigInteger("4");
    private static final BigInteger FIVE = new BigInteger("5");
    private static final BigInteger EIGHT = new BigInteger("8");
    public static final int k = 10000;

    public static BigInteger jacobi(BigInteger a, BigInteger b) {

        if (!a.gcd(b).equals(BigInteger.ONE)) return BigInteger.ZERO;
        BigInteger r = BigInteger.ONE;

        if (a.compareTo(BigInteger.ZERO) == -1){
            a = a.negate();
            if (b.mod(FOUR).equals(THREE)){
                r = r.negate();
            }
        }

        while (!a.equals(BigInteger.ZERO)) {
            BigInteger t = BigInteger.ZERO;
            while (a.mod(TWO).equals(BigInteger.ZERO)){
                t = t.add(BigInteger.ONE);
                a  = a.divide(TWO);
            }

            if (!t.mod(TWO).equals(BigInteger.ZERO)) {
                BigInteger mod = b.mod(EIGHT);

                if (mod.equals(THREE) || mod.equals(FIVE)) {
                    r = r.negate();
                }
            }

            BigInteger modA = a.mod(FOUR);

            if (modA.equals(b.mod(FOUR)) && modA.equals(THREE)) {
                r = r.negate();
            }
            BigInteger c = a;
            a = b.mod(c);
            b = c;
        }
        return r;
    }

    public static boolean isPrime(BigInteger p, int l, int roundCount) {

        if (p.equals(TWO) || p.equals(THREE) || p.equals(FIVE)) {
            return true;
        }

        if (p.equals(BigInteger.ONE) || p.equals(BigInteger.ZERO)) {
            return false;
        }

        BigInteger numberMinus1 = p.subtract(BigInteger.ONE);
        BigInteger t = numberMinus1;
        int s = 0;
        while (t.mod(TWO).equals(BigInteger.ZERO)) {
            t = t.divide(TWO);
            s++;
        }
        for (int i = 0; i < roundCount; i++) {

            BigInteger random = new BigInteger(l, new Random());
            BigInteger a = TWO.add(random).mod(p.subtract(TWO));
            BigInteger x = a.modPow(t, p);
            if (x.equals(BigInteger.ONE) || x.equals(numberMinus1)) continue;
            boolean flagToStop = true;
            for (int j = 0; j < s - 1; j++) {
                x = x.modPow(TWO, p);

                if (x.equals(BigInteger.ONE)) {
                    return false;
                }

                if (x.equals(numberMinus1)) {
                    flagToStop = false;
                    break;
                }
            }
            if (flagToStop) {
                return false;
            }
        }
        return true;
    }

    public static Pair<BigInteger, BigInteger> squareRoot(BigInteger n, BigInteger p) {

        BigInteger numberMinus1 = p.subtract(BigInteger.ONE);
        BigInteger q = numberMinus1;
        BigInteger s = BigInteger.ZERO;
        while (q.mod(TWO).equals(BigInteger.ZERO)) {
            q = q.divide(TWO);
            s = s.add(BigInteger.ONE);
        }
        if (s.equals(BigInteger.ONE)) {
            BigInteger result = n.modPow((p.add(BigInteger.ONE)).divide(FOUR), p);
            return new Pair<>(result, p.subtract(result));
        }

        BigInteger z;
        do {
            z = new BigInteger(p.bitCount(), new Random());
        } while (!jacobi(z, p).equals(BigInteger.valueOf(-1)));
        BigInteger c = z.modPow(q, p);
        BigInteger r = n.modPow((q.add(BigInteger.ONE)).divide(TWO), p);
        BigInteger t = n.modPow(q, p);
        BigInteger m = s;

        while (true) {
            if (t.mod(p).equals(BigInteger.ONE)) {
                return new Pair<>(r, p.subtract(r));
            }
            BigInteger i;
            for (i = BigInteger.ONE; i.compareTo(m) == -1; i = i.add(BigInteger.ONE)) {
                if (t.modPow(TWO.pow(i.intValue()), p).equals(BigInteger.ONE)) {
                    break;
                }
            }
            BigInteger b = c.modPow(BigInteger.valueOf((int)Math.pow(2, m.intValue() - i.intValue() - 1)), p);
            r = r.multiply(b).mod(p);
            t = t.multiply(b.pow(2)).mod(p);
            c = b.pow(2).mod(p);
            m = i;
        }
    }

    public static void printParams(BigInteger p, Pair<BigInteger, BigInteger> q, BigInteger r, BigInteger b) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("Params.txt"))) {
            String s = "Простое число p = " + p + "\n" +
                    "Коэффицент B = " + b + "\n" +
                    "Образующая точка Q = (" + q.getKey() + ", " + q.getValue() + ")" +
                    " простого порядка r = " + r + "\n";
            bufferedWriter.write(s);
        }
        catch (IOException e) {
            System.out.println("Ошибка записи!");
        }
    }
    public static void writeInFile(BigInteger p, Pair<BigInteger, BigInteger> q, BigInteger r, BigInteger b, String file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("p:" + p + "\n");
            fileWriter.write("xQ:" + q.getKey() + "\n");
            fileWriter.write("yQ:" + q.getValue() + "\n");
            fileWriter.write("r:" + r + "\n");
            fileWriter.write("b:" + b + "\n");
            fileWriter.close();
        }
        catch (IOException e) {
            System.out.println("Ошибка записи в файл!");
        }
    }

    public static void printPoints(Pair<BigInteger, BigInteger> q, BigInteger p, BigInteger r) {
        try (BufferedWriter bufferedWriter1 = new BufferedWriter(new FileWriter("X.txt"));
             BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter("Y.txt"))) {

            List<Pair<BigInteger, BigInteger>> points = new ArrayList<>();

            Pair<BigInteger, BigInteger> point = q;

            while (point != null) {
                points.add(point);
                point = EllipticCurve.summ(point, q, p);
            }

            for (Pair<BigInteger, BigInteger> xy: points) {
                    bufferedWriter1.write(xy.getKey() + "\n");
                    bufferedWriter2.write(xy.getValue() + "\n");
            }
        }
        catch (IOException e) {
            System.out.println("Ошибка записи!");
        }
    }

    private static int[] generateBits(int l) {
        Random bit = new Random();

        int primeNumberBit[] = new int[l];
        primeNumberBit[l - 1] = 1;
        primeNumberBit[0] = 1;

        for (int i = 1; i < l - 1; i++) {
            primeNumberBit[i] = bit.nextInt(2);
        }

        return primeNumberBit;
    }

    public static BigInteger generatePrime(int l) {
        BigInteger mod = BigInteger.ONE;
        for (int i = 0; i < l; i++)
            mod = mod.multiply(TWO);

        BigInteger number = BigInteger.valueOf(0);
        BigInteger deg = TWO.modPow(BigInteger.valueOf(l - 1), mod);
        int primeNumberBit[] = generateBits(l);

        for (int i = 0; i < l; i++) {
            number = number.add(deg.multiply(BigInteger.valueOf(primeNumberBit[i])));
            deg = deg.divide(TWO);
        }
        return number;
    }
}
