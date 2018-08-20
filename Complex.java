/******************************************************************************
 * DISCLAIMER: STRAIGHT FROM PRINCETON IMPLEMENTATION
 *  Compilation:  javac Complex.java
 *  Execution:    java Complex
 *
 *  Data type for complex numbers.
 *
 *  The data type is "immutable" so once you create and initialize
 *  a Complex object, you cannot change it. The "final" keyword
 *  when declaring re and im enforces this rule, making it a
 *  compile-time error to change the .re or .im instance variables after
 *  they've been initialized.
 *
 *  % java Complex
 *  a            = 5.0 + 6.0i
 *  b            = -3.0 + 4.0i
 *  Re(a)        = 5.0
 *  Im(a)        = 6.0
 *  b + a        = 2.0 + 10.0i
 *  a - b        = 8.0 + 2.0i
 *  a * b        = -39.0 + 2.0i
 *  b * a        = -39.0 + 2.0i
 *  a / b        = 0.36 - 1.52i
 *  (a / b) * b  = 5.0 + 6.0i
 *  conj(a)      = 5.0 - 6.0i
 *  |a|          = 7.810249675906654
 *  tan(a)       = -6.685231390246571E-6 + 1.0000103108981198i
 *
 ******************************************************************************/

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;

import javax.lang.model.type.NullType;
import java.util.Objects;

public class Complex {
    private final double re;   // the real part
    private final double im;   // the imaginary part

    // create a new object with the given real and imaginary parts
    public Complex(double real, double imag) {
        re = real;
        im = imag;
    }

    // return a string representation of the invoking Complex object
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im < 0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    // return abs/modulus/magnitude
    public double abs() {
        return Math.hypot(re, im);
    }

    // return angle/phase/argument, normalized to be between -pi and pi
    public double phase() {
        return Math.atan2(im, re);
    }

    // return a new Complex object whose value is (this + b)
    public Complex plus(Complex b) {
        Complex a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex(real, imag);
    }

    public Complex power(int b) {
        Complex a = this;
        Complex result = a;
        for (int i = 0; i < b-1; i++) {
            result = result.times(a);
        }
        return result;
    }

    // return a new object whose value is (this * alpha)
    public Complex scale(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    // return a new Complex object whose value is the conjugate of this
    public Complex conjugate() {
        return new Complex(re, -im);
    }

    // return a new Complex object whose value is the reciprocal of this
    public Complex reciprocal() {
        double scale = re * re + im * im;
        return new Complex(re / scale, -im / scale);
    }

    // return the real or imaginary part
    public double re() {
        return re;
    }

    public double im() {
        return im;
    }

    // return a / b
    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    // return a new Complex object whose value is the complex exponential of this
    public Complex exp() {
        return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    // return a new Complex object whose value is the complex sine of this
    public Complex sin() {
        return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex cosine of this
    public Complex cos() {
        return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex tangent of this
    public Complex tan() {
        return sin().divides(cos());
    }

    //our own fft
    public static Complex[] fft(Complex[] a, Complex omega) {
//        System.out.println("calling fft");
//        System.out.println(a[a.length - 1]);
//        System.out.println(a[0]);
        int count = 0;
        for (int i = 0; i < a.length; i ++) {
            if (a[i] == null) {
                count++;
            }

        }
//        System.out.print("count is ");
//        System.out.println(count);

        if (a.length == 1) {
//            System.out.println("In base case");
//            System.out.print("a[0] is: ");
//            System.out.println(a[0]);
            return a;
        } else {
            int len = a.length;
//            System.out.println(len);
//            System.out.print("omega real: ");
//            System.out.println(omega.re());
//            System.out.print("omega imag: ");
//            System.out.println(omega.im());

            Complex[] result = new Complex[len];

            // fft of even terms
            Complex[] e = new Complex[len / 2];
            for (int i=0; i<len/2; i++) {
                e[i] = a[2*i];
            }

//            System.out.print("parent is of length ");
//            System.out.println(len);
            Complex[] x = fft(e, omega.times(omega));

            // fft of odd terms
            Complex[] o = new Complex[len / 2];
//            System.out.print("length of o: ");
//            System.out.println(o.length);
            for (int i=0; i<len/2; i++) {
//                System.out.print("Print a ");
//                System.out.print(a[0]);
//                System.out.print(a[1]);
//                System.out.println(a[i]);
                o[i] = a[2*i+1];
//                System.out.println("Print this: ");
//                System.out.println(2*i+1);
//                System.out.print("o[i] is: ");
//                System.out.println(o[i]);
            }

//            System.out.print("parent is of length ");
//            System.out.println(len);
            Complex[] y = fft(o, omega.times(omega));

            for (int i = 0; i < len/2; i++) {
//                System.out.println("x[i] is: ");
//                System.out.println(x[i]);
//                System.out.println("y[i] is: ");
//                System.out.println(y[i]);
                Complex wi = omega.power(i);
                result[i] = a[i].plus(wi.times(y[i]));
                result[i + len / 2] = a[i].minus(wi.times(y[i]));
            }
//            System.out.println("Completed fft");
            return result;
        }
    }

//    public static Complex[] fft(Complex[] x) {
//        if (x == null) {
//            System.out.println("poopsters");
//            return null;
//        }
//        int len = x.length;
//        System.out.println("length");
//        System.out.println(len);
//
//        if (len == 1 ) {
//            System.out.println("Length is 1");
//            return x;
//        }
//
//        Complex[] result = new Complex[len];
//
//        // fft of even terms
//        Complex[] e = new Complex[len / 2];
//        for (int i=0; i<len/2; i++) {
//            e[i] = x[2*i];
//        }
//
//        if (len > 0) {
//            System.out.println("INSIDE length > 0");
//            System.out.println(len);
//            Complex[] a = fft(e);
//
//            // fft of odd terms
//            Complex[] o = e; // reuse the array
//            for (int i=0; i<len/2; i++) {
//                o[i] = x[2*i+1];
//            }
//
//            Complex[] b = fft(o);
//
//            //combine
//            if (a != null || b != null) {
//                for (int i = 0; i < len / 2; i++) {
//                    double kth = -2 * i * Math.PI / len;
//                    Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
//                    System.out.println("A is: ");
//                    System.out.println(a[i]);
//                    System.out.println("B is: ");
//                    System.out.println(b[i]);
//                    result[i] = a[i].plus(wk.times(b[i]));
//                    result[i + len / 2] = a[i].minus(wk.times(b[i]));
//                }
//                return result;
//            }
//        }
//        return result;
//    }
}