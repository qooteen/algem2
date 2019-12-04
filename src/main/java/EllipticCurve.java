import javafx.util.Pair;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EllipticCurve {
    private static final BigInteger TWO = new BigInteger("2");
    private static final BigInteger THREE = new BigInteger("3");
    private static final BigInteger SIX = new BigInteger("6");
    private BigInteger b;
    private BigInteger p;
    private BigInteger N;
    private BigInteger r;
    private BigInteger c;
    private BigInteger d;
    private Pair<BigInteger, BigInteger> q;
    private BigInteger x0;
    private BigInteger y0;
    private int L;

    public void findP(int L) {
        this.L = L;
        BigInteger p;
        do {
            p = BigInteger.probablePrime(L, new SecureRandom());
        } while (!Help.isPrime(p, L, 200) || p.toString(2).toCharArray()[0] != '1' || !p.mod(SIX).equals(BigInteger.ONE));
        this.p = p;
    }

    public boolean faction(BigInteger d) {
        BigInteger simbol = Help.jacobi(d.negate(), p);
        if (simbol.equals(BigInteger.valueOf(-1))) {
            return false;
        }
        Pair<BigInteger, BigInteger> u =  Help.squareRoot(d.negate(), p);

        BigInteger ui = u.getKey();
            BigInteger mi = p;
            int i = 0;
            List<BigInteger> uis = new ArrayList<>();
            uis.add(ui);
            while (true) {
                if (i >= Help.k) {
                    return false;
                }
                if (mi.equals(BigInteger.ZERO)) {
                    return false;
                }

                mi = (uis.get(i).pow(2).add(d)).divide(mi);
                ui = (ui.mod(mi)).min(mi.subtract(ui).mod(mi));
                uis.add(ui);
                if (mi.equals(BigInteger.ONE)) {
                    break;
                } else {
                    i++;
                }
            }

            BigInteger ai = uis.get(uis.size() - 2);
            BigInteger bi = BigInteger.ONE;
            BigInteger aiCopy = new BigInteger(ai.toString());


            while (true) {
                if (i == 0) {
                    if (!ai.abs().pow(2).mod(p).add(THREE.multiply(bi.abs().pow(2)).mod(p)).equals(p)) {
                        return false;
                    }
                    this.c = ai;
                    this.d = bi;
                    return true;
                }
                if ((uis.get(i - 1).multiply(ai).add(d.multiply(bi))).mod((ai.pow(2)).add(d.multiply(bi.pow(2)))).equals(BigInteger.ZERO)) {
                    ai = (uis.get(i - 1).multiply(ai).add(d.multiply(bi))).divide((ai.pow(2)).add(d.multiply(bi.pow(2))));
                } else {
                    ai = (uis.get(i - 1).negate().multiply(ai).add(d.multiply(bi))).divide((ai.pow(2)).add(d.multiply(bi.pow(2))));
                }
                if ((uis.get(i - 1).multiply(bi).subtract(aiCopy)).mod((aiCopy.pow(2)).add(d.multiply(bi.pow(2)))).equals(BigInteger.ZERO)) {
                    bi = (uis.get(i - 1).multiply(bi).subtract(aiCopy)).divide((aiCopy.pow(2)).add(d.multiply(bi.pow(2))));
                } else {
                    bi = (uis.get(i - 1).negate().multiply(bi).subtract(aiCopy)).divide((aiCopy.pow(2)).add(d.multiply(bi.pow(2))));
                }
                aiCopy = new BigInteger(ai.toString());
                i--;
            }
    }

    public boolean verify() {

        List<BigInteger> T = new ArrayList<>();
        T.add(c.add(THREE.multiply(d)));
        T.add(c.subtract(THREE.multiply(d)));
        T.add(TWO.multiply(c));
        T.add(c.add(THREE.multiply(d)).negate());
        T.add(c.subtract(THREE.multiply(d)).negate());
        T.add(TWO.multiply(c).negate());
        BigInteger N;
        for (BigInteger t: T) {
            N = p.add(BigInteger.ONE).add(t);
            if (N.isProbablePrime(200)) {
                BigInteger r = N;
                this.N = N;
                this.r = r;
                return true;
            }
            if (N.mod(SIX).equals(BigInteger.ZERO)
                    && N.divide(SIX).isProbablePrime(200)) {
                BigInteger r = N.divide(SIX);
                this.N = N;
                this.r = r;
                return true;
            }
            if (N.mod(TWO).equals(BigInteger.ZERO)
                    && N.divide(TWO).isProbablePrime(200)) {
                BigInteger r = N.divide(TWO);
                this.N = N;
                this.r = r;
                return true;
            }
            if (N.mod(THREE).equals(BigInteger.ZERO)
                    && N.divide(THREE).isProbablePrime(200)) {
                BigInteger r = N.divide(THREE);
                this.N = N;
                this.r = r;
                return true;
            }
        }
        return false;
    }

    public boolean check(int m) {
        if (p.equals(r)) {
            return false;
        }
        for (int i = 1; i <= m; i++) {
            if (p.modPow(BigInteger.valueOf(i), r).equals(BigInteger.ONE)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCubic(BigInteger a, BigInteger p, BigInteger N) {
        return N.modPow(THREE, p).equals(a);
    }

    public static boolean isQuad(BigInteger a, BigInteger p, BigInteger N) {
        return N.modPow(TWO, p).equals(a);
    }

    public boolean generate() {

        int l = p.bitLength();

        BigInteger x0 = new BigInteger(new Random().nextInt(l + 1) + l / 2 + 1, 200, new SecureRandom()).mod(p);
        BigInteger y0 = new BigInteger(new Random().nextInt(l + 1) + l / 2 + 1, 200, new SecureRandom()).mod(p);

        BigInteger b = y0.pow(2).subtract(x0.pow(3)).mod(p);

        if (r.equals(N) && !isQuad(b, p, N) && !isCubic(b, p, N)) {
            this.b = b;
            this.x0 = x0;
            this.y0 = y0;
            return true;
        }

        if (r.multiply(SIX).equals(N) && isQuad(b, p, N) && isCubic(b, p, N)) {
            this.b = b;
            this.x0 = x0;
            this.y0 = y0;
            return true;
        }

        if (r.multiply(TWO).equals(N) && !isQuad(b, p, N) && isCubic(b, p, N)) {
            this.b = b;
            this.x0 = x0;
            this.y0 = y0;
            return true;
        }

        if (r.multiply(THREE).equals(N) && isQuad(b, p, N) && !isCubic(b, p, N)) {
            this.b = b;
            this.x0 = x0;
            this.y0 = y0;
            return true;
        }
        return false;
    }

    public boolean checkXY() {

        Pair<BigInteger, BigInteger> point = new Pair<>(x0, y0);

        for (BigInteger i = BigInteger.ONE; i.compareTo(N.subtract(BigInteger.ONE)) == -1; i = i.add(BigInteger.ONE)) {
            point = summ(point, new Pair<>(x0, y0), p);
            if (point == null) {
                return false;
            }
        }
        return summ(point, new Pair<>(x0, y0), p) == null;
    }

    public static Pair<BigInteger, BigInteger> summ(Pair<BigInteger, BigInteger> first, Pair<BigInteger, BigInteger> second,
                                                    BigInteger p) {
        try {
            if (first == null || second == null) {
                return null;
            }

            BigInteger l;
            BigInteger x1 = first.getKey();
            BigInteger y1 = first.getValue();
            BigInteger x2 = second.getKey();
            BigInteger y2 = second.getValue();

            if (x1.equals(x2) && y1.equals(y2)) {
                if (y1.equals(BigInteger.ZERO)) {
                    return null;
                } else {
                    l = x1.pow(2).multiply(THREE).multiply((TWO.multiply(y1)).modInverse(p));
                }
            } else {
                l = (y2.subtract(y1)).multiply((x2.subtract(x1)).modInverse(p)).mod(p);
            }

            BigInteger x3 = l.pow(2).subtract(x1).subtract(x2).mod(p);
            BigInteger y3 = (x1.subtract(x3)).multiply(l).subtract(y1).mod(p);
            return new Pair<>(x3, y3);
        } catch (ArithmeticException e) {
            return null;
        }
    }

    public void generateQ() {
        Pair<BigInteger, BigInteger> point = new Pair<>(x0, y0);

        for (int i = 0; i < N.divide(r).intValue() - 1; i++) {
            point = summ(point, new Pair<>(x0, y0), p);
        }
        this.q = point;
    }

    public BigInteger getB() {
        return b;
    }

    public Pair<BigInteger, BigInteger> getQ() {
        return q;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getR() {
        return r;
    }
}
