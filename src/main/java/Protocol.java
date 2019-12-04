import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import javafx.util.Pair;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class Protocol {

    private static final String INPUT = "Input.txt";
    private static final String OPEN_KEY = "OpenKey.txt";
    private static final String ALICE = "Alice.txt";
    private static final String BOB = "Bob.txt";
    private static final String M = "M.txt";
    private int l;
    private double v = 1.0;
    private List<Integer> command = new ArrayList<>();

    public void init() throws Exception {
        writeInFile(ALICE, null);
        writeInFile(BOB, null);
        writeInFile(OPEN_KEY, null);

        this.v = 1;
        command.add(1);
        Map<String, String> inputOpenParameters = readFromFile(INPUT);
        Map<String, String> aliceParameters = new HashMap<>();
        Map<String, String> bobsParameters = new HashMap<>();
        Applicant applicant = new Applicant(new BigInteger(inputOpenParameters.get("p")),
                new BigInteger(inputOpenParameters.get("xQ")),
                new BigInteger(inputOpenParameters.get("yQ")),
                new BigInteger(inputOpenParameters.get("r")),
                new BigInteger(String.valueOf(l)),
                new BigInteger(inputOpenParameters.get("b"))
        );
        inputOpenParameters.put("xP", applicant.getP().getKey().toString());
        inputOpenParameters.put("yP", applicant.getP().getValue().toString());
        aliceParameters.put("xP", applicant.getP().getKey().toString());
        aliceParameters.put("yP", applicant.getP().getValue().toString());
        aliceParameters.put("xQ", applicant.getQ().getKey().toString());
        aliceParameters.put("yQ", applicant.getQ().getValue().toString());
        aliceParameters.put("b", applicant.getB().toString());
        aliceParameters.put("r", applicant.getR().toString());
        aliceParameters.put("p", applicant.getPrime().toString());
        aliceParameters.put("l", applicant.getL().toString());

        bobsParameters.put("xP", applicant.getP().getKey().toString());
        bobsParameters.put("yP", applicant.getP().getValue().toString());
        bobsParameters.put("xQ", applicant.getQ().getKey().toString());
        bobsParameters.put("yQ", applicant.getQ().getValue().toString());
        bobsParameters.put("b", applicant.getB().toString());
        bobsParameters.put("r", applicant.getR().toString());
        bobsParameters.put("p", applicant.getPrime().toString());

        writeInFile(OPEN_KEY, inputOpenParameters);
        writeInFile(ALICE, aliceParameters);
        writeInFile(BOB, bobsParameters);
    }

    public void firstStep() throws Exception{
        Map<String, String> ver = readFromFile("ver.txt");
        String m = readMessage(M);
        if (m == null) {
            throw new Exception("Невозможно подсчитать хэш подпись, так как файл с сообщением M.txt пуст");
        }
        Map<String, String> aliceParameters = readFromFile(ALICE);
        if (aliceParameters == null || aliceParameters.isEmpty()) {
            throw new Exception("Невозможно подсчитать хэш подпись, так как файл Alice.txt пуст");
        }
        BigInteger r = new BigInteger(aliceParameters.get("r"));
        BigInteger p = new BigInteger(aliceParameters.get("p"));
        BigInteger xQ = new BigInteger(aliceParameters.get("xQ"));
        BigInteger yQ = new BigInteger(aliceParameters.get("yQ"));
        BigInteger b = new BigInteger(aliceParameters.get("b"));
        if (ver.size() > 0 && this.v == 1) {
            if (ver.get("xQ").equals(xQ.toString()) && ver.get("yQ").equals(yQ.toString())
                    && ver.get("p").equals(p.toString()) && ver.get("r").equals(r.toString())
                    && ver.get("b").equals(b.toString())) {
                this.v = 0;
                String cmd = ver.get("command");
                List<Integer> command = new ArrayList<>();
                for (Character c : cmd.toCharArray()) {
                    command.add(Integer.parseInt(c.toString()));
                }
                this.command = command;
            }
        }
        command.add(2);
        BigInteger hash = encode(m, r);
        aliceParameters.put("hash", hash.toString());
        writeInFile(ALICE, aliceParameters);
    }

    public void secondStep() throws Exception{
        Map<String, String> aliceParameters = readFromFile(ALICE);
        if (aliceParameters == null || aliceParameters.isEmpty()) {
            throw new Exception("Невозможно сформировать подпись, так как файл Alice.txt пуст");
        }
        BigInteger r = new BigInteger(aliceParameters.get("r"));
        BigInteger p = new BigInteger(aliceParameters.get("p"));
        BigInteger xQ = new BigInteger(aliceParameters.get("xQ"));
        BigInteger yQ = new BigInteger(aliceParameters.get("yQ"));
        BigInteger l = new BigInteger(aliceParameters.get("l"));

        BigInteger k;
        Pair<BigInteger, BigInteger> R;
        BigInteger xR;
        BigInteger yR;
        do {
            k = new BigInteger(l.bitLength(), new SecureRandom()).mod(r);
            R = EllipticCurve2.generateQ(xQ, yQ, p, k);
            xR = R.getKey();
            yR = R.getValue();
        }
        while (xR.add(yR).mod(r).equals(BigInteger.ZERO) || k.equals(BigInteger.ZERO));

        aliceParameters.put("xR", xR.toString());
        aliceParameters.put("yR", yR.toString());
        aliceParameters.put("k", k.toString());

        writeInFile(ALICE, aliceParameters);
    }

    public void thirdStep() throws Exception{
        Map<String, String> aliceParameters = readFromFile(ALICE);
        if (aliceParameters == null || aliceParameters.isEmpty()) {
            throw new Exception("Невозможно сформировать подпись, так как файл Alice.txt пуст");
        }
        Map<String, String> bobsParameters = readFromFile(BOB);
        if (bobsParameters == null || bobsParameters.isEmpty()) {
            throw new Exception("Невозможно сформировать подпись, так как файл Bob.txt пуст");
        }

        if (aliceParameters.get("hash") == null) {
            throw new Exception("Невозможно сформировать подпись, так как в файле Alice.txt не подсчитан хэш");
        }

        BigInteger hash = new BigInteger(aliceParameters.get("hash"));

        BigInteger r = new BigInteger(aliceParameters.get("r"));
        if (aliceParameters.get("xR") == null || aliceParameters.get("yR") == null || aliceParameters.get("k") == null) {
            throw new Exception("Невозможно сформировать подпись, так как в файле Alice.txt отсутствуют параметр k и точка R");
        }

        BigInteger xR = new BigInteger(aliceParameters.get("xR"));
        BigInteger yR = new BigInteger(aliceParameters.get("yR"));
        BigInteger l = new BigInteger(aliceParameters.get("l"));
        BigInteger k = new BigInteger(aliceParameters.get("k"));
        String m = readMessage(M);

        BigInteger s = hash.subtract(l.multiply(xR.add(yR)).mod(r)).multiply(k.modInverse(r)).mod(r);

        bobsParameters.put("m", m);
        bobsParameters.put("xR", xR.toString());
        bobsParameters.put("yR", yR.toString());
        bobsParameters.put("s", s.toString());

        writeInFile(BOB, bobsParameters);
    }

    public void check() throws Exception {
        command.add(3);

        Map<String, String> open = readFromFile(BOB);
        if (open == null || open.isEmpty()) {
            throw new Exception("Невозможно проверить подпись, так как нет открытого ключа (файл Bob.txt)");
        }
        Map<String, String> signature = readFromFile(BOB);
        if (signature == null || signature.isEmpty()) {
            throw new Exception("Невозможно проверить подпись, так как Боб не получил ее (файл Bob.txt)");
        }
        String m = signature.get("m");

        if (signature.get("xR") == null || signature.get("yR") == null) {
            throw new Exception("Невозможно сформировать подпись, так как в файле Bob.txt отсутствует точка R");
        }

        BigInteger xR = new BigInteger(signature.get("xR"));
        BigInteger yR = new BigInteger(signature.get("yR"));
        BigInteger s = new BigInteger(signature.get("s"));
        BigInteger p = new BigInteger(open.get("p"));
        BigInteger b = new BigInteger(open.get("b"));
        BigInteger r = new BigInteger(open.get("r"));
        BigInteger xQ = new BigInteger(open.get("xQ"));
        BigInteger yQ = new BigInteger(open.get("yQ"));
        BigInteger xP = new BigInteger(open.get("xP"));
        BigInteger yP = new BigInteger(open.get("yP"));

        if (!yR.modPow(BigInteger.valueOf(2), p).equals(xR.modPow(BigInteger.valueOf(3), p).add(b).mod(p))) {
            writeInFile("ver.txt", null);
            this.v = 1;
            throw new Exception("Ошибка!Точка не принадлежит кривой!");
        }

        BigInteger hash = encode(m, r);

        Pair<BigInteger, BigInteger> hQ = EllipticCurve2.generateQ(xQ, yQ, p, hash);
        BigInteger f = xR.add(yR).mod(r);
        Pair<BigInteger, BigInteger> fP = EllipticCurve2.generateQ(xP, yP, p, f);

        Pair<BigInteger, BigInteger> sR = EllipticCurve2.generateQ(xR, yR, p, s);

        Pair<BigInteger, BigInteger> eq = EllipticCurve2.summ(fP, sR, p);

        if (!hQ.getKey().equals(eq.getKey()) || !hQ.getValue().equals(eq.getValue())) {
            writeInFile("ver.txt", null);
            this.v = 1;
            throw new Exception("Подпись отвергается!");
        }
        System.out.println("Подпись подтверждается!");

        Map<String, String> ver = new HashMap<>();
        ver.put("ver", String.valueOf(v));
        String command = "";
        for (int i = 0; i < this.command.size(); i++) {
            command += this.command.get(i);
        }
        ver.put("command", command);
        ver.put("xQ", xQ.toString());
        ver.put("yQ", yQ.toString());
        ver.put("p", p.toString());
        ver.put("r", r.toString());
        ver.put("b", b.toString());
        writeInFile("ver.txt", ver);
    }

    public static void writeInFile(String file, Map<String, String> params) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            if (params == null) {
                fileWriter.write("");
            } else {
                for (Map.Entry<String, String> p : params.entrySet()) {
                    fileWriter.write(p.getKey() + ":" + p.getValue() + "\n");
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл!");
        }
    }


    public static Map<String, String> readFromFile(String file) {
        Map<String, String> params = new HashMap<>();
        try {
            Scanner scanner = new Scanner(new FileReader(file));
            while (scanner.hasNext()) {
                String s = scanner.nextLine().trim();
                params.put(s.split(":")[0], s.split(":")[1]);
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла!");
        }
        return params;
    }

    public static String readMessage(String file) {
        String res = "";
        try {
            Scanner scanner = new Scanner(new FileReader(file));
            while (scanner.hasNext()) {
                res += scanner.nextLine();
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла!");
        }
        return res;
    }

    public static boolean isNumber(String s) {
        char[] a = s.toCharArray();

        if (a[0] == '0' && a.length > 1)
            return false;

        for (int i = 0; i < a.length; i++) {

            if (a[i] < '0' || a[i] > '9')
                return false;
        }
        return true;
    }

    public static BigInteger encode(String text, BigInteger p) {

        HashCode a = Hashing.sha512().hashBytes(text.getBytes());
        BigInteger b = BigInteger.valueOf(a.asLong()).abs();
        return b.mod(p);
    }

    public void setL(int l) {
        this.l = l;
    }
}
