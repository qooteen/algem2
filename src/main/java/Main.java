import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Scanner;

public class Main {
    private static final String INPUT = "Input.txt";

    private static final String COMMANDS = "Выберете команду:\n" +
            "1 - Инициализация параметров\n" +
            "2 - Посчитать хэш-значение от сообщения М\n" +
            "3 - Генерация k и вычисление точки R\n" +
            "4 - Вычисление s и формирование подписи (M, R, s)\n" +
            "5 - Проверка подписи\n" +
            "0 - Выход из алгоритма";
    private static final BigInteger THREE = new BigInteger("3");
    public static void main(String[] args) throws Exception {

        System.out.println(COMMANDS);
        Protocol protocol = new Protocol();
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            if (!Protocol.isNumber(command)) {
                throw new Exception("Ошибка! Введите команду - число от 0 до 5");
            }
            switch (Integer.parseInt(command)) {
                case 1: {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                    System.out.println("Введите L - количество бит");
                    String s1 = reader.readLine();
                    if (s1.equals("") || !isNumber(s1)) {
                        throw new Exception("Ошибка! Введите число");
                    }
                    int L = Integer.parseInt(s1);
                    if (L <= 3) {
                        throw new Exception("Ошибка! Число бит должно быть > 3");
                    }

                    while (true) {
                        EllipticCurve ellipticCurve = new EllipticCurve();
                        while (true) {
                            ellipticCurve.findP(L);
                            if (!ellipticCurve.faction(THREE)) continue;
                            if (ellipticCurve.verify() && ellipticCurve.check(5)) break;
                        }

                        int k = 0;
                        boolean f = false;
                        while (true) {
                            if (k++ > Help.k) {
                                f = true;
                                break;
                            }
                            if (!ellipticCurve.generate()) continue;
                            if (ellipticCurve.checkXY()) {
                                ellipticCurve.generateQ();
                                break;
                            }
                        }

                        if (!f) {
                            Help.writeInFile(ellipticCurve.getP(), ellipticCurve.getQ(), ellipticCurve.getR(), ellipticCurve.getB(), INPUT);
                            Help.printPoints(ellipticCurve.getQ(), ellipticCurve.getP(), ellipticCurve.getR());
                            break;
                        }
                    }
                    System.out.println("Введите логарифм l");
                    String s2 = reader.readLine();
                    if (s2.equals("") || !isNumber(s2)) {
                        System.out.println("Введите число!!");
                        return;
                    }
                    int ll = Integer.parseInt(s2);
                    protocol.setL(ll);
                    protocol.init();
                    System.out.println("Шаг отработан!");
                    break;
                }
                case 2: {
                    protocol.firstStep();
                    System.out.println("Шаг отработан!");
                    break;
                }
                case 3: {
                    protocol.secondStep();
                    System.out.println("Шаг отработан!");
                    break;
                }
                case 4: {
                    protocol.thirdStep();
                    System.out.println("Шаг отработан!");
                    break;
                }
                case 5: {
                    protocol.check();
                    System.out.println("Шаг отработан!");
                    break;
                }
                case 0: {
                    return;
                }
                default: {
                    throw new Exception("Ошибка! Введите команду - число от 0 до 5");
                }
            }
        }


    }
    static boolean isNumber(String s) {
        char[] a = s.toCharArray();

        if (a[0] == '0' && a.length > 1)
            return false;

        for (int i = 0; i < a.length; i++) {

            if (a[i] < '0' || a[i] >'9')
                return false;
        }
        return true;
    }
}
