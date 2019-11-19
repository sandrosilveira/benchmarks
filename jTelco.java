/* ------------------------------------------------------------------ */
/* The 'telco' 0.52 benchmark in Java                                 */
/* ------------------------------------------------------------------ */
/* Copyright (c) IBM Corporation, 2001, 2002.  All rights reserved.   */
/* ------------------------------------------------------------------ */
/* Call as:                                                           */
/*                                                                    */
/*   jTelco [infile [outfile]] [flags]                                */
/*                                                                    */
/* where the parameters are:                                          */
/*   infile  -- input file name [default telco.test]                  */
/*   outfile -- output file name [default telco.outj]                 */
/*   flags   -- may be before or after file names, and may be         */
/*                                                                    */
/*              -nocalc  -- omit all calculations for each number     */
/*              -notax   -- omit tax calculations (and summations)    */
/*                                                                    */
/* See 'http://speleotrove.com/decimal/telco.html' for background     */
/* and details.                                                       */
/*                                                                    */
/* ------------------------------------------------------------------ */
import java.io.*;
import java.math.*;
import java.util.Formatter;

public class jTelco {
 public static void main(String args[]) {
  int numbers;                     // loop counter
  long msStart, msFinis;           // start/end times
  BufferedInputStream inp=null;    // files
  BufferedWriter oup=null;         // ..

  BigDecimal sumT, sumB, sumD;     // sums
  BigDecimal n = new BigDecimal(0);                    // number from file
  BigDecimal p = new BigDecimal(0);                    // price
  BigDecimal b = new BigDecimal(0);                    // base tax
  BigDecimal d = new BigDecimal(0);                    // distance tax
  BigDecimal t = new BigDecimal(0);                    // total price

  long    calltype;                // 0 or 1
  boolean calc=true;               // 1 for calculations, 0 to skip
  boolean tax=true;                // 1 for tax calculations, 0 to skip

  // call rates, tax rates and other constants
  BigDecimal baserate=new BigDecimal("0.0013");   // low call rate
  BigDecimal distrate=new BigDecimal("0.00894");  // high call rate
  BigDecimal basetax =new BigDecimal("0.0675");   // base tax rate
  BigDecimal disttax =new BigDecimal("0.0341");   // distance tax rate
  BigDecimal zero    =new BigDecimal("0");        // zero

  String  filein  ="telco.testb";  // default file names
  String  fileou  ="telco.outj";   // ..
  String  line = "";               // Output line
  byte[]  inbytes=new byte[8];     // input array

  int j=0;                                   // parameter number
  for (int i=0; i < args.length; i++) {
    if (args[i].startsWith("-")) {           // flag expected
      if (args[i].equals("-nocalc")) calc=false;
      else if (args[i].equals("-notax")) tax=false;
      else System.out.println("Flag '"+args[i]+"' ignored");
    } else {
      j++;                                   // have a parameter
      if (j==1) filein=args[i];              // is input file name
      else if (j==2) fileou=args[i];         // is output file name
      else System.out.println("Extra parameter '"+args[i]+"' ignored");
    }
  } // i; getting arguments

  System.out.println("telco Java benchmark; processing '"+filein+"'");

  File ofile=new File(fileou);
  if (ofile.exists()) ofile.delete();        // delete existing output

  /* ---------------------------------------------------------------- */
  /* Benchmark timing starts here                                     */
  /* ---------------------------------------------------------------- */
  msStart=System.currentTimeMillis();

  // you can continue to append data to sbuf here.

  sumT=zero;                       // zero accumulators
  sumB=zero;
  sumD=zero;


  // open files
  try {
    inp=new BufferedInputStream(new FileInputStream(filein));
  } catch (FileNotFoundException fnfe) {
    System.out.println("Error: file '"+filein+"' not found");
    return;
  }
  try {
    oup=new BufferedWriter(new FileWriter(fileou));
  } catch (IOException ioe) {
    try {
      inp.close();
    } catch (IOException ioe2) {
      // (ignore the exception on close)
    }
    System.out.println("Error: file '"+fileou+"' could not be created");
    return;
  }
  // From now on, files must be closed explicitly

  /* Start of the by-number loop */
  for (numbers=0; ; numbers++) {

    // get next 8-byte number into n
    try {
      int got=inp.read(inbytes, 0, 8);       // get 8 bytes
      if (got<0) break;                      // EOF
      long num=0;
      for (int bc=0; bc<8; bc++) {
        num=(num<<8) + (inbytes[bc] & 0xff); // [unsigned byte]
      }
      n=BigDecimal.valueOf(num, 0);
    }
    catch (IOException ioe) {
      System.out.println("Error: cannot read file '"+filein+"'");
      return;
    }

    if (calc) {
      calltype=n.longValue() & 0x01;         // last bit

      if (calltype==0)  {                    // p=r[c]*n
        p=baserate.multiply(n);
      } else {
        p=distrate.multiply(n);
      }
      p=p.setScale(2, BigDecimal.ROUND_HALF_EVEN); // to x.xx

      if (tax) {
        b=p.multiply(basetax);               // b=p*0.0675
        b=b.setScale(2, BigDecimal.ROUND_DOWN);
        sumB=sumB.add(b);
        t=p.add(b);

        if (calltype!=0) {
          d=p.multiply(disttax);             // b=p*0.0341
          d=d.setScale(2, BigDecimal.ROUND_DOWN);
          sumD=sumD.add(d);
          t=t.add(d);
        }
      } else {
        t=p;                                 // notax; copy price
      }
      sumT=sumT.add(t);
      line = String.format("    %s    D  |         %s         %s         %s |         0.38", n.toString(), p.toString(), b.toString(), d.toString());

    } else {
      line="0.77";                            // nocalc; simple String
    }

    try {
      oup.write(line, 0, line.length());       // output to file..
      oup.newLine();
    }
    catch (IOException ioe) {
      System.out.println("Error: cannot write file '"+fileou+"'");
      return;
    }
  } // numbers loop

  // flush amd close files
  try {
    oup.flush();
    oup.close();
    inp.close();
  }
  catch (IOException ioe) {
    System.out.println("Error: flush or close failed");
    return;
  }

  /* ---------------------------------------------------------------- */
  /* Benchmark timing ends here                                       */
  /* ---------------------------------------------------------------- */
  msFinis=System.currentTimeMillis();

  System.out.println("-- telco Java benchmark result --");
  System.out.println("   "+numbers+" numbers read from '"+filein+"'");
  showUs("Time per number", msStart, msFinis, numbers);
  System.out.println("--");
  System.out.println("   sumT = "+sumT);
  System.out.println("   sumB = "+sumB);
  System.out.println("   sumD = "+sumD);

  return;
  } // Main

