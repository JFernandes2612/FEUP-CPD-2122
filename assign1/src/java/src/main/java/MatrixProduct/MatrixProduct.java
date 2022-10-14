package MatrixProduct;

import java.util.InputMismatchException;
import java.util.Scanner;

import static java.lang.Math.min;

public class MatrixProduct {

    static int getDimensions(Scanner scanner){
        System.out.print("Dimensions: lins=cols ? ");
        int retVal = scanner.nextInt();
        scanner.nextLine();
        return retVal;
    }

    static void onMult(int m_ar, int m_br) {

        double time1, time2;
        double temp;
        int i, j, k;

        double[] pha, phb, phc;

        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];

        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_ar; j++) {
                pha[i * m_ar + j] = 1.0;
            }
        }

        for (i = 0; i < m_br; i++) {
            for (j = 0; j < m_br; j++) {
                phb[i * m_br + j] = (double) i+1;
            }
        }

        time1 = System.currentTimeMillis();

        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_br; j++) {
                temp = 0;
                for (k = 0; k < m_ar; k++) {
                    temp += pha[i * m_ar + k] * phb[k * m_br + j];
                }
                phc[i * m_ar + j] = temp;
            }
        }

        time2 = System.currentTimeMillis();

        System.out.println("\nTime: " + (time2 - time1)/1000 + " seconds");
        /*
        System.out.println("Result Matrix:");
        for (i = 0; i < min(10, m_br); i++) {
            System.out.print(phc[i] + " ");
        }
         */
        System.out.print("\n");
        return;
    }

    static void onMultLine(int m_ar, int m_br) {
        double time1, time2;
        int i, j, k;
        double temp;

        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_ar; j++) {
                pha[i * m_ar + j] = (double) 1.0;
            }
        }

        for (i = 0; i < m_br; i++) {
            for (j = 0; j < m_br; j++) {
               phb[i * m_br + j] = (double) (i + 1);
            }
        }System.out.print("\n");

        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_ar; j++) {
                phc[i * m_ar + j] = (double) 0;
            }
        }

        time1 = System.currentTimeMillis();

        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_br; j++) {
                for (k = 0; k < m_ar; k++) {
                    phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_ar + k];
                }
            }
        }

        time2 = System.currentTimeMillis();

        System.out.println("\nTime: " + (time2 - time1)/1000 + " seconds");
/*
        System.out.println("Result Matrix:");
        for (i = 0; i < min(10, m_br); i++) {
            System.out.print(phc[i] + " ");
        }
 */
        System.out.print("\n");
        return;
    }



    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int op = -1;
        do {
            try {
                System.out.println("1. Multiplication");
                System.out.println("2. Line Multiplication");
                System.out.print("Selection?: ");
                op = scanner.nextInt();
                scanner.nextLine();
                int dim = 0;
                switch (op) {
                    case 1:
                        dim = getDimensions(scanner);
                        onMult(dim, dim);
                        break;
                    case 2:
                        dim = getDimensions(scanner);
                        onMultLine(dim, dim);
                        break;
                    default:
                        return;
                }
            } catch (InputMismatchException e) {
                System.out.println("You must input a valid integer.");
                scanner.next();
            }
        } while (op != 0);
        scanner.close();
        return;
    }
}
