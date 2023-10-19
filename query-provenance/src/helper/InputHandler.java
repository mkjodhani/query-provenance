package helper;

import java.util.Scanner;


/**
 * @author mkjodhani
 * @project Query Provenance
 * @version 1
 */
public class InputHandler {
    static private Scanner scanner = new Scanner(System.in);
    private static final int padding = 70;
    private static void prefixInputLabel(){
        System.out.print(">>> ");
    }
    /**
     * takes input from the user
     * @param label description of the user input
     * @return valid integer value
     */
    public static int getInteger(String label){
        int input;
        while (true){
            try{
                System.out.println(label);
                prefixInputLabel();
                input = Integer.parseInt(scanner.nextLine());
                return input;
            }
            catch (Exception e){
                System.out.println("Please enter digit as a input for given field.");
            }
        }
    }

    /**
     *
     * @param label provide the description of input
     * @param min provide min value for given input
     * @param max provide max value for given input
     * @return valid int value
     */
    public static int getIntegerInRange(String label,int min,int max){
        int input;
        while (true){
            try{
                System.out.println(label);
                prefixInputLabel();
                input = Integer.parseInt(scanner.nextLine());
                if(input >= min && input <= max){
                    return input;
                }
                else {
                    System.out.println(String.format("Please enter the value between %d and %d.",min,max));
                }
            }
            catch (Exception e){
                System.out.println("Please enter digit as a input for given field.");
            }
        }
    }

    /**
     * takes input from the user
     * @param label description of the user input
     * @return valid decimal value
     */
    public static float getFloat(String label){
        float input;
        while (true){
            try{
                System.out.println(label);
                prefixInputLabel();

                input = Float.parseFloat(scanner.nextLine());
                return input;
            }
            catch (Exception e){
                System.out.println("Please enter floating number as a input for given field.");
            }
        }
    }

    /**
     *
     * @param label
     * @return boolean value
     */

    public static boolean getBoolean(String label, String option1, String option2){
        String input;
        while (true){
            try{
                System.out.println(String.format("%s(%s/%s)",label,option1,option2));
                prefixInputLabel();
                input = scanner.nextLine();
                if(input.equals(option1)){
                    return true;
                }else if(input.equals(option2)){
                    return false;
                }
                else{
                    throw new Exception("Invalid Input");
                }
            }
            catch (Exception e){
                System.out.println(String.format("Please provide one of the available options(%s/%s).",option1,option2));
            }
        }
    }

    /**
     * takes input from the user
     * @param label description of the user input
     * @return valid string
     */
    public static String getString(String label){
        String input;
        while (true){
            try{
                System.out.println(label);
                prefixInputLabel();
                input = scanner.nextLine().trim();
                return input;
            }
            catch (Exception e){
                System.out.println("Please enter digit as a input for given field.");
            }
        }
    }

    public static void printError(String error){
        System.out.print(getHorizontalLine());
        System.out.print(getRightPaddingString("Error"));
        System.out.print(getLeftPaddingString(error));
        System.out.print(getHorizontalLine());
    }
    public static void printMessage(String message){
        System.out.println(getHorizontalLine());
        System.out.println(getRightPaddingString("Success"));
        System.out.println(getLeftPaddingString(message));
        System.out.println(getHorizontalLine());
    }
    public static String getHorizontalLine(){
        String string = "";
        for (int i=0;i<InputHandler.padding;i++){
            string += "-";
        }
        string += "\n";
        return string;
    }
    public static String getRightPaddingString(String value){
        return String.format("|%-"+(InputHandler.padding-2)+"s|\n",value);
    }
    public static String getLeftPaddingString(String value){
        return String.format("|%"+(InputHandler.padding-2)+"s|\n",value);
    }

}