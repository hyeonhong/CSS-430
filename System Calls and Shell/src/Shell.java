
/**
 *
 * @author  Hyeon Hong
 *
 * Lab 1 - Part 2
 * 07/05/2016
 *
 *
 * Shell class simulates the functionality of shell used in Unix.
 *
 * The shell simply repeats the following behavior:
 * 1. Displaying a prompt to tell user that it is ready to accept a next command
 * 2. Reading a line of keyboard input as a command
 * 3. Spawning and having a new process execute the user command
 *
 * ';' and '&' are used as a delimiter specifying the end of each single command.
 * If a command is delimited by ';',
 *      the shell spawns a new process to execute this command,
 *      waits for the process to be terminated,
 *      and then continues to interpret the next command.
 * If a command is delimited by '&',
 *      the shell execution continues by interpreting the next command
 *      without waiting for the completion of the current command.
 *
 *
 * Assumptions:
 * 1. User input is expected to be properly formatted with correct usage of command.
 * 2. No other delimiter symbol is used other than ';' and '&'.
 * 3. In each line, only one single program is used.
 *    For example, the following line calls one program named 'PingPong' and no other programs.
 *    "PingPong abc 50 & PingPong 123 50 & PingPong xyz 50 ;"
 *
 */


public class Shell extends Thread {

    private String cmdLine; // string that contains the full command line

    // constructor
    public Shell() {
        cmdLine = "";
    }

    public void run() {

        int count = 1; // the number of command line input

        for (;;) {

            SysLib.cout("shell[" + count + "]% ");

            // read a command line from keyboard input
            StringBuffer sb = new StringBuffer();
            SysLib.cin(sb);

            // store it as string
            cmdLine = sb.toString();

            if (cmdLine.equals("exit")) {
                break;
            }

            // Hitting enter will simply move the cursor to next line.
            if (cmdLine.equals("")) {
                continue;
            }

            SysLib.cout("\n");
            count++;

            // split the line into words as elements of array
            String[] args = SysLib.stringToArgs(cmdLine);

            // find out how many arguments one command passes in
            // for example, the command "PingPong abc 50" has 2 arguments (abc, 50)
            int numOfArg = 0;
            for (int i = 0; i < args.length; i++) {
               if (i == 0) {  // first element is the program itself
                   continue;
               } else if (!args[i].equals(";") && !args[i].equals("&")){  // if it's not ';','&'
                   numOfArg++;
               } else {  // reached the delimiter(;,&)
                   break;
               }
            }

            int threadCount = 0;  // keeps track of number of concurrent threads


            // traverse the command line in set increments (numOfArg + 2),
            // which is the same as the total number of elements for one command
            for (int i = 0; i < args.length ; i += numOfArg + 2){
                SysLib.cout(args[i] + "\n");

                // create an array that can hold the total arguments for one command
                String[] args2 = new String[numOfArg + 1];


                // fill the array (args2) with only the elements of one command
                // for example, array will be filled with "PingPong", "abc", "50"
                // in the case of the command line "PingPong abc 50 & PingPong 123 50 ;"
                int temp = i;
                for (int j = 0; j <= numOfArg; j++, temp++) {
                    args2[j] = args[temp];
                }

                // run the command and check for error
                if (SysLib.exec(args2) < 0){
                    SysLib.cout("Error: exec failed");
                    return;
                }

                // process the delimiter part
                if (i + numOfArg + 1 < args.length){  // if there is a delimiter

                    if (args[i+ numOfArg + 1].equals(";")){  // if delimiter is ';'

                        // There is a possibility that more than one thread are
                        // finished from previous calls and need to be joined.
                        // Join all of those finished concurrent threads first.
                        for (int j = 0; j < threadCount; j++){
                            SysLib.join();
                        }
                        threadCount = 0;  // reset to 0 for later use

                        // Join the current one
                        SysLib.join();

                    } else if (args[i + numOfArg + 1].equals("&")){  // if delimiter is '&'

                        if (i + numOfArg + 2 == args.length){  // if '&' is at the end of command line

                            // This will be treated the same as ';'

                            // There is a possibility that more than one thread are
                            // finished from previous calls and need to be joined.
                            // Join all of those finished concurrent threads first.
                            for (int j = 0; j < threadCount; j++){
                                SysLib.join();
                            }
                            // ampDelay = 0; // this is not necessary since it's the last one


                            // Join the current one
                            SysLib.join();

                        } else {  // if '&' is expecting next command

                            threadCount++;  // keep track of concurrent threads that are running

                            continue;  // move on to next command without waiting
                        }
                    } else {  // if it's neither ';' nor '&'
                        SysLib.cout("Error: Invalid command");
                        return;
                    }

                } else {  // if there's no delimiter,

                    // This will be treated the same as ';'

                    // There is a possibility that more than one thread are
                    // finished from previous calls and need to be joined.
                    // Join all of those finished concurrent threads first.
                    for (int j = 0; j < threadCount; j++){
                        SysLib.join();
                    }
                    threadCount = 0; // reset to 0 for later use

                    // Join the current one
                    SysLib.join();
                }
            }
            SysLib.cout("\n");
        }

        SysLib.cout("Done!\n");
        SysLib.exit();

    }
}