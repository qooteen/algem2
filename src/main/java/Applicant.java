import javafx.util.Pair;

import java.math.BigInteger;

public class Applicant {

    private BigInteger l;
    private Pair<BigInteger, BigInteger> Q;
    private Pair<BigInteger, BigInteger> P;
    private BigInteger prime;
    private BigInteger r;
    private BigInteger b;

    public Applicant(BigInteger prime, BigInteger x, BigInteger y, BigInteger r, BigInteger l, BigInteger b) throws Exception{
        this.prime = prime;
        this.r = r;
        if (x.compareTo(BigInteger.ZERO) == -1 || x.compareTo(prime) == 1) {
            throw new Exception("Ошибка xQ должен быть 0 <= xQ < p");
        }
        if (y.compareTo(BigInteger.ZERO) == -1 || y.compareTo(prime) == 1) {
            throw new Exception("Ошибка yQ должен быть 0 <= yQ < p");
        }
        this.Q = new Pair<>(x, y);
        if (l.compareTo(r) >= 0) {
            throw new Exception("Ошибка l должен быть l < r");
        }
        this.l = l;
        Pair<BigInteger, BigInteger> P = EllipticCurve2.generateQ(x, y, prime, l);
        if (P == null) {
            throw new Exception("Ошибка P - бесконечно удаленная точка!");
        }
        this.P = P;
        if (b.compareTo(BigInteger.ZERO) == -1 || b.compareTo(prime) == 1) {
            throw new Exception("Ошибка b должен быть 0 <= b < p");
        }
        this.b = b;
    }

    public BigInteger getL() {
        return l;
    }

    public Pair<BigInteger, BigInteger> getQ() {
        return Q;
    }

    public BigInteger getR() {
        return r;
    }

    public BigInteger getPrime() {
        return prime;
    }

    public Pair<BigInteger, BigInteger> getP() {
        return P;
    }

    public BigInteger getB() {
        return b;
    }
}
