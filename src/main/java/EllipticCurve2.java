import javafx.util.Pair;

import java.math.BigInteger;

public class EllipticCurve2 {

    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);

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

    public static Pair<BigInteger, BigInteger> generateQ(BigInteger x, BigInteger y, BigInteger p, BigInteger l) {
        Pair<BigInteger, BigInteger> point = new Pair<>(x, y);

        for (int i = 0; i < l.intValue() - 1; i++) {
            point = summ(point, new Pair<>(x, y), p);
        }
        return point;
    }
}
