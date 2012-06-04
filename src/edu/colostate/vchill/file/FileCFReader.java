package edu.colostate.vchill.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * This class converts a cf file into a float[] of adjustment values
 *
 * @author  Brian Eriksson
 * @author  Jochen Deyke
 * @created June 30, 2003
 * @version 2005-06-23
 */
class FileCFReader
{
    private final File cfFile;
    private BufferedReader fin;

    /**
     * Constructor for the FileCFReader object
     *
     * @param CHLfile The file to read the cf for
     */
    public FileCFReader (final File CHLfile)
    {
        this.cfFile = new File(CHLfile.getAbsolutePath() + ".cf");
        try {
            this.fin = new BufferedReader(new InputStreamReader(new FileInputStream(cfFile)));
        } catch (FileNotFoundException fnfe) {
            System.err.println("File = " + cfFile.getAbsolutePath() + " is not found");
        } //end catch
    } //end method

    /**
     * Gets the array attribute of the FileCFReader object
     *
     * @return The array of adjustments
     */
    public float[] getArray ()
    {
        float[] returnArray = new float[4];
        if (!this.cfFile.exists()) {
            System.out.println("CF File does not exists, returning zero float array");
            return returnArray;
        } //end if
        char[] tempchararray = new char[1120];
        StringTokenizer strToken, strToken2;
        String tempString;
        String tempString2;

        try {
            int start = 0;
            while (start < tempchararray.length - 1) start += fin.read(tempchararray, start, tempchararray.length - 1);

            tempString = new String(tempchararray);
            //System.out.println("String tokened = " + tempString);

            strToken = new StringTokenizer(tempString, "\n");
            //System.out.println("Num tokens = " +strToken.countTokens());

            tempString2 = strToken.nextToken();
            //System.out.println("Token1 = " +tempString2);
            tempString2 = strToken.nextToken();
            //System.out.println("Token2 = " +tempString2);
            tempString2 = strToken.nextToken();
            //System.out.println("Token3 = " +tempString2);
            tempString2 = strToken.nextToken();
            //System.out.println("Token4 = " +tempString2);
            //tempString2 = strToken.nextToken();
            //System.out.println("Token5 = " +tempString2);

            //For the 8 other lines of the CF file
            for (int e = 0; e < 8; ++e) {
                tempString2 = strToken.nextToken();
                //System.out.println("Token = " +tempString2);

                if (tempString2.charAt(0) == '#'){
                    tempString2 = strToken.nextToken();
                }//end if

                strToken2 = new StringTokenizer(tempString2, " ");

                //Read the line, convert to tokens seperated by the spaces in the line
                boolean flagger = true;
                while (strToken2.hasMoreTokens() & flagger) {
                    tempString = strToken2.nextToken();

                    if (tempString.equalsIgnoreCase("ADJUST")) {
                        tempString = strToken2.nextToken();
                        tempString2 = strToken2.nextToken();

                        switch (tempString.charAt(1)) {
                            case 'Z': returnArray[0] = Float.parseFloat(tempString2); flagger = false; break;
                            case 'R': returnArray[1] = Float.parseFloat(tempString2); flagger = false; break;
                            case 'H': returnArray[2] = Float.parseFloat(tempString2); flagger = false; break;
                            case 'V': returnArray[3] = Float.parseFloat(tempString2); flagger = false; break;
                        } //end switch
                    } //end if
                } //end while
            } //end other for
        } catch (IOException ioe) {
            return returnArray;
        } //end catch
        return returnArray;
    } //end method
} //end class