 /* ----------------------------------------------------------------- */
 /* Calculate time per iteration and display                          */
 /*                                                                   */
 /*   Arg1 is title flag                                              */
 /*   Arg2 is start time (ms)                                         */
 /*   Arg3 is end   time (ms)                                         */
 /*   Arg4 is count (number of iterations)                            */
 /*                                                                   */
 /* Returns the time in microseconds [unscaled]                       */
 /* ----------------------------------------------------------------- */
 public static double showUs(String sTitle,
                             long msStart, long msFinis,
                             int iCount) {
  double dDiffms;             // float time in ms
  double dCount=iCount;       // count as a double
  double dUs;                 // time per iteration in microseconds

  dDiffms=(double)(msFinis-msStart);
  dUs=(dDiffms*1000/dCount);
  sTitle=right(sTitle, 16);   // right-align label

  String s=Double.toString(dUs+0.0005);   // round up
  int dot=s.indexOf('.');                 // will always be a dot
  if (s.indexOf('E')<0)                   // unless E-format..
    s=s.substring(0, dot+4);              // make three decimal places
  System.out.println("  "+sTitle+" "+right(s, 11)+"us  ["+iCount+"]");
  return dUs;
 } // showus

 /* ----------------------------------------------------------------- */
 /* Right-align a string (truncate on left if too long)               */
 /*                                                                   */
 /*   Arg1 is string to align                                         */
 /*   Arg2 is length of the returned string                           */
 /* ----------------------------------------------------------------- */
 static String right(String s, int len) {
  int slen = s.length();
  if (slen == len) return s;                 // length just right
  if (slen > len)
      return s.substring(slen-len);          // truncate on left
  // too short
  return (new String(new char[len-slen])).replace('\0', ' ').concat(s);
 } // right

} // class
